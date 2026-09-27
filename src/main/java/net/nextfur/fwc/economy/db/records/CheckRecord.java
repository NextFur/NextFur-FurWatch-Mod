package net.nextfur.fwc.economy.db.records;

import java.util.UUID;

public record CheckRecord(
        UUID checkId,
        UUID issuerUuid,
        String issuerName,
        String payee,
        long amountCents,
        String issuedAt,
        String status,
        UUID depositedByUuid,
        String depositedByName,
        String depositedAt
) {}
