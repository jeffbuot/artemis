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
public class Remark implements Serializable{
    final String remarks;
    final boolean success;

    public Remark(String remarks, boolean success) {
        this.remarks = remarks;
        this.success = success;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isSuccess() {
        return success;
    }
    
}
