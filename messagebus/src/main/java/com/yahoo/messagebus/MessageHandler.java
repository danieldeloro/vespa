// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.messagebus;

/**
 * All classes that wants to handle messages that move through the messagebus need to implement this interface.
 * As opposed to the {@link ReplyHandler} which handles replies as they return from the receiver to the sender, this
 * interface is intended for handling messages as they travel from the sender to the receiver.
 *
 * @author <a href="mailto:simon@yahoo-inc.com">Simon Thoresen</a>
 */
public interface MessageHandler {

    /**
     * This function is called when a message arrives.
     *
     * @param message The message that arrived.
     */
    public void handleMessage(Message message);
}
