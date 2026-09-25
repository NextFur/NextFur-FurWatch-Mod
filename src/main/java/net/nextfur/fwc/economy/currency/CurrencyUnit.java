package net.nextfur.fwc.economy.currency;

import net.nextfur.fwc.economy.data.EconomyFormatHelper;

public enum CurrencyUnit {
    COIN_1C("coin_1c", 1L, CurrencyType.COIN, "1c$M"),
    COIN_5C("coin_5c", 5L, CurrencyType.COIN, "5c$M"),
    COIN_20C("coin_20c", 20L, CurrencyType.COIN, "20c$M"),
    COIN_25C("coin_25c", 25L, CurrencyType.COIN, "25c$M"),
    COIN_50C("coin_50c", 50L, CurrencyType.COIN, "50c$M"),
    COIN_1M("coin_1m", 100L, CurrencyType.COIN, "1$M"),
    COIN_2M("coin_2m", 200L, CurrencyType.COIN, "2$M"),
    BILL_5M("bill_5m", 500L, CurrencyType.BILL, "5$M"),
    BILL_10M("bill_10m", 1000L, CurrencyType.BILL, "10$M"),
    BILL_20M("bill_20m", 2000L, CurrencyType.BILL, "20$M"),
    BILL_50M("bill_50m", 5000L, CurrencyType.BILL, "50$M"),
    BILL_100M("bill_100m", 10000L, CurrencyType.BILL, "100$M"),
    BILL_200M("bill_200m", 20000L, CurrencyType.BILL, "200$M");

    private final String id;
    private final long valueInCents;
    private final CurrencyType type;
    private final String label;

    CurrencyUnit(String id, long valueInCents, CurrencyType type, String label) {
        this.id = id;
        this.valueInCents = valueInCents;
        this.type = type;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public long getValueInCents() {
        return valueInCents;
    }

    public CurrencyType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getFormattedValue() {
        return EconomyFormatHelper.formatStandard(valueInCents);
    }

    public enum CurrencyType {
        COIN,
        BILL
    }
}
