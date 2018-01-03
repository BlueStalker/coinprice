package com.stalker.bitcoin.stragtegy;

/**
 * Created by curt on 1/2/18.
 */
public enum StrategyEnum {
    SingleMaxMinMovingAverageThreshold("singleMovingAverage");

    private String name;

    StrategyEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static StrategyEnum fromName(String name) {
        if (name.equals(SingleMaxMinMovingAverageThreshold.name)) return SingleMaxMinMovingAverageThreshold;
        throw new RuntimeException("fuck you");
    }
}
