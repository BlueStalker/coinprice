package com.stalker.bitcoin.http;

import com.codahale.metrics.health.HealthCheck;

/**
 * Created by curt on 1/2/18.
 */
public class CoinPriceHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
