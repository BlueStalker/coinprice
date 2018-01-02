package com.stalker.bitcoin.exchange;

import com.stalker.bitcoin.ws.EventSocket;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.Future;

/**
 * Created by curt on 12/27/17.
 */
public abstract class WebSocketExchange extends AbstractExchange {

    private String name;
    private String ws;
    private String endpoint;

    private WebSocketClient client;
    private Session session;

    public WebSocketExchange(int id, String name, String ws, String endpoint) {
        super(id);
        this.name = name;
        this.ws = ws;
        this.endpoint = endpoint;
    }

    public String getName() {
        return name;
    }

    public abstract void onSocketText(String message);

    public void start() {
        URI uri = URI.create(ws);

        client = new WebSocketClient(new SslContextFactory());
        try {
            client.start();
            // The socket that receives events
            EventSocket socket = new EventSocket(this);
            // Attempt Connect
            Future<Session> fut = client.connect(socket, uri);
            // Wait for Connect
            session = fut.get();
            // Send a message
            if (endpoint != null) {
                session.getRemote().sendString(endpoint);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void stop() throws Exception {
        if (session != null) {
            session.close();
        }
        if (client != null) {
            client.stop();
        }
    }
}
