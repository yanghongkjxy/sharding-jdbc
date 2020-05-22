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

package org.apache.shardingsphere.infra.executor.sql.execute.jdbc.group;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.connection.JDBCExecutionConnection;
import org.apache.shardingsphere.infra.executor.sql.group.ExecuteGroupEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * Execute group engine for statement.
 */
public final class StatementExecuteGroupEngine extends ExecuteGroupEngine<StatementExecuteUnit, JDBCExecutionConnection, Connection, StatementOption> {
    
    public StatementExecuteGroupEngine(final int maxConnectionsSizePerQuery, final Collection<ShardingSphereRule> rules) {
        super(maxConnectionsSizePerQuery, rules);
    }
    
    @Override
    protected StatementExecuteUnit createStorageResourceExecuteUnit(final ExecutionUnit executionUnit, final JDBCExecutionConnection executionConnection, final Connection connection, 
                                                                    final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return new StatementExecuteUnit(executionUnit, createStatement(executionConnection, connection, connectionMode, option), connectionMode);
    }
    
    private Statement createStatement(final JDBCExecutionConnection executionConnection, final Connection connection,
                                      final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        return executionConnection.createStorageResource(connection, connectionMode, option);
    }
}
