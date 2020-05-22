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

package org.apache.shardingsphere.scaling.core.execute.engine;

import org.apache.shardingsphere.scaling.core.execute.executor.ShardingScalingExecutor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

/**
 * Sharding scaling executor engine.
 */
public class ShardingScalingExecuteEngine {
    
    private final ListeningExecutorService executorService;
    
    public ShardingScalingExecuteEngine(final int maxWorkerNumber) {
        this.executorService = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(maxWorkerNumber, maxWorkerNumber, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy()));
    }
    
    /**
     * Submit a {@code ShardingScalingExecutor} without callback to execute.
     *
     * @param shardingScalingExecutor sharding scaling executor
     * @return execute future
     */
    public Future submit(final ShardingScalingExecutor shardingScalingExecutor) {
        return executorService.submit(shardingScalingExecutor);
    }
    
    /**
     * Submit a {@code ShardingScalingExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param shardingScalingExecutor sharding scaling executor
     * @param executeCallback execute callback
     * @return execute future
     */
    public Future submit(final ShardingScalingExecutor shardingScalingExecutor, final ExecuteCallback executeCallback) {
        ListenableFuture result = executorService.submit(shardingScalingExecutor);
        Futures.addCallback(result, new ExecuteFutureCallback<>(executeCallback));
        return result;
    }
    
    /**
     * Submit a collection of {@code ShardingScalingExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param shardingScalingExecutors sharding scaling executor
     * @param executeCallback execute callback
     * @return execute future of all
     */
    public Future submitAll(final Collection<? extends ShardingScalingExecutor> shardingScalingExecutors, final ExecuteCallback executeCallback) {
        Collection<ListenableFuture<Object>> listenableFutures = new ArrayList<>(shardingScalingExecutors.size());
        for (ShardingScalingExecutor each : shardingScalingExecutors) {
            ListenableFuture listenableFuture = executorService.submit(each);
            listenableFutures.add(listenableFuture);
        }
        ListenableFuture result = Futures.allAsList(listenableFutures);
        Futures.addCallback(result, new ExecuteFutureCallback<Collection<Object>>(executeCallback));
        return result;
    }
    
    @RequiredArgsConstructor
    private static class ExecuteFutureCallback<V> implements FutureCallback<V> {
        
        private final ExecuteCallback executeCallback;
        
        @Override
        public void onSuccess(final V result) {
            executeCallback.onSuccess();
        }
    
        @Override
        public void onFailure(final Throwable throwable) {
            executeCallback.onFailure(throwable);
        }
    }
}
