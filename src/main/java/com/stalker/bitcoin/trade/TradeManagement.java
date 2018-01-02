package com.stalker.bitcoin.trade;

import com.stalker.bitcoin.event.PriceAndAmount;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.stragtegy.TradeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by curt on 1/1/18.
 */
public class TradeManagement {
    private static final Logger LOG = LoggerFactory.getLogger("coinprice");

    private Map<Integer, Exchange> exchanges;
    private static final String ORDER_FILE = "./order.csv";

    private FileWriter orderPrinter;

    private Map<Integer, Double> cash;
    private Map<Integer, Double> coins;

    private final boolean isSimulation;

    // This constructor should be called by production
    // and we need to use Exchange API to update balance info here.
    public TradeManagement() {
        this(new HashMap<>(), new HashMap<>(), false);
    }

    // This should always be called by simulation
    public TradeManagement(Map<Integer, Double> cash, Map<Integer, Double> coins, boolean isSimulation) {
        this.isSimulation = isSimulation;
        try {
            this.cash = cash;
            this.coins = coins;
            this.orderPrinter = new FileWriter(ORDER_FILE, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TradeListener getTradeListener() {
        return tradeListener;
    }

    private TradeListener tradeListener = new TradeListener() {
        @Override
        public void onTrade(long ts, int buyExchange, int sellExchange, PriceAndAmount maxBuy, PriceAndAmount minSell) {
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
            double buyCoins = availableCash / minSell.price;

            double maxExternalAmount = Math.min(maxBuy.amount, minSell.amount);
            double tradeAmount = Math.min(Math.min(sellCoins, buyCoins), maxExternalAmount);

            if (tradeAmount == 0.0d) return;
            LOG.info(" Trading amount " + tradeAmount + " Sell " + maxBuy.price
                    + " Buy " + minSell.price
                    + " Latency " + (System.currentTimeMillis() - ts));
            if (isSimulation) {
                simulateTrade(ts, buyExchange, sellExchange, tradeAmount, maxBuy, minSell);
            }
        }
    };


    private void simulateTrade(long ts, int buyExchange, int sellExchange,
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


    private void debugBalance() {
        for (int i : exchanges.keySet()) {
            LOG.info(" EXCHANGE:: " + i + " CASH: " + cash.get(i) + " COINS: " + coins.get(i));
        }
    }

    public void finalize() {
        try {
            if (orderPrinter != null) {
                orderPrinter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
