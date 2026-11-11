package net.nextfur.fwc.economy.db;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.db.records.CheckRecord;
import net.nextfur.fwc.economy.db.records.GlobalEconomyStats;
import net.nextfur.fwc.economy.db.records.TransactionRecord;
import net.nextfur.fwc.economy.db.records.WalletSnapshotRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EconomyDatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EconomyDatabaseManager.class);
    private static final String EMBEDDED_SQLITE_RESOURCE = "META-INF/libraries/sqlite-jdbc.jar";
    private static final String EMBEDDED_SQLITE_JAR_NAME = "sqlite-jdbc-3.46.1.0.jar";
    private static final String[] DRIVER_CLASS_CANDIDATES = {
            "org.sqlite.JDBC",
            "com.fursmp.shadow.sqlite.JDBC"
    };

    private static EconomyDatabaseManager instance;
    private static volatile Driver cachedSqliteDriver;
    private static volatile IsolatedSqliteClassLoader isolatedClassLoader;

    private Connection connection;
    private ExecutorService dbExecutor = createExecutor();

    private EconomyDatabaseManager() {}

    /**
     * Child-first URLClassLoader that isolates {@code org.sqlite.*} classes and native library
     * resources from Youer/Paper and other mods while delegating JDK ({@code java.sql.*}) and
     * logging ({@code org.slf4j.*}) classes to the parent classloader.
     */
    private static final class IsolatedSqliteClassLoader extends URLClassLoader {
        static {
            ClassLoader.registerAsParallelCapable();
        }

        IsolatedSqliteClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        private static boolean isIsolatedSqliteEntry(String name) {
            return name.startsWith("org.sqlite.")
                    || name.startsWith("org/sqlite/")
                    || name.startsWith("META-INF/maven/org.xerial/")
                    || name.startsWith("META-INF/native-image/org.xerial/")
                    || name.equals("sqlite-jdbc.properties");
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    if (isIsolatedSqliteEntry(name)) {
                        try {
                            c = findClass(name);
                        } catch (ClassNotFoundException ignored) {
                        }
                    }
                    if (c == null) {
                        c = super.loadClass(name, false);
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }

        @Override
        public URL getResource(String name) {
            if (isIsolatedSqliteEntry(name)) {
                URL url = findResource(name);
                if (url != null) {
                    return url;
                }
            }
            return super.getResource(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            if (isIsolatedSqliteEntry(name)) {
                List<URL> urls = Collections.list(findResources(name));
                if (!urls.isEmpty()) {
                    return Collections.enumeration(urls);
                }
            }
            return super.getResources(name);
        }
    }

    private static ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "FurWatch-Economy-DB");
            thread.setDaemon(true);
            return thread;
        });
    }

    private synchronized ExecutorService getExecutor() {
        if (dbExecutor == null || dbExecutor.isShutdown() || dbExecutor.isTerminated()) {
            dbExecutor = createExecutor();
        }
        return dbExecutor;
    }

    public static synchronized EconomyDatabaseManager getInstance() {
        if (instance == null) {
            instance = new EconomyDatabaseManager();
        }
        return instance;
    }

    private Connection connectSqlite(String dbUrl, Path dbDirectory) throws Exception {
        Driver existingDriver = cachedSqliteDriver;
        if (existingDriver != null) {
            Connection conn = existingDriver.connect(dbUrl, new Properties());
            if (conn != null) {
                return conn;
            }
        }

        synchronized (EconomyDatabaseManager.class) {
            if (cachedSqliteDriver != null) {
                Connection conn = cachedSqliteDriver.connect(dbUrl, new Properties());
                if (conn != null) {
                    return conn;
                }
            }

            Exception lastError = null;

            // 1. Primary strategy: Load embedded META-INF/libraries/sqlite-jdbc.jar in an isolated
            // child-first URLClassLoader so JPMS/FML never sees duplicate modules or split packages,
            // while JNI native bindings (Java_org_sqlite_core_NativeDB_*) remain intact.
            try {
                Connection conn = connectViaIsolatedEmbeddedJar(dbUrl, dbDirectory);
                if (conn != null) {
                    return conn;
                }
            } catch (Exception e) {
                lastError = e;
                LOGGER.debug("[FurWatch Economy] Isolated embedded SQLite loader fallback triggered: {}", e.getMessage());
            }

            // 2. Fallback strategy: Try already-loaded SQLite drivers across accessible classloaders
            // (e.g., Youer/Paper server classpath, dev environment, or relocated driver).
            ClassLoader[] loaders = new ClassLoader[]{
                    EconomyDatabaseManager.class.getClassLoader(),
                    Thread.currentThread().getContextClassLoader(),
                    ClassLoader.getSystemClassLoader()
            };

            for (String driverClassName : DRIVER_CLASS_CANDIDATES) {
                for (ClassLoader loader : loaders) {
                    if (loader == null) continue;
                    try {
                        Class<?> clazz = Class.forName(driverClassName, true, loader);
                        Driver driver = (Driver) clazz.getDeclaredConstructor().newInstance();
                        Connection conn = connectWithNativeIsolation(driver, dbUrl, dbDirectory);
                        if (conn != null) {
                            cachedSqliteDriver = driver;
                            return conn;
                        }
                    } catch (Throwable t) {
                        if (t instanceof Exception ex) {
                            lastError = ex;
                        } else {
                            lastError = new SQLException("Failed to load SQLite driver " + driverClassName, t);
                        }
                    }
                }
            }

            // 3. Final fallback: DriverManager
            try {
                Connection conn = DriverManager.getConnection(dbUrl);
                if (conn != null) {
                    return conn;
                }
            } catch (Exception e) {
                if (lastError == null) {
                    lastError = e;
                }
            }

            if (lastError != null) {
                throw lastError;
            }
            throw new SQLException("No suitable SQLite JDBC driver found for " + dbUrl);
        }
    }

    private static Connection connectViaIsolatedEmbeddedJar(String dbUrl, Path dbDirectory) throws Exception {
        if (isolatedClassLoader == null) {
            Path extractedJar = extractEmbeddedSqliteJar(dbDirectory);
            if (extractedJar == null) {
                return null;
            }
            isolatedClassLoader = new IsolatedSqliteClassLoader(
                    new URL[]{extractedJar.toUri().toURL()},
                    EconomyDatabaseManager.class.getClassLoader()
            );
        }

        Class<?> clazz = Class.forName("org.sqlite.JDBC", true, isolatedClassLoader);
        Driver driver = (Driver) clazz.getDeclaredConstructor().newInstance();
        Connection conn = connectWithNativeIsolation(driver, dbUrl, dbDirectory);
        if (conn != null) {
            cachedSqliteDriver = driver;
        }
        return conn;
    }

    private static Connection connectWithNativeIsolation(Driver driver, String dbUrl, Path dbDirectory) throws Exception {
        // Temporarily clear org.sqlite.lib.path if set by another mod/Youer so SQLiteJDBCLoader
        // extracts a unique UUID-named native library for our ClassLoader instead of colliding
        // with a native binary path already bound to another ClassLoader.
        String prevLibPath = System.getProperty("org.sqlite.lib.path");
        if (prevLibPath != null) {
            System.clearProperty("org.sqlite.lib.path");
        }
        try {
            return driver.connect(dbUrl, new Properties());
        } catch (Throwable firstError) {
            // Retry with a dedicated native temp directory inside furwatch/libs/native in case
            // the host OS mounts /tmp (java.io.tmpdir) with noexec.
            if (dbDirectory != null) {
                Path nativeTmpDir = dbDirectory.resolve("libs").resolve("native");
                Files.createDirectories(nativeTmpDir);
                String prevTmpDir = System.getProperty("org.sqlite.tmpdir");
                System.setProperty("org.sqlite.tmpdir", nativeTmpDir.toAbsolutePath().toString());
                try {
                    return driver.connect(dbUrl, new Properties());
                } finally {
                    if (prevTmpDir != null) {
                        System.setProperty("org.sqlite.tmpdir", prevTmpDir);
                    } else {
                        System.clearProperty("org.sqlite.tmpdir");
                    }
                }
            }
            if (firstError instanceof Exception ex) {
                throw ex;
            }
            throw new SQLException("SQLite native initialization failed", firstError);
        } finally {
            if (prevLibPath != null) {
                System.setProperty("org.sqlite.lib.path", prevLibPath);
            }
        }
    }

    private static Path extractEmbeddedSqliteJar(Path dbDirectory) throws IOException {
        InputStream in = EconomyDatabaseManager.class.getClassLoader().getResourceAsStream(EMBEDDED_SQLITE_RESOURCE);
        if (in == null) {
            in = EconomyDatabaseManager.class.getResourceAsStream("/" + EMBEDDED_SQLITE_RESOURCE);
        }
        if (in == null) {
            return null;
        }

        try (InputStream stream = in) {
            Path targetDir;
            if (dbDirectory != null) {
                targetDir = dbDirectory.resolve("libs");
            } else {
                targetDir = Paths.get(System.getProperty("java.io.tmpdir"), "furwatch-sqlite");
            }
            Files.createDirectories(targetDir);
            Path targetJar = targetDir.resolve(EMBEDDED_SQLITE_JAR_NAME);

            Path tempJar = targetDir.resolve(EMBEDDED_SQLITE_JAR_NAME + ".tmp");
            Files.copy(stream, tempJar, StandardCopyOption.REPLACE_EXISTING);
            long newSize = Files.size(tempJar);

            if (!Files.exists(targetJar) || Files.size(targetJar) != newSize) {
                try {
                    Files.move(tempJar, targetJar, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException moveEx) {
                    // On Windows, if targetJar is already open in a previous ClassLoader, keep using targetJar
                    Files.deleteIfExists(tempJar);
                }
            } else {
                Files.deleteIfExists(tempJar);
            }
            return targetJar;
        }
    }

    public synchronized void initialize(MinecraftServer server) {
        try {
            getExecutor();
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }

            Path dbDirectory;
            try {
                dbDirectory = server.getWorldPath(LevelResource.ROOT).resolve("furwatch").normalize();
            } catch (Exception e) {
                dbDirectory = server.getServerDirectory().resolve("furwatch").normalize();
            }

            File dirFile = dbDirectory.toFile();
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            File dbFile = dbDirectory.resolve("economy.db").toFile();
            String dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            LOGGER.info("[FurWatch Economy] Connecting to SQLite database at: {}", dbFile.getAbsolutePath());
            this.connection = connectSqlite(dbUrl, dbDirectory);

            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode = WAL;");
                st.execute("PRAGMA synchronous = NORMAL;");
            }

            initTables();
            LOGGER.info("[FurWatch Economy] Database initialized successfully.");
        } catch (Exception e) {
            LOGGER.error("[FurWatch Economy] Failed to initialize SQLite database!", e);
        }
    }

    private void initTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS economy_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tx_uuid TEXT NOT NULL UNIQUE,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    tx_type TEXT NOT NULL,
                    player_uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL,
                    target_uuid TEXT,
                    target_name TEXT,
                    amount_cents INTEGER NOT NULL,
                    wallet_balance_after INTEGER NOT NULL,
                    details TEXT
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS economy_checks (
                    check_uuid TEXT PRIMARY KEY,
                    issuer_uuid TEXT NOT NULL,
                    issuer_name TEXT NOT NULL,
                    payee_name TEXT NOT NULL,
                    amount_cents INTEGER NOT NULL,
                    issued_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    status TEXT NOT NULL,
                    deposited_by_uuid TEXT,
                    deposited_by_name TEXT,
                    deposited_at DATETIME
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS economy_wallet_snapshots (
                    player_uuid TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    wallet_id TEXT NOT NULL,
                    last_balance_cents INTEGER NOT NULL DEFAULT 0,
                    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS economy_wallets (
                    wallet_uuid TEXT PRIMARY KEY,
                    owner_uuid TEXT NOT NULL,
                    owner_name TEXT NOT NULL,
                    balance_cents INTEGER NOT NULL DEFAULT 0,
                    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS economy_bank_accounts (
                    pda_uuid TEXT PRIMARY KEY,
                    owner_uuid TEXT NOT NULL,
                    owner_name TEXT NOT NULL,
                    balance_cents INTEGER NOT NULL DEFAULT 0,
                    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            st.execute("CREATE INDEX IF NOT EXISTS idx_tx_player ON economy_transactions(player_uuid);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_tx_target ON economy_transactions(target_uuid);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_checks_issuer ON economy_checks(issuer_uuid, status);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_checks_status ON economy_checks(status);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_snapshots_name ON economy_wallet_snapshots(player_name COLLATE NOCASE);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_wallets_owner ON economy_wallets(owner_uuid);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_bank_owner ON economy_bank_accounts(owner_uuid);");
        }
    }

    public CompletableFuture<Void> logTransaction(
            String txType,
            UUID playerUuid,
            String playerName,
            UUID targetUuid,
            String targetName,
            long amountCents,
            long balanceAfter,
            String details
    ) {
        return CompletableFuture.runAsync(() -> {
            if (connection == null) return;
            String sql = """
                INSERT INTO economy_transactions (
                    tx_uuid, tx_type, player_uuid, player_name, target_uuid, target_name,
                    amount_cents, wallet_balance_after, details
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, txType);
                ps.setString(3, playerUuid.toString());
                ps.setString(4, playerName);
                ps.setString(5, targetUuid != null ? targetUuid.toString() : null);
                ps.setString(6, targetName);
                ps.setLong(7, amountCents);
                ps.setLong(8, balanceAfter);
                ps.setString(9, details);
                ps.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Failed to log transaction", e);
            }
        }, getExecutor());
    }

    public CompletableFuture<Boolean> registerCheck(
            UUID checkId,
            UUID issuerUuid,
            String issuerName,
            String payee,
            long amountCents
    ) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null) return false;
            String sql = """
                INSERT OR IGNORE INTO economy_checks (
                    check_uuid, issuer_uuid, issuer_name, payee_name, amount_cents, status
                ) VALUES (?, ?, ?, ?, ?, 'ACTIVE');
            """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, checkId.toString());
                ps.setString(2, issuerUuid.toString());
                ps.setString(3, issuerName);
                ps.setString(4, payee);
                ps.setLong(5, amountCents);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Failed to register check: " + checkId, e);
                return false;
            }
        }, getExecutor());
    }

    public CompletableFuture<DepositResult> depositCheck(UUID checkId, UUID depositorUuid, String depositorName) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null) return DepositResult.ERROR;
            String selectSql = "SELECT status, amount_cents, issuer_uuid, issuer_name FROM economy_checks WHERE check_uuid = ?;";
            try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                ps.setString(1, checkId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return DepositResult.NOT_FOUND;
                    }
                    String status = rs.getString("status");
                    if ("DEPOSITED".equalsIgnoreCase(status)) {
                        return DepositResult.ALREADY_DEPOSITED;
                    }
                    if ("CANCELLED".equalsIgnoreCase(status)) {
                        return DepositResult.CANCELLED;
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error checking check status: " + checkId, e);
                return DepositResult.ERROR;
            }

            String updateSql = """
                UPDATE economy_checks
                SET status = 'DEPOSITED', deposited_by_uuid = ?, deposited_by_name = ?, deposited_at = CURRENT_TIMESTAMP
                WHERE check_uuid = ? AND status = 'ACTIVE';
            """;
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setString(1, depositorUuid.toString());
                ps.setString(2, depositorName);
                ps.setString(3, checkId.toString());
                int updated = ps.executeUpdate();
                return updated > 0 ? DepositResult.SUCCESS : DepositResult.ALREADY_DEPOSITED;
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error updating check status for deposit: " + checkId, e);
                return DepositResult.ERROR;
            }
        }, getExecutor());
    }

    public CompletableFuture<List<CheckRecord>> getActiveChecksByIssuer(UUID issuerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<CheckRecord> list = new ArrayList<>();
            if (connection == null) return list;
            String sql = "SELECT * FROM economy_checks WHERE issuer_uuid = ? AND status = 'ACTIVE' ORDER BY issued_at DESC;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, issuerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapCheckRecord(rs));
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error fetching active checks for: " + issuerUuid, e);
            }
            return list;
        }, getExecutor());
    }

    public CompletableFuture<Long> getTotalActiveChecksAmount(UUID issuerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null) return 0L;
            String sql = "SELECT COALESCE(SUM(amount_cents), 0) AS total FROM economy_checks WHERE issuer_uuid = ? AND status = 'ACTIVE';";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, issuerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("total");
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error calculating active checks total for: " + issuerUuid, e);
            }
            return 0L;
        }, getExecutor());
    }

    public CompletableFuture<List<TransactionRecord>> getRecentTransactions(UUID playerUuid, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<TransactionRecord> list = new ArrayList<>();
            if (connection == null) return list;
            String sql = "SELECT * FROM economy_transactions WHERE player_uuid = ? OR target_uuid = ? ORDER BY id DESC LIMIT ?;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setString(2, playerUuid.toString());
                ps.setInt(3, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapTransactionRecord(rs));
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error fetching transactions for: " + playerUuid, e);
            }
            return list;
        }, getExecutor());
    }

    public CompletableFuture<Void> updateWalletSnapshot(UUID playerUuid, String playerName, UUID walletId, long balanceCents) {
        return CompletableFuture.runAsync(() -> {
            if (connection == null) return;
            String sql = """
                INSERT INTO economy_wallet_snapshots (player_uuid, player_name, wallet_id, last_balance_cents, last_updated)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    player_name = excluded.player_name,
                    wallet_id = excluded.wallet_id,
                    last_balance_cents = excluded.last_balance_cents,
                    last_updated = CURRENT_TIMESTAMP;
            """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setString(2, playerName);
                ps.setString(3, walletId != null ? walletId.toString() : "");
                ps.setLong(4, balanceCents);
                ps.executeUpdate();

                if (walletId != null) {
                    upsertWalletSync(walletId, playerUuid, playerName, balanceCents);
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error updating wallet snapshot for: " + playerUuid, e);
            }
        }, getExecutor());
    }

    public CompletableFuture<WalletSnapshotRecord> getWalletSnapshot(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null) return null;
            String sql = "SELECT * FROM economy_wallet_snapshots WHERE player_uuid = ?;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapSnapshotRecord(rs);
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error fetching wallet snapshot for: " + playerUuid, e);
            }
            return null;
        }, getExecutor());
    }

    public CompletableFuture<WalletSnapshotRecord> getWalletSnapshotByName(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null) return null;
            String sql = "SELECT * FROM economy_wallet_snapshots WHERE player_name = ? COLLATE NOCASE;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapSnapshotRecord(rs);
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error fetching wallet snapshot by name: " + playerName, e);
            }

            // Fallback: check economy_wallets, economy_bank_accounts, or economy_transactions
            String fallbackSql = """
                SELECT owner_uuid AS p_uuid, owner_name AS p_name, last_updated FROM economy_wallets WHERE owner_name = ? COLLATE NOCASE
                UNION ALL
                SELECT owner_uuid AS p_uuid, owner_name AS p_name, last_updated FROM economy_bank_accounts WHERE owner_name = ? COLLATE NOCASE
                UNION ALL
                SELECT player_uuid AS p_uuid, player_name AS p_name, timestamp AS last_updated FROM economy_transactions WHERE player_name = ? COLLATE NOCASE
                LIMIT 1;
            """;
            try (PreparedStatement ps = connection.prepareStatement(fallbackSql)) {
                ps.setString(1, playerName);
                ps.setString(2, playerName);
                ps.setString(3, playerName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new WalletSnapshotRecord(
                                UUID.fromString(rs.getString("p_uuid")),
                                rs.getString("p_name"),
                                null,
                                0L,
                                rs.getString("last_updated")
                        );
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error in fallback player lookup by name: " + playerName, e);
            }
            return null;
        }, getExecutor());
    }

    private void upsertWalletSync(UUID walletId, UUID ownerUuid, String ownerName, long balanceCents) throws SQLException {
        if (connection == null || walletId == null) return;
        String sql = """
            INSERT INTO economy_wallets (wallet_uuid, owner_uuid, owner_name, balance_cents, last_updated)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(wallet_uuid) DO UPDATE SET
                owner_uuid = excluded.owner_uuid,
                owner_name = excluded.owner_name,
                balance_cents = excluded.balance_cents,
                last_updated = CURRENT_TIMESTAMP;
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, walletId.toString());
            ps.setString(2, ownerUuid != null ? ownerUuid.toString() : "");
            ps.setString(3, ownerName != null ? ownerName : "Desconhecido");
            ps.setLong(4, balanceCents);
            ps.executeUpdate();
        }
    }

    public CompletableFuture<Void> upsertWallet(UUID walletId, UUID ownerUuid, String ownerName, long balanceCents) {
        return CompletableFuture.runAsync(() -> {
            try {
                upsertWalletSync(walletId, ownerUuid, ownerName, balanceCents);
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error upserting wallet: " + walletId, e);
            }
        }, getExecutor());
    }

    public CompletableFuture<Void> upsertBankAccount(UUID pdaId, UUID ownerUuid, String ownerName, long balanceCents) {
        return CompletableFuture.runAsync(() -> {
            if (connection == null || pdaId == null) return;
            String sql = """
                INSERT INTO economy_bank_accounts (pda_uuid, owner_uuid, owner_name, balance_cents, last_updated)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(pda_uuid) DO UPDATE SET
                    owner_uuid = excluded.owner_uuid,
                    owner_name = excluded.owner_name,
                    balance_cents = excluded.balance_cents,
                    last_updated = CURRENT_TIMESTAMP;
            """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, pdaId.toString());
                ps.setString(2, ownerUuid != null ? ownerUuid.toString() : "");
                ps.setString(3, ownerName != null && !ownerName.isBlank() ? ownerName : "Desconhecido");
                ps.setLong(4, balanceCents);
                ps.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error upserting bank account: " + pdaId, e);
            }
        }, getExecutor());
    }

    public CompletableFuture<GlobalEconomyStats> getGlobalEconomyStats() {
        return getGlobalEconomyStats(0L);
    }

    public CompletableFuture<GlobalEconomyStats> getGlobalEconomyStats(long onlinePhysicalCashCents) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null) {
                return new GlobalEconomyStats(0, 0L, 0, 0L, onlinePhysicalCashCents, 0, 0L, onlinePhysicalCashCents, 0L, "Nenhum", 0L);
            }

            int totalWallets = 0;
            long totalWalletCents = 0L;
            long avgWalletCents = 0L;
            String topOwner = "Nenhum";
            long topBalance = 0L;

            String walletStatsSql = "SELECT COUNT(*) AS total, COALESCE(SUM(balance_cents), 0) AS sum_cents, COALESCE(AVG(balance_cents), 0) AS avg_cents FROM economy_wallets;";
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(walletStatsSql)) {
                if (rs.next()) {
                    totalWallets = rs.getInt("total");
                    totalWalletCents = rs.getLong("sum_cents");
                    avgWalletCents = rs.getLong("avg_cents");
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error querying global wallet stats", e);
            }

            int totalBankAccounts = 0;
            long totalBankCents = 0L;
            String bankStatsSql = "SELECT COUNT(*) AS total, COALESCE(SUM(balance_cents), 0) AS sum_cents FROM economy_bank_accounts;";
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(bankStatsSql)) {
                if (rs.next()) {
                    totalBankAccounts = rs.getInt("total");
                    totalBankCents = rs.getLong("sum_cents");
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error querying global bank stats", e);
            }

            String topHolderSql = """
                SELECT owner_name, SUM(balance_cents) AS total_owner_cents
                FROM (
                    SELECT owner_uuid, owner_name, balance_cents FROM economy_wallets
                    UNION ALL
                    SELECT owner_uuid, owner_name, balance_cents FROM economy_bank_accounts
                )
                WHERE owner_uuid != ''
                GROUP BY owner_uuid
                ORDER BY total_owner_cents DESC
                LIMIT 1;
            """;
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(topHolderSql)) {
                if (rs.next()) {
                    topOwner = rs.getString("owner_name");
                    topBalance = rs.getLong("total_owner_cents");
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error querying top holder", e);
            }

            int activeChecks = 0;
            long totalChecksCents = 0L;
            String checksSql = "SELECT COUNT(*) AS total_checks, COALESCE(SUM(amount_cents), 0) AS sum_checks FROM economy_checks WHERE status = 'ACTIVE';";
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(checksSql)) {
                if (rs.next()) {
                    activeChecks = rs.getInt("total_checks");
                    totalChecksCents = rs.getLong("sum_checks");
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error querying active checks stats", e);
            }

            int totalAccounts = totalWallets + totalBankAccounts;
            long avgAccountCents = totalAccounts > 0 ? (totalWalletCents + totalBankCents) / totalAccounts : avgWalletCents;
            long totalCirculating = totalWalletCents + totalBankCents + onlinePhysicalCashCents + totalChecksCents;

            return new GlobalEconomyStats(
                    totalWallets,
                    totalWalletCents,
                    totalBankAccounts,
                    totalBankCents,
                    onlinePhysicalCashCents,
                    activeChecks,
                    totalChecksCents,
                    totalCirculating,
                    avgAccountCents,
                    topOwner,
                    topBalance
            );
        }, getExecutor());
    }

    public CompletableFuture<Long> getTotalWalletsBalanceByOwner(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null || ownerUuid == null) return 0L;
            String sql = "SELECT COALESCE(SUM(balance_cents), 0) AS total FROM economy_wallets WHERE owner_uuid = ?;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, ownerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("total");
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error querying total wallets balance for owner: " + ownerUuid, e);
            }
            return 0L;
        }, getExecutor());
    }

    public CompletableFuture<Long> getTotalBankBalanceByOwner(UUID ownerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null || ownerUuid == null) return 0L;
            String sql = "SELECT COALESCE(SUM(balance_cents), 0) AS total FROM economy_bank_accounts WHERE owner_uuid = ?;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, ownerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("total");
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error querying total bank balance for owner: " + ownerUuid, e);
            }
            return 0L;
        }, getExecutor());
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                LOGGER.info("[FurWatch Economy] SQLite connection closed.");
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error closing database connection", e);
            }
        }
        if (dbExecutor != null) {
            dbExecutor.shutdown();
        }
    }

    private static CheckRecord mapCheckRecord(ResultSet rs) throws SQLException {
        String depUuid = rs.getString("deposited_by_uuid");
        return new CheckRecord(
                UUID.fromString(rs.getString("check_uuid")),
                UUID.fromString(rs.getString("issuer_uuid")),
                rs.getString("issuer_name"),
                rs.getString("payee_name"),
                rs.getLong("amount_cents"),
                rs.getString("issued_at"),
                rs.getString("status"),
                depUuid != null ? UUID.fromString(depUuid) : null,
                rs.getString("deposited_by_name"),
                rs.getString("deposited_at")
        );
    }

    private static TransactionRecord mapTransactionRecord(ResultSet rs) throws SQLException {
        String targetUuid = rs.getString("target_uuid");
        return new TransactionRecord(
                rs.getLong("id"),
                rs.getString("tx_uuid"),
                rs.getString("timestamp"),
                rs.getString("tx_type"),
                UUID.fromString(rs.getString("player_uuid")),
                rs.getString("player_name"),
                targetUuid != null ? UUID.fromString(targetUuid) : null,
                rs.getString("target_name"),
                rs.getLong("amount_cents"),
                rs.getLong("wallet_balance_after"),
                rs.getString("details")
        );
    }

    private static WalletSnapshotRecord mapSnapshotRecord(ResultSet rs) throws SQLException {
        String walletId = rs.getString("wallet_id");
        return new WalletSnapshotRecord(
                UUID.fromString(rs.getString("player_uuid")),
                rs.getString("player_name"),
                (walletId != null && !walletId.isEmpty()) ? UUID.fromString(walletId) : null,
                rs.getLong("last_balance_cents"),
                rs.getString("last_updated")
        );
    }
}
