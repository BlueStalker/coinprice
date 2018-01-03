package com.stalker.bitcoin.http;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.stalker.bitcoin.http.config.CoinPriceConfiguration;
import com.stalker.bitcoin.http.resources.ApiResource;
import io.dropwizard.Application;
import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by curt on 1/2/18.
 */
public class CoinPriceApplication extends Application<CoinPriceConfiguration> {

    public static void main(String[] args) throws Exception {
        new CoinPriceApplication().run(args);
    }

    @Override
    public String getName() {
        return "CoinPrice";
    }

    @Override
    public void initialize(Bootstrap<CoinPriceConfiguration> bootstrap) {
        final MetricRegistry metrics = new MetricRegistry();
        final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();
        bootstrap.setMetricRegistry(metrics);
        super.initialize(bootstrap);
    }

    @Override
    public void run(CoinPriceConfiguration configuration, Environment environment) throws Exception {

        Injector injector = Guice.createInjector(new CoinPriceModule(configuration, environment));

        configureEnvironment(configuration, environment, injector);
    }

    public static void configureEnvironment(CoinPriceConfiguration configuration, Environment environment, Injector injector) {

        environment.jersey().register(new JsonProcessingExceptionMapper(true));
        environment.jersey().register(new EarlyEofExceptionMapper());
        environment.jersey().register(new GenericExceptionMapper());

        environment.healthChecks().register("healthcheck", new CoinPriceHealthCheck());
        final ApiResource api = injector.getInstance(ApiResource.class);
        environment.jersey().register(api);
    }
}
