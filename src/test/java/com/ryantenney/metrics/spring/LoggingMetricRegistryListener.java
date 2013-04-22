package com.ryantenney.metrics.spring;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Created with IntelliJ IDEA.
* User: ryan
* Date: 4/2/13
* Time: 5:50 PM
* To change this template use File | Settings | File Templates.
*/
class LoggingMetricRegistryListener implements MetricRegistryListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingMetricRegistryListener.class);

    @Override
    public void onGaugeAdded(String s, Gauge<?> gauge) {
        log.info("Gauge added: {}", s);
    }

    @Override
    public void onGaugeRemoved(String s) {
        log.info("Gauge removed: {}", s);
    }

    @Override
    public void onCounterAdded(String s, Counter counter) {
        log.info("Counter added: {}", s);
    }

    @Override
    public void onCounterRemoved(String s) {
        log.info("Counter removed: {}", s);
    }

    @Override
    public void onHistogramAdded(String s, Histogram histogram) {
        log.info("Histogram added: {}", s);
    }

    @Override
    public void onHistogramRemoved(String s) {
        log.info("Histogram removed: {}", s);
    }

    @Override
    public void onMeterAdded(String s, Meter meter) {
        log.info("Meter added: {}", s);
    }

    @Override
    public void onMeterRemoved(String s) {
        log.info("Meter removed: {}", s);
    }

    @Override
    public void onTimerAdded(String s, Timer timer) {
        log.info("Timer added: {}", s);
    }

    @Override
    public void onTimerRemoved(String s) {
        log.info("Timer removed: {}", s);
    }
}
