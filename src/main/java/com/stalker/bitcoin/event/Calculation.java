package com.stalker.bitcoin.event;

import com.google.common.collect.ImmutableMap;
import com.stalker.bitcoin.exchange.CoinCheck;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.exchange.ExchangePriceChangeListener;
import com.stalker.bitcoin.exchange.Quoine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by curt on 12/26/17.
 */
public class Calculation implements ExchangePriceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger("coinprice");
    private static final String PRICE_FILE = "./price.csv";
    private static final String ORDER_FILE = "./order.csv";

    private PrintWriter pricePrinter;
    private PrintWriter orderPrinter;

    private static final long WINDOW = 5 * 60 * 1000L;

    private boolean beginTrade = false;
    class Distance {
        public long ts;
        public double value;

        public Distance(long ts, double value) {
            this.ts = ts;
            this.value = value;
        }
    }

    class PriceAndAmount {
        public double price;
        public double amount;

        public PriceAndAmount(double price, double amount) {
            this.price = price;
            this.amount = amount;
        }
    }

    private Map<Integer, Exchange> exchanges;
    private int N;
    // Will be a two digit Integer as KEY
    private Map<Integer, Double> movingTotal;
    private Map<Integer, PriceAndAmount> maxBuy;
    private Map<Integer, PriceAndAmount> minSell;
    private Map<Integer, Double> cash;
    private Map<Integer, Double> coins;
    private Map<Integer, Queue<Distance>> distances;

    public void change(int id, boolean buy, double price, double amount) {
        synchronized (this) {
            try {
                long ts = System.currentTimeMillis();
                if (buy) {
                    PriceAndAmount existing = maxBuy.get(id);
                    if (existing != null && existing.price == price && existing.amount == amount) return;
                    maxBuy.put(id, new PriceAndAmount(price, amount));
                } else {
                    PriceAndAmount existing = minSell.get(id);
                    if (existing != null && existing.price == price && existing.amount == amount) return;
                    minSell.put(id, new PriceAndAmount(price, amount));
                }
                for (int i : exchanges.keySet()) {
                    if (i != id) {
                        int key = buy ? (id * 10 + i) : (i * 10 + id);
                        Queue<Distance> q = distances.get(key);
                        double total = movingTotal.get(key);
                        while (!q.isEmpty()) {
                            Distance d = q.peek();
                            if (ts - d.ts > WINDOW) {
                                total -= q.poll().value;
                            } else {
                                break;
                            }
                        }

                        double value;
                        if (buy) {
                            value = price - (minSell.containsKey(i) ? minSell.get(i).price : price);
                            if (minSell.get(i) != null)
                                pricePrinter.println(ts + "," + id + "," + price + "," + i + "," + minSell.get(i).price);
                        } else {
                            value = (maxBuy.containsKey(i) ? maxBuy.get(i).price : price) - price;
                            if (maxBuy.get(i) != null)
                                pricePrinter.println(ts + "," + i + "," + maxBuy.get(i).price + "," + id + "," + price);
                        }

                        Distance distance = new Distance(ts, value);
                        total += value;
                        q.offer(distance);
                        movingTotal.put(key, total);
                        double movingAverage = total / q.size();
                        double percent = (value - movingAverage) * 1000.0d / price;
                        //LOG.info(key + "," + String.format("%.3f", percent) + "," + String.format("%.3f", value) + "," + String.format("%.3f", movingAverage));
                        if (percent > 1.5d && beginTrade) trade(key, ts);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void trade(int key, long ts) throws IOException {
        int buyExchange = key / 10;
        int sellExchange = key % 10;
        // We want to sell on "buyExchange" and buy on "sellExchange"
        double sellCoins = coins.get(buyExchange);
        if (sellCoins == 0d) {
            LOG.info("no coins to sell exchange : " + buyExchange);
            return;
        }
        double availableCash = cash.get(sellExchange);
        if (availableCash == 0d) {
            LOG.info("no cash to buy exchange : " + sellExchange);
            return;
        }

        // The max coins we can buy on sell exchange;
        double buyCoins = availableCash / minSell.get(sellExchange).price;
        // Send orders, only this amount is available on the market
        double sellMaxAmount = maxBuy.containsKey(buyExchange) ? maxBuy.get(buyExchange).amount : 0d;
        double buyMaxAmount = minSell.containsKey(sellExchange) ? minSell.get(sellExchange).amount : 0d;
        double tradeAmount = Math.min(Math.min(sellCoins, buyCoins), Math.min(sellMaxAmount, buyMaxAmount));

        if (tradeAmount == 0.0d) return;
        LOG.info(" Trading amount " + tradeAmount + " Buy " + maxBuy.get(buyExchange).price
                + " Sell " + minSell.get(sellExchange).price);
        // Fake trading
        coins.put(buyExchange, coins.get(buyExchange) - tradeAmount);
        coins.put(sellExchange, coins.get(sellExchange) + tradeAmount);
        cash.put(buyExchange, cash.get(buyExchange) + tradeAmount * maxBuy.get(buyExchange).price);
        cash.put(sellExchange, cash.get(sellExchange) - tradeAmount * minSell.get(sellExchange).price);
        orderPrinter.println(ts +"," + buyExchange + "," + cash.get(buyExchange) + "," + coins.get(buyExchange) + ",buy");
        orderPrinter.println(ts +"," + sellExchange + "," + cash.get(sellExchange) + "," + coins.get(sellExchange) + ",sell");
        debugBalance();
        System.out.println();
    }

    private void debugBalance() {
        for (int i : exchanges.keySet()) {
            LOG.info(" EXCHANGE:: " + i + " CASH: " + cash.get(i) + " COIN CASH: " + coins.get(i) * maxBuy.get(i).price);
        }
    }

    public Calculation() throws Exception {

        TimerTask timerTask = new TimerTask() {
            public void run() {
                beginTrade = true;
            }
        };
        pricePrinter = new PrintWriter(new FileWriter(PRICE_FILE,true));
        orderPrinter = new PrintWriter(new FileWriter(ORDER_FILE,true));

        Timer timer = new Timer();
        timer.schedule(timerTask, 1000l * 60);
        Exchange coinCheck = new CoinCheck(1);
        coinCheck.setOnPriceChangeListener(this);
        Exchange quoine = new Quoine(2);
        quoine.setOnPriceChangeListener(this);
        //Exchange bitFlyer = new Bitflyer(3);
        //bitFlyer.setOnPriceChangeListener(this);
        exchanges = ImmutableMap.of(1, coinCheck, 2, quoine);
        N = exchanges.size();
        // Fake cash value to be 10k for every exchange
        // Fake coins value to be 1 for every exchange.
        cash = new HashMap<>();
        coins = new HashMap<>();
        for (int i : exchanges.keySet()) {
            cash.put(i, 10000d);
            coins.put(i, 0.0064516);
        }
        movingTotal = new HashMap<>(N * (N - 1));
        distances = new HashMap<>(N * (N - 1));
        maxBuy = new HashMap<>(N * (N - 1));
        minSell = new HashMap<>(N * (N - 1));
        for (int i : exchanges.keySet()) {
            for (int j : exchanges.keySet()) {
                if (i != j) {
                    distances.put(i * 10 + j, new LinkedList<>());
                    movingTotal.put(i * 10 + j, 0.0d);
                }
            }
        }
    }

    public void process() throws Exception {
        Object object = new Object();

        for (Exchange e : exchanges.values()) {
            e.start();
        }

        synchronized (object) {
            object.wait();
        }
        pricePrinter.close();
        orderPrinter.close();
    }

    public static void main(String[] argv) throws Exception {
        Calculation pc = new Calculation();
        pc.process();
    }
}
