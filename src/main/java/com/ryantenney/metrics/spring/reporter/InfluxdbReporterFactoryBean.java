/**
 * Copyright (C) 2016 Andrey Smorodin (andrey.v.smorodin@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ryantenney.metrics.spring.reporter;

import com.codahale.metrics.ScheduledReporter;
import com.ryantenney.metrics.spring.InfluxdbAuthInfo;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.MetricMeasurementTransformer;
import metrics_influxdb.api.protocols.InfluxdbProtocols;

import java.util.concurrent.TimeUnit;

public class InfluxdbReporterFactoryBean extends AbstractScheduledReporterFactoryBean<ScheduledReporter> {

    //Defaults
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin";


    //Required
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String DATABASE = "database";
    public static final String INFLUXDB_CONNECTION_FACTORY_REF = "influxdb-auth-ref";
    public static final String PERIOD = "period";

    //Optional
    public static final String SKIP_IDLE = "skipIdle";
    public static final String DURATION_UNIT = "duration-unit";
    public static final String RATE_UNIT = "rate-unit";
    public static final String METRIC_MEASUREMENT_TRANSFORMER = "metric-measurement-transformer-ref";

    @Override
    public Class<ScheduledReporter> getObjectType() {
        return ScheduledReporter.class;
    }

    @Override
    protected ScheduledReporter createInstance() {
        final InfluxdbReporter.Builder reporter = InfluxdbReporter.forRegistry(getMetricRegistry());

        final String hostname = getProperty(HOST);
        final int port = getProperty(PORT, Integer.TYPE);
        final String database = getProperty(DATABASE);
        final String skipIdleMetrics = getProperty(SKIP_IDLE);

        if ("true".equals(skipIdleMetrics)) {
            reporter.skipIdleMetrics(true);
        } else {
            reporter.skipIdleMetrics(false);
        }

        if (hasProperty(RATE_UNIT))
            reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));

        if (hasProperty(DURATION_UNIT))
            reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));

        String userName = DEFAULT_USERNAME;
        String password = DEFAULT_PASSWORD;
        if (hasProperty(INFLUXDB_CONNECTION_FACTORY_REF)) {
            InfluxdbAuthInfo authInfo = getPropertyRef(INFLUXDB_CONNECTION_FACTORY_REF,
                    InfluxdbAuthInfo.class);
            userName = authInfo.getUserName();
            password = authInfo.getPassword();
        }

        reporter.protocol(InfluxdbProtocols.http(hostname, port, userName, password, database));

        MetricMeasurementTransformer mmt = MetricMeasurementTransformer.NOOP;
        if (hasProperty(METRIC_MEASUREMENT_TRANSFORMER))
            mmt = getPropertyRef(METRIC_MEASUREMENT_TRANSFORMER, MetricMeasurementTransformer.class);
        reporter.filter(getMetricFilter()).transformer(mmt);

        return reporter.build();
    }

    @Override
    protected long getPeriod() {
        return convertDurationString(getProperty(PERIOD));
    }
}