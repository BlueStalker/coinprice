package com.stalker.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by curt on 1/3/18.
 */
public class AccountBalance {
    private Long ts;

    private List<ExchangeBalance> balances;
    @JsonProperty
    public void setTs(Long ts) {
        this.ts = ts;
    }

    @JsonProperty
    public Long getTs() {
        return ts;
    }

    @JsonProperty
    public void setBalances(List<ExchangeBalance> balances) {
        this.balances = balances;
    }

    @JsonProperty
    public List<ExchangeBalance> getBalances() {
        return balances;
    }

}
