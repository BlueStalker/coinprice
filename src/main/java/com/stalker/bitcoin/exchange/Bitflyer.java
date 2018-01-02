package com.stalker.bitcoin.exchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by curt on 12/28/17.
 */
public class Bitflyer extends AbstractExchange {

    private static final String KEY = "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f";
    public String getName() {
        return "bitflyer";
    }
    PubNub pubnub;
    public Bitflyer(int id) {
        super(id);
        PNConfiguration pnConf = new PNConfiguration();
        pnConf.setSubscribeKey(KEY);
        pubnub = new PubNub(pnConf);
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }
            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                JsonElement elem = message.getMessage();
                processOrder(elem.getAsJsonObject().get("bids").getAsJsonArray(), buys);
                processOrder(elem.getAsJsonObject().get("asks").getAsJsonArray(), sells);
                //debugOrderBook();
                if (listener != null) {
                    long ts = System.currentTimeMillis();
                    if (!buys.isEmpty()) listener.change(ts, id, true, buys);
                    if (!sells.isEmpty()) listener.change(ts, id, false, sells);
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });
    }

    private void processOrder(JsonArray delta, TreeMap<Double, Double> existing) {
        for (JsonElement e : delta) {
            JsonObject object = e.getAsJsonObject();
            double price = object.get("price").getAsDouble();
            double amount = object.get("size").getAsDouble();
            if (amount == 0) {
                existing.remove(price);
            } else {
                existing.put(price, amount);
                if (existing.size() > COUNT) {
                    existing.remove(existing.lastKey());
                }
            }
        }
    }
    public void start() {
        pubnub.subscribe().channels(Arrays.asList("lightning_board_BTC_JPY")).execute();
    }
}
