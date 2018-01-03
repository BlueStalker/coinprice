package com.stalker.bitcoin.http;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.stalker.bitcoin.exchange.Bitflyer;
import com.stalker.bitcoin.exchange.CoinCheck;
import com.stalker.bitcoin.exchange.Exchange;
import com.stalker.bitcoin.exchange.ExchangeEnum;
import com.stalker.bitcoin.exchange.LocalExchange;
import com.stalker.bitcoin.exchange.Quoine;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import com.stalker.bitcoin.http.config.ExchangeConfiguration;
import com.stalker.bitcoin.http.config.SimulationConfiguration;
import com.stalker.bitcoin.stragtegy.SingleMaxMinMovingAverageThreshold;
import com.stalker.bitcoin.stragtegy.Strategy;
import com.stalker.bitcoin.stragtegy.StrategyEnum;
import com.stalker.bitcoin.trade.DefaultTradeManagement;
import com.stalker.bitcoin.trade.SimulationTradeManagement;
import com.stalker.bitcoin.trade.TradeManagement;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by curt on 1/2/18.
 */
public class CoinPriceModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger("coinprice");

    private final CoinPriceConfiguration config;
    private final Environment env;

    public CoinPriceModule(CoinPriceConfiguration config, Environment env) {
        this.config = config;
        this.env = env;
    }

    @Override
    protected void configure() {
        bind(CoinPriceConfiguration.class).toInstance(config);
        bind(MetricRegistry.class).toInstance(env.metrics());
        bind(ObjectMapper.class).toInstance(env.getObjectMapper());
        if (config.getSimulation()) {
            bind(TradeManagement.class).to(SimulationTradeManagement.class);
        } else {
            bind(TradeManagement.class).to(DefaultTradeManagement.class);
        }
    }

    @Singleton
    @Provides
    public Strategy provideStrategy(
            CoinPriceConfiguration config,
            @Named("allExchanges") Map<Integer, Exchange> exchanges,
            TradeManagement tradeManagement
    ) {
        return toStrategy(exchanges, StrategyEnum.fromName(config.getStrategy().getName()), config, tradeManagement);
    }

    @Singleton
    @Provides
    @Named("allExchanges")
    public Map<Integer, Exchange> provideExchangeConfigurations(CoinPriceConfiguration config) {
        ImmutableMap.Builder<Integer, Exchange> builder = ImmutableMap.builder();
        if (config.getSimulation()) {
            for (SimulationConfiguration.ExchangeSimulationConfiguration e : config.getSimulationConfiguration().getSimulationExchanges()) {
                Exchange exchange = toExchange(ExchangeEnum.fromName(e.getName()));
                builder.put(exchange.getID(), exchange);
            }
            builder.put(-1, new LocalExchange(config.getSimulationConfiguration().getFileName()));
        } else {
            for (ExchangeConfiguration ec: config.getExchanges()) {
                Exchange exchange = toExchange(ExchangeEnum.fromName(ec.getName()));
                builder.put(exchange.getID(), exchange);
            }
        }
        return builder.build();
    }

    private Strategy toStrategy(Map<Integer, Exchange> exchanges, StrategyEnum e, CoinPriceConfiguration config, TradeManagement tradeManagement) {
        switch (e) {
            case SingleMaxMinMovingAverageThreshold:
                return new SingleMaxMinMovingAverageThreshold(exchanges, config, tradeManagement);
            default: throw new RuntimeException("WTF with you");
        }
    }

    private Exchange toExchange(ExchangeEnum e) {
        switch (e) {
            case COINCHECK: return new CoinCheck(e.getId());
            case QUOINE: return new Quoine(e.getId());
            case BITFLYER: return new Bitflyer(e.getId());
            default: throw new RuntimeException("WTF with you");
        }
    }
}
