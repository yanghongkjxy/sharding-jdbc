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

package org.apache.shardingsphere.metrics.configuration.swapper;

import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class MetricsConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapDefault() {
        MetricsConfigurationYamlSwapper swapper = new MetricsConfigurationYamlSwapper();
        YamlMetricsConfiguration yaml = new YamlMetricsConfiguration();
        yaml.setHost("127.0.0.1");
        yaml.setName("prometheus");
        
        MetricsConfiguration metricsConfiguration = swapper.swap(yaml);
        assertThat(metricsConfiguration.getPort(), is(9190));
        assertThat(metricsConfiguration.getAsync(), is(true));
        assertThat(metricsConfiguration.getThreadCount(), is(Runtime.getRuntime().availableProcessors() << 1));
    
        YamlMetricsConfiguration yamlSwap = swapper.swap(metricsConfiguration);
        assertNotNull(yamlSwap);
        assertThat(yamlSwap.getPort(), is(9190));
        assertThat(yamlSwap.getName(), is("prometheus"));
        assertThat(yamlSwap.getHost(), is("127.0.0.1"));
        assertThat(yamlSwap.getAsync(), is(true));
        assertThat(yamlSwap.getThreadCount(), is(Runtime.getRuntime().availableProcessors() << 1));
    }
    
    @Test
    public void assertSwapFull() {
        MetricsConfigurationYamlSwapper swapper = new MetricsConfigurationYamlSwapper();
        YamlMetricsConfiguration yaml = new YamlMetricsConfiguration();
        yaml.setHost("127.0.0.1");
        yaml.setName("prometheus");
        yaml.setPort(9195);
        yaml.setThreadCount(8);
        yaml.setAsync(false);
        
        MetricsConfiguration metricsConfiguration = swapper.swap(yaml);
        assertThat(metricsConfiguration.getPort(), is(9195));
        assertThat(metricsConfiguration.getAsync(), is(false));
        assertThat(metricsConfiguration.getThreadCount(), is(8));
        
        YamlMetricsConfiguration yamlSwap = swapper.swap(metricsConfiguration);
        assertNotNull(yamlSwap);
        assertThat(yamlSwap.getPort(), is(9195));
        assertThat(yamlSwap.getName(), is("prometheus"));
        assertThat(yamlSwap.getHost(), is("127.0.0.1"));
        assertThat(yamlSwap.getAsync(), is(false));
        assertThat(yamlSwap.getThreadCount(), is(8));
    }
}

