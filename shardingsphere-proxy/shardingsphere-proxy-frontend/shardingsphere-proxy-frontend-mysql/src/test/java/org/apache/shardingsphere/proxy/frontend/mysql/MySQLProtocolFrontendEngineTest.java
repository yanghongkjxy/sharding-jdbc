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

package org.apache.shardingsphere.proxy.frontend.mysql;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConnectionPhase;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.context.ShardingSphereProxyContext;
import org.apache.shardingsphere.proxy.frontend.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.mysql.auth.MySQLAuthenticationEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLProtocolFrontendEngineTest {
    
    private MySQLProtocolFrontendEngine mysqlProtocolFrontendEngine;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private MySQLPacketPayload payload;

    @Mock
    private Channel channel;
    
    @Before
    @SneakyThrows
    public void resetConnectionIdGenerator() {
        Field field = ConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(ConnectionIdGenerator.getInstance(), 0);
        mysqlProtocolFrontendEngine = new MySQLProtocolFrontendEngine();
    }
    
    @Test
    public void assertHandshake() {
        mysqlProtocolFrontendEngine.getAuthEngine().handshake(context, mock(BackendConnection.class));
        verify(context).writeAndFlush(isA(MySQLHandshakePacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginSuccess() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ProxyUser proxyUser = new ProxyUser("", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        assertTrue(mysqlProtocolFrontendEngine.getAuthEngine().auth(context, payload, mock(BackendConnection.class)));
        verify(context).writeAndFlush(isA(MySQLOKPacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginFailure() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ProxyUser proxyUser = new ProxyUser("error", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 3307));
        when(context.channel()).thenReturn(channel);
        assertTrue(mysqlProtocolFrontendEngine.getAuthEngine().auth(context, payload, mock(BackendConnection.class)));
        verify(context).writeAndFlush(isA(MySQLErrPacket.class));
    }

    @Test
    @SneakyThrows
    public void assertErrorMsgWhenLoginFailure() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ProxyUser proxyUser = new ProxyUser("error", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(context.channel()).thenReturn(channel);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByAddress(new byte[] {(byte) 192, (byte) 168, (byte) 0, (byte) 102}), 3307));
        assertTrue(mysqlProtocolFrontendEngine.getAuthEngine().auth(context, payload, mock(BackendConnection.class)));
        verify(context).writeAndFlush(Mockito.argThat(
                (ArgumentMatcher<MySQLErrPacket>) argument -> argument.getErrorMessage().equals("Access denied for user 'root'@'192.168.0.102' (using password: YES)")));
    }
    
    @SneakyThrows
    private void setAuthentication(final ProxyUser proxyUser) {
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        Field field = ShardingSphereProxyContext.class.getDeclaredField("authentication");
        field.setAccessible(true);
        field.set(ShardingSphereProxyContext.getInstance(), authentication);
    }
    
    @SneakyThrows
    private void setConnectionPhase(final MySQLConnectionPhase connectionPhase) {
        Field field = MySQLAuthenticationEngine.class.getDeclaredField("connectionPhase");
        field.setAccessible(true);
        field.set(mysqlProtocolFrontendEngine.getAuthEngine(), connectionPhase);
    }
}
