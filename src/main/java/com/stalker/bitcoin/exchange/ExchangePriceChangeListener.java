package com.stalker.bitcoin.exchange;

/**
 * Created by curt on 12/29/17.
 */
public interface ExchangePriceChangeListener {
    void change(int id, boolean buy, double price, double amount);
}
