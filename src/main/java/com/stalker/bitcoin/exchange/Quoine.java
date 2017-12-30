package com.stalker.bitcoin.exchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import java.util.TreeMap;

/**
 * Created by curt on 12/28/17.
 */
public class Quoine extends AbstractExchange {

    private static final String APIKEY = "2ff981bb060680b5ce97";
    private Channel buyChannel;
    private Channel sellChannel;
    private Pusher pusher;
    ObjectMapper mapper;
    @Override
    public String getName() {
        return "quoine";
    }

    public Quoine(int id) {
        super(id);
        pusher = new Pusher(APIKEY);
        pusher.connect();
        mapper = new ObjectMapper();
    }

    public void start() {
        buyChannel = pusher.subscribe("price_ladders_cash_btcjpy_buy");
        buyChannel.bind("updated", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                    updateOrderBook(buys, mapper.readTree(data));
                    if (listener != null) {
                        if (!buys.isEmpty()) listener.change(id, true, buys.firstEntry().getKey(), buys.firstEntry().getValue());
                    }
                    //debugOrderBook();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        sellChannel = pusher.subscribe("price_ladders_cash_btcjpy_sell");
        sellChannel.bind("updated", new SubscriptionEventListener() {
            @Override
            public void onEvent(String channel, String event, String data) {
                try {
                //    System.out.println(Thread.currentThread().getId() + " sell " + data);
                    updateOrderBook(sells, mapper.readTree(data));
                    if (listener != null) {
                        if (!sells.isEmpty()) listener.change(id, false, sells.firstEntry().getKey(), sells.firstEntry().getValue());
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    private void updateOrderBook(TreeMap<Double, Double> existing, JsonNode delta) {
        existing.clear();
        for (JsonNode sell : delta) {
            double price = sell.get(0).asDouble();
            double amount = sell.get(1).asDouble();
            existing.put(price, amount);
            if (existing.size() == COUNT) break;
        }
    }
}
