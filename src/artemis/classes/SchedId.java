/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import java.io.Serializable;

/**
 *
 * @author Jefferson
 */
public class SchedId implements Serializable{
    private final int id;
    private final String day;

    public SchedId(int id, String day) {
        this.id = id;
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public String getDay() {
        return day;
    }
    
}
