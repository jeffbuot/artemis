/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import artemis.sched_board_module.ScheduleRequestData;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jefferson
 */
public class PermissionRequest implements Serializable {

    private String id;
    private String date;
    private String note;
    private ClientAccount sender;
    private ClientAccount receiver;
    private String instituteToBeSent;
    private List<ScheduleRequestData> srd;
    private List<QueryData> qd;
    private String status = "Pending";

    public PermissionRequest(String id, String date, String note, ClientAccount sender,
            String instituteToBeSent, List<ScheduleRequestData> srd) {
        this.id = id;
        this.date = date;
        this.note = note;
        this.sender = sender;
        this.instituteToBeSent = instituteToBeSent;
        this.srd = srd;
    }

    public PermissionRequest(String id, String date, String note, ClientAccount sender, 
            ClientAccount receiver, String instituteToBeSent, List<ScheduleRequestData> srd,String status) {
        this.id = id;
        this.date = date;
        this.note = note;
        this.sender = sender;
        this.receiver = receiver;
        this.instituteToBeSent = instituteToBeSent;
        this.srd = srd;
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ClientAccount getSender() {
        return sender;
    }

    public void setSender(ClientAccount sender) {
        this.sender = sender;
    }

    public ClientAccount getReceiver() {
        return receiver;
    }

    public void setReceiver(ClientAccount receiver) {
        this.receiver = receiver;
    }

    public String getInstituteToBeSent() {
        return instituteToBeSent;
    }

    public void setInstituteToBeSent(String instituteToBeSent) {
        this.instituteToBeSent = instituteToBeSent;
    }

    public List<ScheduleRequestData> getSrd() {
        return srd;
    }

    public void setSrd(List<ScheduleRequestData> srd) {
        this.srd = srd;
    }

    public List<QueryData> getQd() {
        return qd;
    }

    public void setQd(List<QueryData> qd) {
        this.qd = qd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
