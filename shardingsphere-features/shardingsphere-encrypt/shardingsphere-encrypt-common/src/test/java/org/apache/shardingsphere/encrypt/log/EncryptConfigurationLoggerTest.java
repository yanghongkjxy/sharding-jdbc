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

package org.apache.shardingsphere.encrypt.log;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorRuleConfiguration;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptConfigurationLoggerTest {
    
    @Mock
    private Logger log;
    
    @Before
    @SneakyThrows
    public void setLog() {
        setFinalStaticField(ConfigurationLogger.class.getDeclaredField("log"), log);
    }
    
    @SneakyThrows
    private void setFinalStaticField(final Field field, final Object newValue) {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
    
    @Test
    public void assertLogEncryptRuleConfiguration() {
        String yaml = "rules:\n"
                + "- !ENCRYPT\n"
                + "  encryptors:\n"
                + "    encryptor_aes:\n"
                + "      props:\n"
                + "        aes.key.value: 123456abc\n"
                + "      type: aes\n"
                + "  tables:\n"
                + "    t_encrypt:\n"
                + "      columns:\n"
                + "        user_id:\n"
                + "          assistedQueryColumn: user_assisted\n"
                + "          cipherColumn: user_encrypt\n"
                + "          encryptor: encryptor_aes\n"
                + "          plainColumn: user_decrypt\n";
        assertLogInfo(yaml);
        ConfigurationLogger.log(Collections.singletonList(getEncryptRuleConfiguration()));
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        Properties properties = new Properties();
        properties.put("aes.key.value", "123456abc");
        EncryptorRuleConfiguration encryptorRuleConfiguration = new EncryptorRuleConfiguration("aes", properties);
        EncryptTableRuleConfiguration tableRuleConfiguration =
                new EncryptTableRuleConfiguration(Collections.singletonMap("user_id", new EncryptColumnRuleConfiguration("user_decrypt", "user_encrypt", "user_assisted", "encryptor_aes")));
        return new EncryptRuleConfiguration(ImmutableMap.of("encryptor_aes", encryptorRuleConfiguration), ImmutableMap.of("t_encrypt", tableRuleConfiguration));
    }
    
    private void assertLogInfo(final String logContent) {
        doAnswer(invocationOnMock -> {
            assertThat(invocationOnMock.getArgument(1).toString(), is("Rule configurations: "));
            assertThat(invocationOnMock.getArgument(2).toString(), is(logContent));
            return null;
        }).when(log).info(anyString(), anyString(), anyString());
    }
}
