package com.stalker.bitcoin.stragtegy;

import com.stalker.bitcoin.model.PriceAndAmount;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.exchange.ExchangePriceChangeListener;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import com.stalker.bitcoin.trade.TradeManagement;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
 * Created by curt on 1/1/18.
 */
public abstract class SingleMaxMin implements Strategy {
    private PrintWriter pricePrinter;
    private ObjectOutputStream rawPricePrinter;
    private static final String PRICE_FILE = "./price.csv";
    private static final String RAW_PRICE_FILE = "./raw_price.log";
    protected static final long WINDOW = 5 * 60 * 1000L;

    protected Map<Integer, PriceAndAmount> maxBuy;
    protected Map<Integer, PriceAndAmount> minSell;

    protected final Map<Integer, Exchange> exchanges;
    protected int N;
    protected boolean beginTrade;

    protected CoinPriceConfiguration config;

    protected TradeManagement tradeManagement;

    protected void logModelingPrice(long ts, int buyExchange, double maxBuyPrice, int sellExchange, double minSellPrice) {
        //if (!config.getSimulation()) {
        pricePrinter.println(ts + "," +
                buyExchange + "," + maxBuyPrice + "," + minSell.get(buyExchange).price +
                "," + sellExchange + "," + maxBuy.get(sellExchange).price + "," + minSellPrice);
        //}
    }

    private void logRawPrices(long ts, int id, boolean buy, TreeMap<Double, Double> prices) {
        try {
            //if (!config.getSimulation()) {
            rawPricePrinter.writeLong(ts);
            rawPricePrinter.writeInt(id);
            rawPricePrinter.writeBoolean(buy);
            rawPricePrinter.writeObject(prices);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public SingleMaxMin(Map<Integer, Exchange> exchanges,
                        CoinPriceConfiguration config,
                        TradeManagement tradeManagement) {
        this.config = config;
        this.exchanges = exchanges;
        this.tradeManagement = tradeManagement;
        try {
            TimerTask timerTask = new TimerTask() {
                public void run() {
                    beginTrade = true;
                }
            };
            Timer timer = new Timer();
            timer.schedule(timerTask, 1000l * 60);
            N = exchanges.size();
            maxBuy = new HashMap<>();
            minSell = new HashMap<>();
            pricePrinter = new PrintWriter(new FileWriter(PRICE_FILE, true));
            rawPricePrinter = new ObjectOutputStream(new FileOutputStream(RAW_PRICE_FILE, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private ExchangePriceChangeListener priceChangeListener = new ExchangePriceChangeListener() {
        // I assume the first Entry is always what we should be cared about
        // EG. Buy is descending order and sell is ascending order.
        public void change(long ts, int id, boolean buy, TreeMap<Double, Double> prices) {
            logRawPrices(ts, id, buy, prices);
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
