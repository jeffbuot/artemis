/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.sched_board_module;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class SerializableModel implements Serializable {

    private String roomId;
    private List<Schedule> scheds;

    public SerializableModel(String roomId, List<Schedule> scheds) {
        this.roomId = roomId;
        this.scheds = scheds;
    }

    public String getRoomId() {
        return roomId;
    }

    public List<Schedule> getScheds() {
        return scheds;
    }

}
