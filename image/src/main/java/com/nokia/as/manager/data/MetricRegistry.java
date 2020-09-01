/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.manager.data;

import com.nokia.as.connector.core.AbstractConnector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricRegistry {
    static final Logger logger = LoggerFactory.getLogger(AbstractConnector.class);

    private CollectorRegistry collectorRegistry;

    public MetricRegistry() {
        this.collectorRegistry = new CollectorRegistry();
    }

    public CollectorRegistry getCollectorRegistry() {
        return collectorRegistry;
    }

    public void register(GaugeMetric gaugeMetric) {
        Gauge gauge = gaugeMetric.getGauge();

        collectorRegistry.register(gauge);
        gaugeMetric.addMetricRegistry(this);

        gauge.labels(gaugeMetric.getLabelValues().toArray(String[]::new));

        if (gaugeMetric.isMutable()) {
            Gauge tsGauge = gaugeMetric.getTsGauge();
            collectorRegistry.register(tsGauge);
            tsGauge.labels(gaugeMetric.getNoMutableLabelValues().toArray(String[]::new));
        }

        logger.info("Register metric: " + gaugeMetric.getName());
    }

    public void unregister(GaugeMetric gaugeMetric) {
        Gauge gauge = gaugeMetric.getGauge();

        collectorRegistry.unregister(gauge);
        gaugeMetric.removeMetricRegistry(this);

        if (gaugeMetric.isMutable()) {
            Gauge tsGauge = gaugeMetric.getTsGauge();
            collectorRegistry.unregister(tsGauge);
        }

        logger.info("Unregister metric: " + gaugeMetric.getName());
    }

    public void update(Gauge previousGauge, GaugeMetric gaugeMetric) {
        Gauge gauge = gaugeMetric.getGauge();
        collectorRegistry.unregister(previousGauge);
        collectorRegistry.register(gauge);
        gauge.labels(gaugeMetric.getLabelValues().toArray(String[]::new));

        logger.info("Update metric: " + gaugeMetric.getName());
    }

    public void clear() {
        collectorRegistry.clear();
    }
}
