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

package org.apache.shardingsphere.example.sharding.raw.jdbc.config;

import org.apache.shardingsphere.example.algorithm.StandardModuloShardingDatabaseAlgorithm;
import org.apache.shardingsphere.example.algorithm.StandardModuloShardingTableAlgorithm;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveDataSourceConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.TableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.strategy.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.keygen.KeyGenerateAlgorithm;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ShardingMasterSlaveConfigurationPrecise implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Arrays.asList(createShardingRuleConfiguration(), createMasterSlaveRuleConfiguration()), new Properties());
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        final Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_master_0", DataSourceUtil.createDataSource("demo_ds_master_0"));
        result.put("demo_ds_master_0_slave_0", DataSourceUtil.createDataSource("demo_ds_master_0_slave_0"));
        result.put("demo_ds_master_0_slave_1", DataSourceUtil.createDataSource("demo_ds_master_0_slave_1"));
        result.put("demo_ds_master_1", DataSourceUtil.createDataSource("demo_ds_master_1"));
        result.put("demo_ds_master_1_slave_0", DataSourceUtil.createDataSource("demo_ds_master_1_slave_0"));
        result.put("demo_ds_master_1_slave_1", DataSourceUtil.createDataSource("demo_ds_master_1_slave_1"));
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTableRuleConfigs().add(createOrderTableRuleConfiguration());
        result.getTableRuleConfigs().add(createOrderItemTableRuleConfiguration());
        result.getBindingTableGroups().add("t_order, t_order_item");
        result.getBroadcastTables().add("t_address");
        result.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new StandardModuloShardingDatabaseAlgorithm()));
        result.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new StandardModuloShardingTableAlgorithm()));
        return result;
    }
    
    private static TableRuleConfiguration createOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_order", "ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGeneratorConfig(new KeyGeneratorConfiguration("order_id", createSnowflakeKeyGenerateAlgorithm()));
        return result;
    }
    
    private static TableRuleConfiguration createOrderItemTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item_${[0, 1]}");
        result.setKeyGeneratorConfig(new KeyGeneratorConfiguration("order_item_id", createSnowflakeKeyGenerateAlgorithm()));
        return result;
    }
    
    private static KeyGenerateAlgorithm createSnowflakeKeyGenerateAlgorithm() {
        KeyGenerateAlgorithm result = new SnowflakeKeyGenerateAlgorithm();
        result.setProperties(createProperties());
        return result;
    }
    
    private static MasterSlaveRuleConfiguration createMasterSlaveRuleConfiguration() {
        MasterSlaveDataSourceConfiguration dataSourceConfiguration1 = new MasterSlaveDataSourceConfiguration(
                "ds_0", "demo_ds_master_0", Arrays.asList("demo_ds_master_0_slave_0", "demo_ds_master_0_slave_1"));
        MasterSlaveDataSourceConfiguration dataSourceConfiguration2 = new MasterSlaveDataSourceConfiguration(
                "ds_1", "demo_ds_master_1", Arrays.asList("demo_ds_master_1_slave_0", "demo_ds_master_1_slave_1"));
        return new MasterSlaveRuleConfiguration(Arrays.asList(dataSourceConfiguration1, dataSourceConfiguration2));
    }
    
    private static Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("worker.id", "123");
        return result;
    }
}
