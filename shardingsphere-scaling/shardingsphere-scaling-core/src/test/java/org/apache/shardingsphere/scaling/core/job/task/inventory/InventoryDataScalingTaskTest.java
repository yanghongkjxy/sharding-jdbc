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

package org.apache.shardingsphere.scaling.core.job.task.inventory;

import org.apache.shardingsphere.scaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.SyncTaskExecuteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InventoryDataScalingTaskTest {
    
    private static final String DATA_SOURCE_URL = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "password";
    
    private SyncConfiguration syncConfiguration;
    
    private DataSourceManager dataSourceManager;
    
    @Before
    public void setUp() {
        RdbmsConfiguration dumperConfig = mockDumperConfig();
        RdbmsConfiguration importerConfig = mockImporterConfig();
        ScalingContext.getInstance().init(new ServerConfiguration());
        syncConfiguration = new SyncConfiguration(3, Collections.emptyMap(), dumperConfig, importerConfig);
        dataSourceManager = new DataSourceManager();
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test(expected = SyncTaskExecuteException.class)
    public void assertStartWithGetEstimatedRowsFailure() {
        syncConfiguration.getDumperConfiguration().setTableName("t_non_exist");
        InventoryDataScalingTask inventoryDataSyncTask = new InventoryDataScalingTask(syncConfiguration, dataSourceManager);
        inventoryDataSyncTask.start();
    }
    
    @Test
    public void assertGetProgress() throws SQLException {
        initTableData(syncConfiguration.getDumperConfiguration());
        InventoryDataScalingTask inventoryDataSyncTask = new InventoryDataScalingTask(syncConfiguration, dataSourceManager);
        inventoryDataSyncTask.start();
        assertThat(((InventoryDataSyncTaskProgress) inventoryDataSyncTask.getProgress()).getEstimatedRows(), is(2L));
    }
    
    private void initTableData(final RdbmsConfiguration dumperConfig) throws SQLException {
        DataSource dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    private RdbmsConfiguration mockDumperConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD);
        RdbmsConfiguration result = new RdbmsConfiguration();
        result.setDataSourceConfiguration(dataSourceConfiguration);
        result.setTableName("t_order");
        return result;
    }
    
    private RdbmsConfiguration mockImporterConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD);
        RdbmsConfiguration result = new RdbmsConfiguration();
        result.setDataSourceConfiguration(dataSourceConfiguration);
        return result;
    }
}
