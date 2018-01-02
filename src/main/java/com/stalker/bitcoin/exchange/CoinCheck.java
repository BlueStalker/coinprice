package com.stalker.bitcoin.exchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by curt on 12/28/17.
 */
public class CoinCheck extends WebSocketExchange {

    private static final String WSS = "wss://ws-api.coincheck.com/";
    private static final String ENDPOINT = "{\"type\":\"subscribe\",\"channel\":\"btc_jpy-orderbook\"}";

    ObjectMapper mapper = new ObjectMapper();
    public CoinCheck(int id) {
        super(id, "coincheck", WSS, ENDPOINT);
    }

    @Override
    public void onSocketText(String s) {
        try {
            long start = System.nanoTime();
            JsonNode node = mapper.readTree(s);
            synchronized (this) {
                processOrder(node.get(1).get("bids"), buys);
                processOrder(node.get(1).get("asks"), sells);
                //debugOrderBook();
            }
            if (listener != null) {
                long ts = System.currentTimeMillis();
                if (!buys.isEmpty()) listener.change(ts, id, true, buys);
                if (!sells.isEmpty()) listener.change(ts, id, false, sells);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOrder(JsonNode delta, TreeMap<Double, Double> existing) {
        for (int i = 0; i < delta.size(); i++) {
            JsonNode node = delta.get(i);
            double price = node.get(0).asDouble();
            double amount = node.get(1).asDouble();
            if (amount == 0) existing.remove(price);
            else {
                existing.put(price, amount);
                if (existing.size() > COUNT) {
                    existing.remove(existing.lastKey());
                }
            }
        }
    }
}
