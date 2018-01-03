package com.stalker.bitcoin.trade;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.stalker.bitcoin.model.PriceAndAmount;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by curt on 1/2/18.
 */
@Singleton
public class DefaultTradeManagement extends TradeManagement {
    @Inject
    public DefaultTradeManagement(
            @Named("allExchanges")Map<Integer, Exchange> exchanges,
            CoinPriceConfiguration config) {
        super(exchanges, config);
        this.cash = new HashMap<>();
        this.coins = new HashMap<>();
    }

    void doTrade(long ts, int buyExchange, int sellExchange,
                 double tradeAmount, PriceAndAmount maxBuy, PriceAndAmount minSell) {

        LOG.info("Do a real trade");
    }
}
