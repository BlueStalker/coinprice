package com.stalker.bitcoin.http.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by curt on 1/2/18.
 */
public class SimulationConfiguration {

    private List<ExchangeSimulationConfiguration> simulationExchanges = ImmutableList.of();

    @Nullable
    private String fileName;

    private String mode;

    @JsonProperty
    public String getMode() {
        return mode;
    }

    @JsonProperty
    public void setMode(String mode) {
        this.mode = mode;
    }

    @JsonProperty
    public String getFileName() {
        return fileName;
    }

    @JsonProperty
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonProperty("simulationExchanges")
    public List<ExchangeSimulationConfiguration> getSimulationExchanges() {
        return simulationExchanges;
    }

    @JsonProperty("simulationExchanges")
    public void simulationExchanges(List<ExchangeSimulationConfiguration> exchanges) {
        this.simulationExchanges = exchanges;
    }


    public static class ExchangeSimulationConfiguration {
        private final String name;
        private final Double cash;
        private final Double coin;

        @JsonCreator
        public ExchangeSimulationConfiguration(
                @JsonProperty("name") String name,
                @JsonProperty("cash") Double cash,
                @JsonProperty("coin") Double coin
        ) {
            this.name = name;
            this.cash = cash;
            this.coin = coin;
        }

        public String getName() {
            return name;
        }

        public Double getCash() {
            return cash;
        }

        public double getCoin() {
            return coin;
        }
    }
}
