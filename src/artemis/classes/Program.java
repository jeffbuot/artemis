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
public class Program implements Serializable{

    private final String id;
    private final String description;
    private final String instituteId;

    public Program(String id, String description, String instituteId) {
        this.id = id;
        this.description = description;
        this.instituteId = instituteId;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getInstituteId() {
        return instituteId;
    }

}
