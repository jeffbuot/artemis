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
public class Teacher implements Serializable {

    private final String id;
    private final String name;
    private final String instituteId;

    public Teacher(String id, String name, String instituteId) {
        this.id = id;
        this.name = name;
        this.instituteId = instituteId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInstituteId() {
        return instituteId;
    }

}
