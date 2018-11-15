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
public class QueryData implements Serializable {

    private final String id;
    private final String desc;
    private final String query;
    private String remarks;
    private boolean success = false;

    public QueryData(String id, String desc, String query) {
        this.id = id;
        this.desc = desc;
        this.query = query;
    }

    public QueryData(String id, String desc, String query, String remarks) {
        this.id = id;
        this.desc = desc;
        this.query = query;
        this.remarks = remarks;
    }

    public QueryData(String id, String desc, String query, String remarks,boolean success) {
        this.success = success;
        this.id = id;
        this.desc = desc;
        this.query = query;
        this.remarks = remarks;
    }
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public String getQuery() {
        return query;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
