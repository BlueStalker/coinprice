package com.stalker.bitcoin.http.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;

import java.util.List;

/**
 * Created by curt on 1/2/18.
 */
public class CoinPriceConfiguration extends Configuration {

    private Boolean simulation;

    private List<ExchangeConfiguration> exchanges = ImmutableList.of();

    private StrategyConfiguration strategy;

    private SimulationConfiguration simulationConfiguration;

    @JsonProperty
    public SimulationConfiguration getSimulationConfiguration() {
        return simulationConfiguration;
    }

    @JsonProperty
    public void setsimulationConfiguration(SimulationConfiguration simulationConfiguration) {
        this.simulationConfiguration = simulationConfiguration;
    }


    @JsonProperty("strategy")
    public StrategyConfiguration getStrategy() {
        return strategy;
    }

    @JsonProperty("strategy")
    public void setStrategy(StrategyConfiguration strategy) {
        this.strategy = strategy;
    }

    @JsonProperty
    public Boolean getSimulation() {
        return simulation;
    }

    @JsonProperty
    public void setSimulation(Boolean simulation) {
        this.simulation = simulation;
    }

    @JsonProperty("exchanges")
    public List<ExchangeConfiguration> getExchanges() {
        return exchanges;
    }

    @JsonProperty("exchanges")
    public void getExchanges(List<ExchangeConfiguration> exchanges) {
        this.exchanges = exchanges;
    }
}
