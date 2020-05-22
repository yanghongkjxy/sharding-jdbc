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

package org.apache.shardingsphere.cluster.configuration.swapper;

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.config.HeartBeatConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlHeartBeatConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

/**
 * Cluster configuration YAML swapper.
 */
public final class ClusterConfigurationYamlSwapper implements YamlSwapper<YamlClusterConfiguration, ClusterConfiguration> {
    
    @Override
    public YamlClusterConfiguration swap(final ClusterConfiguration clusterConfiguration) {
        final YamlClusterConfiguration yamlClusterConfiguration = new YamlClusterConfiguration();
        final YamlHeartBeatConfiguration yamlHeartBeatConfiguration = new YamlHeartBeatConfiguration();
        yamlHeartBeatConfiguration.setSql(clusterConfiguration.getHeartBeat().getSql());
        yamlHeartBeatConfiguration.setInterval(clusterConfiguration.getHeartBeat().getInterval());
        yamlHeartBeatConfiguration.setRetryEnable(clusterConfiguration.getHeartBeat().getRetryEnable());
        yamlHeartBeatConfiguration.setRetryMaximum(clusterConfiguration.getHeartBeat().getRetryMaximum());
        yamlHeartBeatConfiguration.setThreadCount(clusterConfiguration.getHeartBeat().getThreadCount());
        yamlClusterConfiguration.setHeartBeat(yamlHeartBeatConfiguration);
        return yamlClusterConfiguration;
    }
    
    @Override
    public ClusterConfiguration swap(final YamlClusterConfiguration yamlConfiguration) {
        final ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        final HeartBeatConfiguration heartBeatConfiguration = new HeartBeatConfiguration();
        heartBeatConfiguration.setSql(yamlConfiguration.getHeartBeat().getSql());
        heartBeatConfiguration.setInterval(yamlConfiguration.getHeartBeat().getInterval());
        heartBeatConfiguration.setRetryEnable(yamlConfiguration.getHeartBeat().getRetryEnable());
        heartBeatConfiguration.setRetryMaximum(yamlConfiguration.getHeartBeat().getRetryMaximum());
        heartBeatConfiguration.setThreadCount(yamlConfiguration.getHeartBeat().getThreadCount());
        clusterConfiguration.setHeartBeat(heartBeatConfiguration);
        return clusterConfiguration;
    }
}
