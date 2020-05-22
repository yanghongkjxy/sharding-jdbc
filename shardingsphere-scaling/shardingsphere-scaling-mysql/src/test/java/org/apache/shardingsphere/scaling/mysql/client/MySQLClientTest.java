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

package org.apache.shardingsphere.scaling.mysql.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.Promise;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComBinlogDumpCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog.MySQLComRegisterSlaveCommandPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.scaling.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetSocketAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLClientTest {
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    private InetSocketAddress inetSocketAddress;
    
    private MySQLClient mysqlClient;
    
    @Before
    public void setUp() {
        mysqlClient = new MySQLClient(1, "host", 3306, "username", "password");
        when(channel.pipeline()).thenReturn(pipeline);
        inetSocketAddress = new InetSocketAddress("host", 3306);
        when(channel.localAddress()).thenReturn(inetSocketAddress);
    }
    
    @Test
    public void assertConnect() throws NoSuchFieldException, IllegalAccessException {
        final ServerInfo expected = new ServerInfo();
        mockChannelResponse(expected);
        mysqlClient.connect();
        ServerInfo actual = ReflectionUtil.getFieldValueFromClass(mysqlClient, "serverInfo", ServerInfo.class);
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertExecute() throws NoSuchFieldException, IllegalAccessException {
        mockChannelResponse(new MySQLOKPacket(0));
        ReflectionUtil.setFieldValueToClass(mysqlClient, "channel", channel);
        assertTrue(mysqlClient.execute(""));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertExecuteUpdate() throws NoSuchFieldException, IllegalAccessException {
        MySQLOKPacket expected = new MySQLOKPacket(0, 10, 0);
        ReflectionUtil.setFieldValueToClass(expected, "affectedRows", 10);
        mockChannelResponse(expected);
        ReflectionUtil.setFieldValueToClass(mysqlClient, "channel", channel);
        assertThat(mysqlClient.executeUpdate(""), is(10));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertExecuteQuery() throws NoSuchFieldException, IllegalAccessException {
        InternalResultSet expected = new InternalResultSet(null);
        mockChannelResponse(expected);
        ReflectionUtil.setFieldValueToClass(mysqlClient, "channel", channel);
        assertThat(mysqlClient.executeQuery(""), is(expected));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertSubscribeBelow56Version() throws NoSuchFieldException, IllegalAccessException {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServerVersion(new ServerVersion("5.5.0-log"));
        ReflectionUtil.setFieldValueToClass(mysqlClient, "serverInfo", serverInfo);
        ReflectionUtil.setFieldValueToClass(mysqlClient, "channel", channel);
        mockChannelResponse(new MySQLOKPacket(0));
        mysqlClient.subscribe("", 4L);
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComRegisterSlaveCommandPacket.class));
        verify(channel).writeAndFlush(ArgumentMatchers.any(MySQLComBinlogDumpCommandPacket.class));
    }
    
    @SuppressWarnings("unchecked")
    private void mockChannelResponse(final Object response) {
        new Thread(() -> {
            while (true) {
                Promise responseCallback = null;
                try {
                    responseCallback = ReflectionUtil.getFieldValueFromClass(mysqlClient, "responseCallback", Promise.class);
                } catch (final NoSuchFieldException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    Thread.currentThread().interrupt();
                }
                if (null != responseCallback) {
                    responseCallback.setSuccess(response);
                    break;
                }
            }
        }).start();
    }
}
