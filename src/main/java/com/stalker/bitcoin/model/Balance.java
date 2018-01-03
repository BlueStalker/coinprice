package com.stalker.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by curt on 1/2/18.
 */
public class Balance {
    private Double cash;
    private Double coin;

    @JsonProperty
    public void setCash(Double cash) {
        this.cash = cash;
    }

    @JsonProperty
    public Double getCash() {
        return cash;
    }

    @JsonProperty
    public Double getCoin() {
        return coin;
    }

    @JsonProperty
    public void setCoin(Double coin) {
        this.coin = coin;
    }
}
