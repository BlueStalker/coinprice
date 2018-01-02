package com.stalker.bitcoin.exchange;

import java.util.TreeMap;

/**
 * Created by curt on 1/2/18.
 */
public interface ExchangePriceChangeListener {
    void change(long ts, int id, boolean buy, TreeMap<Double, Double> prices);
}
