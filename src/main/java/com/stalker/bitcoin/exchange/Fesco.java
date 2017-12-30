package com.stalker.bitcoin.exchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by curt on 12/28/17.
 */
public class Fesco extends WebSocketExchange {
    private static final String WSS = "wss://ws-api.coincheck.com/";
    private static final String ENDPOINT = "{\"type\":\"subscribe\",\"channel\":\"btc_jpy-orderbook\"}";
    ObjectMapper mapperFcce = new ObjectMapper();

    public Fesco(int id) {
        super(id, "fesco", WSS, ENDPOINT);
    }

    @Override
    public void onSocketText(String message) {
        try {
            JsonNode node = mapperFcce.readTree(message.getBytes()).get("trades");
            Iterator<JsonNode> it= node.iterator();
            float sum = 0.0f;
            int total = 0;
            while(it.hasNext()) {
                sum += it.next().get("price").asDouble();
                total++;
            }

            //exchangePrices.put(1, sum / total);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
