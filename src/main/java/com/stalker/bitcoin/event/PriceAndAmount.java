package com.stalker.bitcoin.event;

/**
 * Created by curt on 12/31/17.
 */
public class PriceAndAmount {
    public double price;
    public double amount;

    public PriceAndAmount(double price, double amount) {
        this.price = price;
        this.amount = amount;
    }
}
