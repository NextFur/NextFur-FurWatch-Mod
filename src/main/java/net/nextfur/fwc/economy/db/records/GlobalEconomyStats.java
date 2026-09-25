package net.nextfur.fwc.economy.db.records;

public record GlobalEconomyStats(
        int totalWallets,
        long totalWalletCents,
        int activeChecksCount,
        long totalActiveChecksCents,
        long totalCirculatingCents,
        long averageWalletCents,
        String topWalletOwner,
        long topWalletBalanceCents
) {}
