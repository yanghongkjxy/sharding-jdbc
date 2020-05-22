/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.metrics.prometheus;

import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.metrics.enums.MetricsTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class PrometheusMetricsTrackerFactoryTest {
    
    @Test
    public void assertCreate() {
        PrometheusMetricsTrackerFactory factory = new PrometheusMetricsTrackerFactory();
        assertThat(factory.create(MetricsTypeEnum.COUNTER.name(), MetricsLabelEnum.REQUEST_LATENCY.getName()).isPresent(), is(false));
        assertThat(factory.create(MetricsTypeEnum.COUNTER.name(), MetricsLabelEnum.REQUEST_TOTAL.getName()).isPresent(), is(true));
    }
}

