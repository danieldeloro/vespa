// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.jdisc.application;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.yahoo.jdisc.Metric;

/**
 * @author <a href="mailto:simon@yahoo-inc.com">Simon Thoresen Hult</a>
 */
public class MetricProvider implements Provider<Metric> {

    private final Provider<MetricConsumer> consumerProvider;

    @Inject
    public MetricProvider(Provider<MetricConsumer> consumerProvider) {
        this.consumerProvider = consumerProvider;
    }

    @Override
    public Metric get() {
        return new MetricImpl(consumerProvider);
    }
}
