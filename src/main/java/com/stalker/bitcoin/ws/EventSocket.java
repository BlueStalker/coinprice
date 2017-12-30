package com.stalker.bitcoin.ws;

import com.stalker.bitcoin.exchange.WebSocketExchange;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

/**
 * Created by curt on 12/27/17.
 */
public class EventSocket extends WebSocketAdapter {

    private WebSocketExchange exchange;

    public EventSocket(WebSocketExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        //System.out.println("Socket Connected: " + sess);
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        exchange.onSocketText(message);
        //System.out.println("Received TEXT message: " + message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }
}