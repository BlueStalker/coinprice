package com.stalker.bitcoin.stragtegy;


import com.stalker.bitcoin.exchange.ExchangePriceChangeListener;

/**
 * Created by curt on 1/1/18.
 */
public interface Strategy {
    ExchangePriceChangeListener getExchangePriceChangeListener();

    void addTradeListener(TradeListener listener);
}
