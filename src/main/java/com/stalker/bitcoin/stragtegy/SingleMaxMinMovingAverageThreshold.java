package com.stalker.bitcoin.stragtegy;

import com.stalker.bitcoin.model.PriceAndAmount;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import com.stalker.bitcoin.trade.TradeManagement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by curt on 12/31/17.
 */
public class SingleMaxMinMovingAverageThreshold extends SingleMaxMin {

    class Distance {
        public long ts;
        public double value;

        public Distance(long ts, double value) {
            this.ts = ts;
            this.value = value;
        }
    }

    private Map<Integer, Queue<Distance>> distances;
    private Map<Integer, Double> movingSum;
    private final double threshold;

    public SingleMaxMinMovingAverageThreshold(
            Map<Integer, Exchange> exchanges,
            CoinPriceConfiguration config,
            TradeManagement tradeManagement) {
        super(exchanges, config, tradeManagement);
        this.threshold = config.getStrategy().getThreshold();
        movingSum = new HashMap<>(N * (N - 1));
        distances = new HashMap<>(N * (N - 1));
        for (int i : exchanges.keySet()) {
            for (int j : exchanges.keySet()) {
                if (i != j) {
                    distances.put(i * 10 + j, new LinkedList<>());
                    movingSum.put(i * 10 + j, 0.0d);
                }
            }
        }
    }

    public void compute(long ts, int buyExchange, PriceAndAmount maxBuy,
                        int sellExchange, PriceAndAmount minSell) {

        logModelingPrice(ts, buyExchange, maxBuy.price, sellExchange, minSell.price);
        int key = buyExchange * 10 + sellExchange;
        Queue<Distance> q = distances.get(key);
        double sum = movingSum.get(key);
        while (!q.isEmpty()) {
            Distance d = q.peek();
            if (ts - d.ts > WINDOW) {
                sum -= q.poll().value;
            } else {
                break;
            }
        }

        double value = maxBuy.price - minSell.price;
        Distance distance = new Distance(ts, value);
        sum += value;
        q.offer(distance);
        movingSum.put(key, sum);
        double movingAverage = sum / q.size();
        // Current the percent calculation is based on the maxBuy.price
        // probably need to be changed;
        double percent = (value - movingAverage) * 1000.0d / maxBuy.price;
        if (percent > threshold && beginTrade) {
            tradeManagement.onTrade(ts, buyExchange, sellExchange, maxBuy, minSell);
        }
    }
}
