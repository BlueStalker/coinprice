package com.stalker.bitcoin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import com.stalker.bitcoin.http.resources.Status;
import com.stalker.bitcoin.stragtegy.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by curt on 12/26/17.
 */
@Singleton
public class Calculation {

    private static final Logger LOG = LoggerFactory.getLogger("coinprice");
    private Map<Integer, Exchange> exchanges;
    private boolean isStarted;
    private long serverStartTime;
    private CoinPriceConfiguration config;

    @Inject
    public Calculation(
            @Named("allExchanges")Map<Integer, Exchange> exchanges,
            Strategy strategy,
            CoinPriceConfiguration config
            ) throws Exception {
        this.exchanges = exchanges;
        this.serverStartTime = System.currentTimeMillis();
        this.config = config;
        for (Exchange e: exchanges.values()) {
            e.setOnPriceChangeListener(strategy.getExchangePriceChangeListener());
        }
    }

    public Status status() {
        Status s = new Status();
        s.setUptime(System.currentTimeMillis() - serverStartTime);
        s.setStarted(isStarted);
        s.setStatus("green");
        return s;
    }

    public synchronized boolean start() throws Exception {
        if (isStarted) return false;
        //if (!config.getSimulation()) {
            for (Exchange e : exchanges.values()) {
                if (e.getID() != -1) e.start();
            }
        //} else {

        //}
        isStarted = true;
        LOG.info("System calculation started!");
        return true;
    }

    public static void main(String[] argv) {
        String filename = "file.ser";

        // Serialization
        try
        {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream output = new ObjectOutputStream(file);
            Random r = new Random();
            for (int i = 0; i < 1000; i++) {
                output.writeLong(System.currentTimeMillis());
                output.writeBoolean(true);
                TreeMap<Double, Double> tree = new TreeMap<>();
                r.setSeed(System.currentTimeMillis());
                tree.put(r.nextDouble(), r.nextDouble());
                output.writeObject(tree);
            }
            System.out.println("Ser Done");
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            while(in.available() > 0) {
                System.out.println(in.readLong() + " " + in.readBoolean() + " " + in.readObject());
            }

            in.close();
            file.close();

            System.out.println("Object has been deserialized ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
