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

package org.apache.shardingsphere.kernal.context.schema;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRulesBuilder;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

@Getter
public final class ShardingSphereSchema {
    
    private final DatabaseType databaseType;
    
    private final Collection<RuleConfiguration> configurations = new LinkedList<>();
    
    private final Collection<ShardingSphereRule> rules = new LinkedList<>();
    
    private final Map<String, DataSource> dataSources = new LinkedHashMap<>();
    
    private final Map<String, DataSourceParameter> dataSourceParameters = new LinkedHashMap<>();
    
    @Setter
    private ShardingSphereMetaData metaData;
    
    public ShardingSphereSchema(final DatabaseType databaseType, final Collection<RuleConfiguration> configurations, final Collection<ShardingSphereRule> rules, 
                                final Map<String, DataSource> dataSourceMap, final ShardingSphereMetaData shardingSphereMetaData) {
        this.databaseType = databaseType;
        this.configurations.addAll(configurations);
        this.rules.addAll(rules);
        this.dataSources.putAll(dataSourceMap);
        metaData = shardingSphereMetaData;
    }
    
    public ShardingSphereSchema(final DatabaseType databaseType, final Collection<RuleConfiguration> configurations, final Collection<ShardingSphereRule> rules,
                                final Map<String, DataSource> dataSourceMap, final Map<String, DataSourceParameter> dataSourceParameters, final ShardingSphereMetaData shardingSphereMetaData) {
        this(databaseType, configurations, rules, dataSourceMap, shardingSphereMetaData);
        this.dataSourceParameters.putAll(dataSourceParameters);
    }
    
    /**
     * Set configurations.
     *
     * @param configurations rule configurations
     */
    public void renew(final Collection<RuleConfiguration> configurations) {
        this.configurations.clear();
        this.configurations.addAll(configurations);
        Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.build(configurations, dataSources.keySet());
        this.rules.clear();
        this.rules.addAll(rules);
    }
    
    /**
     * Close data sources.
     * @param dataSources data sources
     */
    public void closeDataSources(final Collection<String> dataSources) {
        for (String each :dataSources) {
            close(this.dataSources.get(each));
            dataSourceParameters.remove(each);
        }
    }
    
    private void close(final DataSource dataSource) {
        try {
            Method method = dataSource.getClass().getDeclaredMethod("close");
            method.setAccessible(true);
            method.invoke(dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
