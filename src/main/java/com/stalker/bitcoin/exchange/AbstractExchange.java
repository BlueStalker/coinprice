package com.stalker.bitcoin.exchange;

import java.util.Collections;
import java.util.TreeMap;

/**
 * Created by curt on 12/29/17.
 */
public abstract class AbstractExchange implements Exchange {

    static final int COUNT = 5;
    protected ExchangePriceChangeListener listener;
    protected TreeMap<Double, Double> buys;
    protected TreeMap<Double, Double> sells;
    protected int id;


    public AbstractExchange(int id) {
        this.id = id;
        buys = new TreeMap<>(Collections.reverseOrder());
        sells = new TreeMap<>();
    }

    public void setOnPriceChangeListener(ExchangePriceChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public int getID() {
        return id;
    }

    public TreeMap<Double, Double> getBuys() {
        return buys;
    }

    public TreeMap<Double, Double> getSells() {
        return sells;
    }

    public void debugOrderBook() {
        System.out.println();
        System.out.println("########" + getName() + " buys #############");
        for (Double key : buys.keySet()) System.out.println(key + " " + buys.get(key));
        System.out.println("########" + getName() + " sells #############");
        for (Double key : sells.keySet()) System.out.println(key + " " + sells.get(key));
        System.out.println("######################################");
    }
}
