/**
 * Copyright (C) 2012 Ryan W Tenney (ryan@10e.us)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ryantenney.metrics.spring.reporter;


import io.dropwizard.metrics.influxdb.InfluxDbHttpSender;
import io.dropwizard.metrics.influxdb.InfluxDbReporter;
import io.dropwizard.metrics.influxdb.InfluxDbSender;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:david@davidkarlsen.com">David J. M. Karlsen</a>
 * @since 4.0.0
 */
public class InfluxdbReporterFactoryBean
    extends AbstractScheduledReporterFactoryBean<InfluxDbReporter>
{

    // Required
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String DATABASE = "database";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    //defaulted
    public static final String PERIOD = "period";
    public static final int DEFAULT_PORT = 8086;

    //optionals
    public static final String PRECISION = "precision";
    public static final String DURATION_UNIT = "duration-unit";
    public static final String RATE_UNIT = "rate-unit";


    @Override
    protected long getPeriod() {
   		return convertDurationString(getProperty(PERIOD));
   	}

    @Override
    public Class<? extends InfluxDbReporter> getObjectType()
    {
        return InfluxDbReporter.class;
    }

    @Override
    protected InfluxDbReporter createInstance()
        throws Exception
    {
        final InfluxDbReporter.Builder influxdbReporterBuilder = InfluxDbReporter.forRegistry(getMetricRegistry()).filter(getMetricFilter());

        if ( hasProperty(DURATION_UNIT) ) {
            influxdbReporterBuilder.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
        }

        if ( hasProperty(RATE_UNIT) ) {
            influxdbReporterBuilder.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
        }

        return influxdbReporterBuilder.build(getInfluxDbSender());
    }

    private InfluxDbSender getInfluxDbSender()
        throws Exception
    {
        final String hostname = getProperty(HOST);
        final int port = getProperty(PORT, Integer.TYPE, DEFAULT_PORT);
        final String database = getProperty(DATABASE);
        final String username = getProperty(USERNAME);
        final String password = getProperty(PASSWORD);
        final TimeUnit timeUnit = getProperty(PRECISION, TimeUnit.class, null);

        return timeUnit != null ?
            new InfluxDbHttpSender( hostname, port, database, username, password, timeUnit ) :
            new InfluxDbHttpSender( hostname, port, database, username, password );
    }
}
