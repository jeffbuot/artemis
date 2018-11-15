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
public class DBAccount {

    protected String password;
    protected String username;
    protected String host;

    public DBAccount(String password, String username, String host) {
        this.password = password;
        this.username = username;
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }
}
