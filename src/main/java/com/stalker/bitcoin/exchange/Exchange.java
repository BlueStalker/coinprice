package com.stalker.bitcoin.exchange;

import java.util.TreeMap;

/**
 * Created by curt on 12/28/17.
 */
public interface Exchange {

    String getName();

    int getID();

    void start();

    void setOnPriceChangeListener(ExchangePriceChangeListener listener);

}
