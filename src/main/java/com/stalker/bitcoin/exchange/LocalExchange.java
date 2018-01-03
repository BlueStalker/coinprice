package com.stalker.bitcoin.exchange;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.TreeMap;

/**
 * Created by curt on 1/3/18.
 */
public class LocalExchange extends AbstractExchange {

    private String fileName;
    // This is Local Exchange for simulation only
    public LocalExchange(String fileName) {
        super(-1);
        this.fileName = fileName;
    }

    public String getName() {
        return "local";
    }

    public void start() {
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(file);

            while(in.available() > 0) {
                listener.change(in.readLong(), in.readInt(), in.readBoolean(), (TreeMap)in.readObject());
            }

            in.close();
            file.close();

            System.out.println("Finish");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
