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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MySQLComQueryPacketExecutorTest {
    
    @Mock
    private SQLException sqlException;
    
    @Mock
    private TextProtocolBackendHandler textProtocolBackendHandler;
    
    private final MySQLComQueryPacketExecutor mysqlComQueryPacketExecutor = new MySQLComQueryPacketExecutor(mock(MySQLComQueryPacket.class), null);
    
    @Test
    @SneakyThrows
    public void assertIsErrorResponse() {
        FieldSetter.setField(mysqlComQueryPacketExecutor, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
        when(sqlException.getCause()).thenReturn(new Exception());
        when(textProtocolBackendHandler.execute()).thenReturn(new ErrorResponse(sqlException));
        mysqlComQueryPacketExecutor.execute();
        assertThat(mysqlComQueryPacketExecutor.isErrorResponse(), Matchers.is(true));
    }
    
    @Test
    @SneakyThrows
    public void assertIsUpdateResponse() {
        FieldSetter.setField(mysqlComQueryPacketExecutor, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
        when(textProtocolBackendHandler.execute()).thenReturn(new UpdateResponse());
        mysqlComQueryPacketExecutor.execute();
        assertThat(mysqlComQueryPacketExecutor.isUpdateResponse(), Matchers.is(true));
    }
    
    @Test
    @SneakyThrows
    public void assertIsQuery() {
        FieldSetter.setField(mysqlComQueryPacketExecutor, MySQLComQueryPacketExecutor.class.getDeclaredField("textProtocolBackendHandler"), textProtocolBackendHandler);
        when(textProtocolBackendHandler.execute()).thenReturn(new QueryResponse(Collections.singletonList(mock(QueryHeader.class))));
        mysqlComQueryPacketExecutor.execute();
        assertThat(mysqlComQueryPacketExecutor.isQuery(), Matchers.is(true));
    }
}
