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

package org.apache.shardingsphere.kernal.context;

import lombok.Getter;
import org.apache.shardingsphere.kernal.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernal.context.schema.DataSourceParameter;
import org.apache.shardingsphere.kernal.context.schema.ShardingSphereSchema;

import javax.sql.DataSource;
import java.util.Map;

@Getter
public final class SchemaContext {
    
    private final ShardingSphereSchema schema;
    
    private final RuntimeContext runtimeContext;
    
    public SchemaContext(final ShardingSphereSchema schema, final RuntimeContext runtimeContext) {
        this.schema = schema;
        this.runtimeContext = runtimeContext;
    }
    
    /**
     * Renew data sources.
     *
     * @param dataSourceParameters data source parameters
     * @param dataSources data sources
     * @throws Exception exception
     */
    public void renew(final Map<String, DataSourceParameter> dataSourceParameters, final Map<String, DataSource> dataSources) throws Exception {
        schema.getDataSources().clear();
        schema.getDataSources().putAll(dataSources);
        schema.getDataSourceParameters().clear();
        schema.getDataSourceParameters().putAll(dataSourceParameters);
        runtimeContext.getShardingTransactionManagerEngine().close();
        runtimeContext.getShardingTransactionManagerEngine().init(schema.getDatabaseType(), schema.getDataSources());
    }
}
