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
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EconomyDatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EconomyDatabaseManager.class);
    private static EconomyDatabaseManager instance;

    private Connection connection;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "FurWatch-Economy-DB");
        thread.setDaemon(true);
        return thread;
    });

    private EconomyDatabaseManager() {}

    public static synchronized EconomyDatabaseManager getInstance() {
        if (instance == null) {
            instance = new EconomyDatabaseManager();
        }
        return instance;
    }

    public synchronized void initialize(MinecraftServer server) {
        try {
            Path dbDirectory;
            try {
                dbDirectory = server.getWorldPath(LevelResource.ROOT).resolve("furwatch");
            } catch (Exception e) {
                dbDirectory = server.getServerDirectory().resolve("furwatch");
            }

            File dirFile = dbDirectory.toFile();
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            File dbFile = dbDirectory.resolve("economy.db").toFile();
            String dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            LOGGER.info("[FurWatch Economy] Connecting to SQLite database at: {}", dbFile.getAbsolutePath());
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(dbUrl);

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

            st.execute("CREATE INDEX IF NOT EXISTS idx_tx_player ON economy_transactions(player_uuid);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_checks_issuer ON economy_checks(issuer_uuid, status);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_checks_status ON economy_checks(status);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_snapshots_name ON economy_wallet_snapshots(player_name COLLATE NOCASE);");
            st.execute("CREATE INDEX IF NOT EXISTS idx_wallets_owner ON economy_wallets(owner_uuid);");
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
        }, dbExecutor);
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
                INSERT INTO economy_checks (
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
        }, dbExecutor);
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
        }, dbExecutor);
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
        }, dbExecutor);
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
        }, dbExecutor);
    }

    public CompletableFuture<List<TransactionRecord>> getRecentTransactions(UUID playerUuid, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<TransactionRecord> list = new ArrayList<>();
            if (connection == null) return list;
            String sql = "SELECT * FROM economy_transactions WHERE player_uuid = ? ORDER BY id DESC LIMIT ?;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setInt(2, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapTransactionRecord(rs));
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error fetching transactions for: " + playerUuid, e);
            }
            return list;
        }, dbExecutor);
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
                    upsertWallet(walletId, playerUuid, playerName, balanceCents);
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error updating wallet snapshot for: " + playerUuid, e);
            }
        }, dbExecutor);
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
        }, dbExecutor);
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
            return null;
        }, dbExecutor);
    }

    public CompletableFuture<Void> upsertWallet(UUID walletId, UUID ownerUuid, String ownerName, long balanceCents) {
        return CompletableFuture.runAsync(() -> {
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
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error upserting wallet: " + walletId, e);
            }
        }, dbExecutor);
    }

    public CompletableFuture<GlobalEconomyStats> getGlobalEconomyStats() {
        return CompletableFuture.supplyAsync(() -> {
            if (connection == null) {
                return new GlobalEconomyStats(0, 0L, 0, 0L, 0L, 0L, "Nenhum", 0L);
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

            String topWalletSql = "SELECT owner_name, balance_cents FROM economy_wallets ORDER BY balance_cents DESC LIMIT 1;";
            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(topWalletSql)) {
                if (rs.next()) {
                    topOwner = rs.getString("owner_name");
                    topBalance = rs.getLong("balance_cents");
                }
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error querying top wallet", e);
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

            long totalCirculating = totalWalletCents + totalChecksCents;

            return new GlobalEconomyStats(
                    totalWallets,
                    totalWalletCents,
                    activeChecks,
                    totalChecksCents,
                    totalCirculating,
                    avgWalletCents,
                    topOwner,
                    topBalance
            );
        }, dbExecutor);
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
        }, dbExecutor);
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("[FurWatch Economy] SQLite connection closed.");
            } catch (SQLException e) {
                LOGGER.error("[FurWatch Economy] Error closing database connection", e);
            }
        }
        dbExecutor.shutdown();
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
