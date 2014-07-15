/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.nifty.client;

import com.facebook.nifty.core.NettyConfigBuilderBase;
import com.facebook.nifty.core.NiftyTimer;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.util.Timer;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.Executors.newCachedThreadPool;

/*
 * Hooks for configuring various parts of Netty.
 */
public class NettyClientConfigBuilder extends NettyConfigBuilderBase<NettyClientConfigBuilder>
{
    private HostAndPort defaultSocksProxyAddress = null;

    private final SocketChannelConfig socketChannelConfig = (SocketChannelConfig) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{SocketChannelConfig.class},
            new Magic("")
    );

    @Inject
    public NettyClientConfigBuilder()
    {
        // Thrift turns TCP_NODELAY by default, and turning it off can have latency implications
        // so let's turn it on by default as well. It can still be switched off by explicitly
        // calling setTcpNodelay(false) after construction.
        getSocketChannelConfig().setTcpNoDelay(true);
    }

    /**
     * Returns an implementation of {@link io.netty.channel.socket.SocketChannelConfig} which will
     * be applied to all {@link io.netty.channel.socket.nio.NioSocketChannel} instances created for
     * client connections.
     *
     * @return A mutable {@link io.netty.channel.socket.SocketChannelConfig}
     */
    public SocketChannelConfig getSocketChannelConfig()
    {
        return socketChannelConfig;
    }

    /**
     * A default SOCKS proxy address for client connections. Defaults to {@code null} if not
     * supplied.
     *
     * @param defaultSocksProxyAddress The address of the SOCKS proxy server
     * @return This builder
     */
    public NettyClientConfigBuilder setDefaultSocksProxyAddress(HostAndPort defaultSocksProxyAddress)
    {
        this.defaultSocksProxyAddress = defaultSocksProxyAddress;
        return this;
    }

    public NettyClientConfig build()
    {
        Timer timer = getTimer();
        int workerThreadCount = getWorkerThreadCount();

        return new NettyClientConfig(
                getBootstrapOptions(),
                defaultSocksProxyAddress,
                timer != null ? timer : new NiftyTimer(threadNamePattern("")),
                workerThreadCount
        );
    }


    private String threadNamePattern(String suffix)
    {
        String niftyName = getNiftyName();
        return "nifty-client" + (Strings.isNullOrEmpty(niftyName) ? "" : "-" + niftyName) + suffix;
    }

    private ThreadFactory renamingDaemonThreadFactory(String nameFormat)
    {
        return new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build();
    }
}
