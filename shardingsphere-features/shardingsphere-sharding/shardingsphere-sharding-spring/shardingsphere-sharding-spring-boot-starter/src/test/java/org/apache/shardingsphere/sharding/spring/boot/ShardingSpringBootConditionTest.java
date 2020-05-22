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

package org.apache.shardingsphere.sharding.spring.boot;

import org.apache.shardingsphere.sharding.spring.boot.condition.ShardingSpringBootCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSpringBootConditionTest {
    
    @Test
    public void assertNotMatch() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.rules.encrypt.encryptors.encryptor_aes.type", "aes");
        ConditionContext context = Mockito.mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = Mockito.mock(AnnotatedTypeMetadata.class);
        when(context.getEnvironment()).thenReturn(mockEnvironment);
        ShardingSpringBootCondition condition = new ShardingSpringBootCondition();
        ConditionOutcome matchOutcome = condition.getMatchOutcome(context, metadata);
        assertThat(matchOutcome.isMatch(), is(false));
    }
    
    @Test
    public void assertMatch() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.rules.sharding.binding-tables", "t_order");
        ConditionContext context = Mockito.mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = Mockito.mock(AnnotatedTypeMetadata.class);
        when(context.getEnvironment()).thenReturn(mockEnvironment);
        ShardingSpringBootCondition condition = new ShardingSpringBootCondition();
        ConditionOutcome matchOutcome = condition.getMatchOutcome(context, metadata);
        assertThat(matchOutcome.isMatch(), is(true));
    }
}

