package com.stalker.bitcoin.trade;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.stalker.bitcoin.model.PriceAndAmount;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.exchange.ExchangeEnum;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import com.stalker.bitcoin.http.config.SimulationConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by curt on 1/2/18.
 */
@Singleton
public class SimulationTradeManagement extends TradeManagement {
    @Inject
    public SimulationTradeManagement(
            @Named("allExchanges")Map<Integer, Exchange> exchanges,
            CoinPriceConfiguration config) {
        super(exchanges, config);
        this.cash = new HashMap<>();
        this.coins = new HashMap<>();
        for (SimulationConfiguration.ExchangeSimulationConfiguration ec :config.getSimulationExchanges()) {
            cash.put(ExchangeEnum.fromName(ec.getName()).getId(), ec.getCash());
            coins.put(ExchangeEnum.fromName(ec.getName()).getId(), ec.getCoin());
        }
    }

    void doTrade(long ts, int buyExchange, int sellExchange,
                 double tradeAmount, PriceAndAmount maxBuy, PriceAndAmount minSell) {

        coins.put(buyExchange, coins.get(buyExchange) - tradeAmount);
        coins.put(sellExchange, coins.get(sellExchange) + tradeAmount);
        cash.put(buyExchange, cash.get(buyExchange) + tradeAmount * maxBuy.price);
        cash.put(sellExchange, cash.get(sellExchange) - tradeAmount * minSell.price);
        try {
            orderPrinter.write(ts + "," + buyExchange + "," + cash.get(buyExchange) + "," + coins.get(buyExchange) + ",buy\n");
            orderPrinter.write(ts + "," + sellExchange + "," + cash.get(sellExchange) + "," + coins.get(sellExchange) + ",sell\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        debugBalance();
        System.out.println();
    }
}
