package com.stalker.bitcoin.stragtegy;

import com.stalker.bitcoin.event.PriceAndAmount;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.exchange.ExchangePriceChangeListener;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * Created by curt on 1/1/18.
 */
public abstract class SingleMaxMin implements Strategy {
    private PrintWriter pricePrinter;
    private static final String PRICE_FILE = "./price.csv";
    protected static final long WINDOW = 5 * 60 * 1000L;

    protected Map<Integer, PriceAndAmount> maxBuy;
    protected Map<Integer, PriceAndAmount> minSell;

    protected Map<Integer, Exchange> exchanges;
    protected int N;
    private final boolean isSimulation;
    protected boolean beginTrade;

    protected List<TradeListener> tradeListeners;

    protected void logPrice(long ts, int buyExchange, double maxBuyPrice, int sellExchange, double minSellPrice) {
        if (!isSimulation) {
            pricePrinter.println(ts + "," +
                    buyExchange + "," + maxBuyPrice + "," + minSell.get(buyExchange).price +
                    ","+ sellExchange + "," + maxBuy.get(sellExchange).price + "," + minSellPrice);
        }
    }


    public SingleMaxMin(Map<Integer, Exchange> exchanges, boolean isSimulation) {
        this.isSimulation = isSimulation;
        try {
            TimerTask timerTask = new TimerTask() {
                public void run() {
                    beginTrade = true;
                }
            };
            Timer timer = new Timer();
            timer.schedule(timerTask, 1000l * 60);
            this.exchanges = exchanges;
            N = exchanges.size();
            maxBuy = new HashMap<>();
            minSell = new HashMap<>();
            this.tradeListeners = new ArrayList<>();
            pricePrinter = new PrintWriter(new FileWriter(PRICE_FILE, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public SingleMaxMin(Map<Integer, Exchange> exchanges) {
        this(exchanges, false);
    }

    public void addTradeListener(TradeListener listener) {
        tradeListeners.add(listener);
    }
    // The compute happened when Single Max/Min value has been changed
    protected abstract void compute(long ts,
                                    int buyExchange,
                                    PriceAndAmount maxBuy,
                                    int sellExchange,
                                    PriceAndAmount minSell);

    public ExchangePriceChangeListener getExchangePriceChangeListener() {
        return priceChangeListener;
    }

    private ExchangePriceChangeListener priceChangeListener = new ExchangePriceChangeListener () {
        // I assume the first Entry is always what we should be cared about
        // EG. Buy is descending order and sell is ascending order.
        public void change(long ts, int id, boolean buy, TreeMap<Double, Double> prices) {
            Map.Entry<Double, Double> entry = prices.firstEntry();
            double price = entry.getKey();
            double amount = entry.getValue();
            // This shall be running in different threads.
            synchronized (this) {
                if (buy) {
                    PriceAndAmount existing = maxBuy.get(id);
                    if (existing != null && existing.price == price) return;
                    maxBuy.put(id, new PriceAndAmount(price, amount));
                } else {
                    PriceAndAmount existing = minSell.get(id);
                    if (existing != null && existing.price == price) return;
                    minSell.put(id, new PriceAndAmount(price, amount));
                }

                for (int i : exchanges.keySet()) {
                    if (i != id) {
                        if (buy && minSell.containsKey(i)) {
                            compute(ts, id, new PriceAndAmount(price, amount), i, minSell.get(i));
                        } else if (!buy && maxBuy.containsKey(i)) {
                            compute(ts, i, maxBuy.get(i), id, new PriceAndAmount(price, amount));
                        }
                    }
                }
            }
        }
    };

    public void finalize() {
        if (pricePrinter != null) {
            pricePrinter.close();
        }
    }
}
