package com.stalker.bitcoin.exchange;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by curt on 12/28/17.
 */
public class CoinCheckPerfTest extends WebSocketExchange {

    private static final String WSS = "wss://ws-api.coincheck.com/";
    private static final String ENDPOINT = "{\"type\":\"subscribe\",\"channel\":\"btc_jpy-orderbook\"}";

    private static final int COUNT = 10;
    public CoinCheckPerfTest() {
        super(10001, "coincheck", WSS, ENDPOINT);
        orderBuys = new LinkedList<>();
        orderSells = new LinkedList<>();
    }

    private LinkedList<float[]> orderBuys;
    int countBuys = 0;
    int countSells = 0;
    private LinkedList<float[]> orderSells;

    @Override
    public void onSocketText(String s) {
        System.out.println(Thread.currentThread().getId() + " " + System.nanoTime() + s);
        long start = System.nanoTime();
        int n = s.length();
        // https://coincheck.com/documents/exchange/api#websocket-order-book
        boolean buy = false;
        boolean sell = false;
        boolean isPrice = true;
        float price = 0f;
        ArrayList<float[]> localBuys = new ArrayList<>();
        ArrayList<float[]> localSells = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while(s.charAt(i) != '{') i++;
        for (; i < n - 1; i++) {
            if (s.charAt(i) == '"') {
                int j = i + 1;
                boolean isValue = false;
                float point = 0.0f;
                float value = 0.0f;
                char ch = s.charAt(j);
                if (Character.isDigit(ch)) isValue = true;
                while((ch = s.charAt(j)) != '"') {
                    if (isValue) {
                        if (ch == '.') {
                            point = 10.0000000000f;
                        } else {
                            if (point == 0.0f) {
                                value = value * 10.0000000000f + (ch - '0');
                            }
                            else {
                                value = value + (ch - '0') * 1.0f / point;
                                point *= 10.0000000000f;
                            }
                        }
                    } else {
                        sb.append(ch);
                    }
                    j++;
                }
                if (sb.toString().equals("bids")) {
                    // Buy order
                    sell = false;
                    buy = true;
                } else if (sb.toString().equals("asks")) {
                    // sell order
                    buy = false;
                    sell = true;
                } else {
                    // real value
                    if (isPrice) {
                        price = value;
                    } else {
                        // amount
                        if (buy) {
                            localBuys.add(new float[] {price, value});
                        }
                        if (sell) {
                            localSells.add(new float[] {price, value});
                        }
                    }
                    isPrice = !isPrice;
                }
                i = j;
                sb = new StringBuilder();
            }
        }
        //System.out.println(Thread.currentThread().getId() + " " + (System.nanoTime()-start) + " parse finish");
        processBuy(localBuys);
//        processSell(localSells);
        ListIterator<float[]> it = orderBuys.listIterator();
        while(it.hasNext()) {
            float[] k = it.next();
            System.out.println(k[0] + " " + k[1]);
        }
//        it = orderSells.listIterator();
//        System.out.println("#################");
//        while(it.hasNext()) {
//            float[] k = it.next();
//            System.out.println(k[0] + " " + k[1]);
//        }
        System.out.println(Thread.currentThread().getId() + " " + (System.nanoTime()-start) + " total " + orderBuys.size());
        System.out.println(Thread.currentThread().getId() + " " + (System.nanoTime()-start) + " total " + orderSells.size());

        //   System.out.println(Thread.currentThread().getId() + " coincheck " + price);
    }

    private void processSell(ArrayList<float[]> array) {
        ListIterator<float[]> it = orderSells.listIterator();
        int i = 0;
        int size = array.size();
        while(i < size) {
            float[] item = array.get(i);
            if (item[1] != 0.0f || !it.hasNext()) break;
            float[] current = it.next();
            if (current[0] == item[0]) {
                it.remove();
                countSells--;
            } else if (current[0] > item[0]) {
                it.previous();
            } else {
                i--;
            }
            i++;
        }
        while(i < size && array.get(i)[1] == 0.0f) i++;
        it = orderSells.listIterator();
        while(i < size) {
            float[] item = array.get(i);
            if (!it.hasNext()) {
                it.add(item);
                countSells++;
            } else {
                float[] current = it.next();
                if (current[0] == item[0]) {
                    // refresh amount
                    it.set(item);
                } else if (current[0] > item[0]) {
                    it.previous();
                    it.add(item);
                    countSells++;
                } else {
                    i--;
                }
            }
            if (countSells == COUNT) break;
            i++;
        }
    }

    private synchronized void processBuy(ArrayList<float[]> array) {
        ListIterator<float[]> it = orderBuys.listIterator();
        int i = 0;
        int size = array.size();
        while(i < size) {
            float[] item = array.get(i);
            if (item[1] != 0.0f || !it.hasNext()) break;
            float[] current = it.next();
            if (current[0] == item[0]) {
                it.remove();
                countBuys--;
            } else if (current[0] < item[0]) {
                it.previous();
            } else {
                i--;
            }
            i++;
        }
        while(i < size && array.get(i)[1] == 0.0f) i++;
        it = orderBuys.listIterator();
        while(i < size) {
            float[] item = array.get(i);
            if (!it.hasNext()) {
                it.add(item);
                countBuys++;
            } else {
                float[] current = it.next();
                if (current[0] == item[0]) {
                    // refresh amount
                    it.set(item);
                } else if (current[0] < item[0]) {
                    it.previous();
                    it.add(item);
                    countBuys++;
                } else {
                    i--;
                }
            }
            if (countBuys == COUNT) break;
            i++;
        }
    }
}
