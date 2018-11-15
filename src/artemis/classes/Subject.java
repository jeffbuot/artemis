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
public class Subject implements Serializable {

    private final String id;
    private final String description;
    private final String instituteId;
    private final int lec;
    private final int lab;

    public Subject(String id, String description, String instituteId, int lec, int lab) {
        this.id = id;
        this.description = description;
        this.instituteId = instituteId;
        this.lec = lec;
        this.lab = lab;
    }

    public int getLec() {
        return lec;
    }

    public int getLab() {
        return lab;
    }

    public boolean hasLab() {
        return lab > 0;
    }

    public boolean hasLect() {
        return lec > 0;
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
