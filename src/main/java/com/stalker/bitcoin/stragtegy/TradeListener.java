package com.stalker.bitcoin.stragtegy;

import com.stalker.bitcoin.event.PriceAndAmount;

/**
 * Created by curt on 1/2/18.
 */
public interface TradeListener {
    void onTrade(long ts, int buyExchange, int sellExchange,
                 PriceAndAmount maxBuy, PriceAndAmount minSell);
}
