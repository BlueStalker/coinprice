package com.stalker.bitcoin.event;

import com.google.common.collect.ImmutableMap;
import com.stalker.bitcoin.exchange.CoinCheck;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.exchange.Quoine;
import com.stalker.bitcoin.stragtegy.SingleMaxMinMovingAverageThreshold;
import com.stalker.bitcoin.stragtegy.Strategy;
import com.stalker.bitcoin.trade.TradeManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by curt on 12/26/17.
 */
public class Calculation {

    private static final Logger LOG = LoggerFactory.getLogger("coinprice");
    private static final String ORDER_FILE = "./order.csv";

    private Map<Integer, Exchange> exchanges;
    private Strategy strategy;

    public Calculation() throws Exception {
        Exchange coinCheck = new CoinCheck(1);
        Exchange quoine = new Quoine(2);
        //Exchange bitFlyer = new Bitflyer(3);
        exchanges = ImmutableMap.of(1, coinCheck, 2, quoine);
        strategy = new SingleMaxMinMovingAverageThreshold(exchanges, 1.5d, false);
        coinCheck.setOnPriceChangeListener(strategy.getExchangePriceChangeListener());
        quoine.setOnPriceChangeListener(strategy.getExchangePriceChangeListener());
        //bitFlyer.setOnPriceChangeListener(strategy);

        // Fake cash value to be 10k for every exchange
        // Fake coins value to be 1 for every exchange.
        Map<Integer, Double> cash = new HashMap<>();
        Map<Integer, Double> coins = new HashMap<>();
        for (int i : exchanges.keySet()) {
            cash.put(i, 10000d);
            coins.put(i, 0.0064516);
        }

        TradeManagement tradeManagement = new TradeManagement(cash, coins, true);
        strategy.addTradeListener(tradeManagement.getTradeListener());
    }

    public void process() throws Exception {
        Object object = new Object();

        for (Exchange e : exchanges.values()) {
            e.start();
        }

        LOG.info("System start, Begin object waiting");
        synchronized (object) {
            object.wait();
        }
    }

    public static void main(String[] argv) throws Exception {
        Calculation pc = new Calculation();
        pc.process();
    }
}
