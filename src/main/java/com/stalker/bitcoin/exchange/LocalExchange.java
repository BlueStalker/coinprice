package com.stalker.bitcoin.exchange;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
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
        FileInputStream file = null;
        ObjectInputStream in = null;
        try
        {
            // Reading the object from a file
            file = new FileInputStream(fileName);
            in = new ObjectInputStream(file);
            System.out.println("##################");
            while(true) {
                int available = in.available();
                if (available <= 0) break;
                listener.change(in.readLong(), in.readInt(), in.readBoolean(), (TreeMap)in.readObject());
            }
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$");
        } catch (StreamCorruptedException e) {
            // Ignore
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (file != null) file.close();
            } catch (Exception ignore) {
            }
        }
    }
}
