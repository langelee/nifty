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
package com.facebook.nifty.core;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import javax.management.Attribute;

public class ConnectionContexts
{
    private static final AttributeKey<ConnectionContext> CONNECTION_CONTEXT = AttributeKey.valueOf("Thrift.ConnectionContext");

    public static ConnectionContext createContext(Channel channel) {
        NiftyConnectionContext context = new NiftyConnectionContext();
        channel.attr(CONNECTION_CONTEXT).set(context);
        return context;
    }

    public static ConnectionContext getContext(Channel channel) {
        return channel.attr(CONNECTION_CONTEXT).get();
    }
}
