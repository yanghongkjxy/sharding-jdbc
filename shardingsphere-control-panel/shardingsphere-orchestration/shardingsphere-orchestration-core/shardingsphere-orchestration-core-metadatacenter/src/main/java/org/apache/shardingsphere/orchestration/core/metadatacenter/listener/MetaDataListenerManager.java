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

package org.apache.shardingsphere.orchestration.core.metadatacenter.listener;

import org.apache.shardingsphere.orchestration.center.CenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;

import java.util.Collection;

/**
 * Meta data listener manager.
 */
public class MetaDataListenerManager {
    
    private final MetaDataChangedListener metaDataChangedListener;
    
    public MetaDataListenerManager(final String name, final CenterRepository centerRepository, final Collection<String> shardingSchemaNames) {
        metaDataChangedListener = new MetaDataChangedListener(name, centerRepository, shardingSchemaNames);
    }
    
    /**
     * Initialize all metadata changed listeners.
     */
    public void initListeners() {
        metaDataChangedListener.watch(DataChangedEvent.ChangedType.UPDATED);
    }
}
