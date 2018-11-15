/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package net_overload_test;

import java.io.Serializable;

/**
 *
 * @author Jeff
 */
public class Data implements Serializable{
    
    private final String message;
    private final String sender;

    public Data(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
    
}
