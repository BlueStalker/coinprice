package com.stalker.bitcoin.event;

import com.google.common.collect.ImmutableMap;
import com.stalker.bitcoin.exchange.Bitflyer;
import com.stalker.bitcoin.exchange.CoinCheck;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.exchange.ExchangePriceChangeListener;
import com.stalker.bitcoin.exchange.Quoine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by curt on 12/26/17.
 */
public class Calculation implements ExchangePriceChangeListener {

    private static final long WINDOW = 5 * 60 * 1000L;
    class Distance {
        public long ts;
        public double value;
        public Distance(long ts, double value) {
            this.ts = ts;
            this.value = value;
        }
    }
    private Map<Integer, Exchange> exchanges;
    private int N;
    // Will be a two digit Integer as KEY
    private Map<Integer, Double> movingTotal;
    private Map<Integer, Double> maxBuy;
    private Map<Integer, Double> minSell;
    private Map<Integer, Queue<Distance>> distances;

    public void change(int id, boolean buy, double price, double amount) {
        synchronized (this) {
            long ts = System.currentTimeMillis();
            if (buy) {
                Double old = maxBuy.put(id, price);
                if (old != null && old == price) return;
            } else {
                Double old = minSell.put(id, price);
                if (old != null && old == price) return;
            }
            //System.out.println(id + " " + (buy ? "buy" : "sell"));
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
                    double value = buy ? (price - minSell.getOrDefault(i, price)) : (maxBuy.getOrDefault(i, price) - price);

                    Distance distance = new Distance(ts, value);
                    total += value;
                    q.offer(distance);
                    movingTotal.put(key, total);
//                    System.out.println("###### max buy ######");
//                    System.out.println(maxBuy.getOrDefault(2, 0d) + " " +maxBuy.getOrDefault(3, 0d));
//                    System.out.println("###### min sell ######");
//                    System.out.println(minSell.getOrDefault(2, 0d) + " " + minSell.getOrDefault(3, 0d));
//                    System.out.println("#######total#######");
//                    System.out.println(movingTotal.get(23) + " " + movingTotal.get(32));
 //                   System.out.println("#######size#######");
 //                   System.out.println(distances.get(23).size() + " " + distances.get(32).size());
                    System.out.println(key + "," + (total / q.size()));
                }
            }
        }
    }

    public Calculation() {
        Exchange coinCheck = new CoinCheck(1);
        coinCheck.setOnPriceChangeListener(this);
        Exchange quoine = new Quoine(2);
        quoine.setOnPriceChangeListener(this);
        Exchange bitFlyer = new Bitflyer(3);
        bitFlyer.setOnPriceChangeListener(this);
        exchanges = ImmutableMap.of(1, coinCheck, 2, quoine, 3, bitFlyer);
        N = exchanges.size();
        movingTotal = new HashMap<>(N * (N-1));
        distances = new HashMap<>(N * (N-1));
        maxBuy = new HashMap<>(N * (N-1));
        minSell = new HashMap<>(N * (N-1));
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

        for (Exchange e: exchanges.values()) {
            e.start();
        }

        synchronized (object) {
            object.wait();
        }
    }
    public static void main(String[] argv) throws Exception {
        Calculation pc = new Calculation();
        pc.process();
    }
}
