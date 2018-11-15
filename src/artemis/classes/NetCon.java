/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

/**
 *
 * @author Jeff
 * @author Capslock
 */
public class NetCon {

    protected String host;
    protected int port;

    public NetCon(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
