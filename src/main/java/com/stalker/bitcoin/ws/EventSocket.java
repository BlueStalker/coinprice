package com.stalker.bitcoin.ws;

import com.stalker.bitcoin.exchange.WebSocketExchange;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by curt on 12/27/17.
 */
public class EventSocket extends WebSocketAdapter {
    private static final Logger LOG = LoggerFactory.getLogger("coinprice");

    private WebSocketExchange exchange;

    public EventSocket(WebSocketExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        LOG.info("Socket Connected: " + sess);
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        exchange.onSocketText(message);
        //LOG.info("Received TEXT message: " + message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        LOG.info("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }
}