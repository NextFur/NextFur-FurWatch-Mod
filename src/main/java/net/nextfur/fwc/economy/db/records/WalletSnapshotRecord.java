package net.nextfur.fwc.economy.db.records;

import java.util.UUID;

public record WalletSnapshotRecord(
        UUID playerUuid,
        String playerName,
        UUID walletId,
        long lastBalanceCents,
        String lastUpdated
) {}
