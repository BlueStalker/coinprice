package com.stalker.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by curt on 1/3/18.
 */
public class ExchangeBalance {
    private String exchange;

    private Balance balance;

    @JsonProperty
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @JsonProperty
    public String getExchange() {
        return exchange;
    }

    @JsonProperty
    public void setBalance(Balance balance) {
        this.balance = balance;
    }

    @JsonProperty
    public Balance getBalance() {
        return balance;
    }
}
