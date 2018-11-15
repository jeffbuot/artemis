/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.client_module;

import artemis.classes.QueryData;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class RequestLogData {

    private final List<QueryData> q;
    private final String date;
    private final String time;
    private final String desc;

    public RequestLogData(List<QueryData> q, String date, String time, String desc) {
        this.q = q;
        this.date = date;
        this.time = time;
        this.desc = desc;
    }

    public List<QueryData> getQ() {
        return q;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDesc() {
        return desc;
    }

}
