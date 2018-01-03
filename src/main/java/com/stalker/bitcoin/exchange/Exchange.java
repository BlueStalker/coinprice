package com.stalker.bitcoin.exchange;

/**
 * Created by curt on 12/28/17.
 */
public interface Exchange {

    String getName();

    int getID();

    void start();

    void setOnPriceChangeListener(ExchangePriceChangeListener listener);
}