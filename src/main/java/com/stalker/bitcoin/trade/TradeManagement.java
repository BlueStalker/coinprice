package com.stalker.bitcoin.trade;

import com.stalker.bitcoin.model.AccountBalance;
import com.stalker.bitcoin.model.Balance;
import com.stalker.bitcoin.model.ExchangeBalance;
import com.stalker.bitcoin.model.PriceAndAmount;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by curt on 1/1/18.
 */
public abstract class TradeManagement {
    protected static final Logger LOG = LoggerFactory.getLogger("coinprice");

    private final Map<Integer, Exchange> exchanges;
    private static final String ORDER_FILE = "./order.csv";

    protected FileWriter orderPrinter;

    protected Map<Integer, Double> cash;
    protected Map<Integer, Double> coins;

    private List<AccountBalance> balancesHistory;

    private final CoinPriceConfiguration config;

    public TradeManagement(
            Map<Integer, Exchange> exchanges,
            CoinPriceConfiguration config) {
        this.config = config;
        this.exchanges = exchanges;
        this.balancesHistory = new ArrayList<>();
        try {
            this.orderPrinter = new FileWriter(ORDER_FILE, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized AccountBalance getLatestBalance() {
        return balancesHistory.get(balancesHistory.size() - 1);
    }

    public synchronized List<AccountBalance> getAccountBalanceHistory() {
        return balancesHistory;
    }

    public synchronized void onTrade(long ts, int buyExchange, int sellExchange, PriceAndAmount maxBuy, PriceAndAmount minSell) {
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
        doTrade(ts, buyExchange, sellExchange, tradeAmount, maxBuy, minSell);
        updateAccountBalanceHistory(ts);
    }

    protected void updateAccountBalanceHistory(long ts) {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setTs(ts);
        List<ExchangeBalance> exchangeBalances = new ArrayList<>();
        for (int id: exchanges.keySet()) {
            ExchangeBalance eb = new ExchangeBalance();
            Balance balance = new Balance();
            balance.setCash(cash.get(id));
            balance.setCoin(coins.get(id));
            eb.setExchange(exchanges.get(id).getName());
            eb.setBalance(balance);
            exchangeBalances.add(eb);
        }
        accountBalance.setBalances(exchangeBalances);
        balancesHistory.add(accountBalance);
    }

    abstract void doTrade(long ts, int buyExchange, int sellExchange,
                          double tradeAmount, PriceAndAmount maxBuy, PriceAndAmount minSell);


    protected void debugBalance() {
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
