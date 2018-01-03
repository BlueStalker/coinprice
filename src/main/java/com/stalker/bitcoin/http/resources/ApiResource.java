package com.stalker.bitcoin.http.resources;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.stalker.bitcoin.Calculation;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import com.stalker.bitcoin.model.Balance;
import com.stalker.bitcoin.trade.TradeManagement;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by curt on 1/2/18.
 */
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public class ApiResource {
    private Calculation calculation;
    private ObjectMapper mapper;
    private MetricRegistry metrics;
    private CoinPriceConfiguration config;
    private TradeManagement tradeManagement;

    @Inject
    public ApiResource(Calculation calculation,
                       ObjectMapper objectMapper,
                       MetricRegistry metrics,
                       CoinPriceConfiguration config,
                       TradeManagement tradeManagement) {
        this.calculation = calculation;
        this.mapper = objectMapper;
        this.metrics = metrics;
        this.config = config;
        this.tradeManagement = tradeManagement;
    }

    @GET
    @Path("balance")
    public Map<String, Balance> getBalances() throws Exception {
        return tradeManagement.getBalances();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("begin")
    public Object query(@Context HttpServletRequest request,
                        @Context HttpHeaders httpHeaders
                        ) throws Exception {
        return calculation.start();
    }

    @GET
    @Path("status")
    public Status status() throws Exception {
        return calculation.status();
    }
}
