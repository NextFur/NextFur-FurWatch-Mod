package net.nextfur.fwc.economy.db.records;

import java.util.UUID;

public record TransactionRecord(
        long id,
        String txUuid,
        String timestamp,
        String txType,
        UUID playerUuid,
        String playerName,
        UUID targetUuid,
        String targetName,
        long amountCents,
        long balanceAfter,
        String details
) {}
