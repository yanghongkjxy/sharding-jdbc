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

package org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local;

import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.sharding.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.TableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.strategy.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.strategy.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.keygen.KeyGenerateAlgorithm;
import org.apache.shardingsphere.driver.orchestration.api.OrchestrationShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class LocalShardingDatabasesConfiguration implements ExampleConfiguration {
    
    private final Map<String, CenterConfiguration> centerConfigurationMap;
    
    public LocalShardingDatabasesConfiguration(final Map<String, CenterConfiguration> centerConfigurationMap) {
        this.centerConfigurationMap = centerConfigurationMap;
    }
    
    private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
        return new KeyGeneratorConfiguration("order_id", getSnowflakeKeyGenerateAlgorithm());
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        shardingRuleConfig.getBroadcastTables().add("t_address");
        InlineShardingAlgorithm shardingAlgorithm = new InlineShardingAlgorithm();
        shardingAlgorithm.getProperties().setProperty("algorithm.expression", "demo_ds_${user_id % 2}");
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", shardingAlgorithm));
        OrchestrationConfiguration orchestrationConfig = new OrchestrationConfiguration(centerConfigurationMap);
        Collection<RuleConfiguration> configurations = new LinkedList<>();
        configurations.add(shardingRuleConfig);
        return OrchestrationShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), configurations, new Properties(), orchestrationConfig);
    }
    
    private TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_order");
        result.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
        return result;
    }
    
    private TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        return new TableRuleConfiguration("t_order_item");
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
        result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
        return result;
    }
    
    private static KeyGenerateAlgorithm getSnowflakeKeyGenerateAlgorithm() {
        KeyGenerateAlgorithm result = new SnowflakeKeyGenerateAlgorithm();
        result.setProperties(getProperties());
        return result;
    }
    
    private static Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("worker.id", "123");
        return result;
    }
}
