// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.messagebus.shared;

import com.yahoo.jrt.ListenFailedException;
import com.yahoo.jrt.slobrok.server.Slobrok;
import com.yahoo.messagebus.MessageBusParams;
import com.yahoo.messagebus.network.rpc.RPCNetworkParams;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:simon@yahoo-inc.com">Simon Thoresen</a>
 */
public class SharedMessageBusTestCase {

    @Test
    public void requireThatMbusCanNotBeNull() {
        try {
            new SharedMessageBus(null);
            fail();
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void requireThatMbusIsClosedOnDestroy() throws ListenFailedException {
        Slobrok slobrok = new Slobrok();
        SharedMessageBus mbus = SharedMessageBus.newInstance(new MessageBusParams(),
                                                             new RPCNetworkParams()
                                                                     .setSlobrokConfigId(slobrok.configId()));
        mbus.release();
        assertFalse(mbus.messageBus().destroy());
    }
}
