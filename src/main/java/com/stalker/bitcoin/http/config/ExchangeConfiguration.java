package com.stalker.bitcoin.http.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by curt on 1/2/18.
 */
public class ExchangeConfiguration {

    private final String name;

    @JsonCreator
    public ExchangeConfiguration(
            @JsonProperty String name
    ) {
        this.name = name;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

}
