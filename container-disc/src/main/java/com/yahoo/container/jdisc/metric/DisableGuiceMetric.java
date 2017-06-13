// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.container.jdisc.metric;

import com.yahoo.jdisc.Metric;

import java.util.Map;

/**
 * @author tonytv
 */
public class DisableGuiceMetric implements Metric {

    @Override
    public void set(String s, Number number, Context context) {
        throw newException();
    }

    @Override
    public void add(String s, Number number, Context context) {
        throw newException();
    }

    @Override
    public Context createContext(Map<String, ?> stringMap) {
        throw newException();
    }

    private static RuntimeException newException() {
        return new UnsupportedOperationException("The Metric framework is only available to components.");
    }
}
