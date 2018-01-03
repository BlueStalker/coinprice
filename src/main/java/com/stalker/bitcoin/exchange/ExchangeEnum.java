package com.stalker.bitcoin.exchange;

/**
 * Created by curt on 1/2/18.
 */
public enum ExchangeEnum {
    COINCHECK(1, "coincheck"),

    QUOINE(2, "quoine"),

    BITFLYER(3, "bitflyer");

    private int id;
    private String name;

    ExchangeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static ExchangeEnum fromId(int id) {
        if (id == 1) return COINCHECK;
        else if (id == 2) return QUOINE;
        else if (id == 3) return BITFLYER;
        throw new RuntimeException("fuck you");
    }

    public static ExchangeEnum fromName(String name) {
        if (name.equals(COINCHECK.name)) return COINCHECK;
        else if (name.equals(QUOINE.name)) return QUOINE;
        else if (name.equals(BITFLYER.name)) return BITFLYER;
        throw new RuntimeException("fuck you");
    }
}
