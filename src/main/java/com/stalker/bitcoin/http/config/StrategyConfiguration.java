package com.stalker.bitcoin.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by curt on 1/2/18.
 */

public class StrategyConfiguration {
    private String name;

    private Double threshold;

    @JsonProperty
    public Double getThreshold() {
        return threshold;
    }

    @JsonProperty
    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }
}
