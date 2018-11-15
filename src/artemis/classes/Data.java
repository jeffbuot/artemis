/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import artemis.chat_module.SerializableImage;
import artemis.sched_board_module.Cell;
import artemis.sched_board_module.Room;
import artemis.sched_board_module.ScheduleBoardUpdater;
import artemis.sched_board_module.ScheduleRequestData;
import artemis.sched_board_module.SerializableModel;
import artemis.view_teachers_load.InstituteTeacherLoad;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Jeff
 * @author Capslock
 */
public class Data implements Serializable {

    private final int type;
    private String query;
    private String sender;
    private Message message;
    private List<QueryData> queryData;
    private List<Room> rooms;
    private Cell cell;
    private SerializableImage img;
    private List<SerializableModel> scheduleModel;
    private ScheduleBoardUpdater clientUpdater;
    private ClientAccount account;
    private List<ScheduleRequestData> srd;
    private List<PermissionRequest> PRList;
    private InstituteTeacherLoad instituteTeacherLoad;

    public Data(int type, List<ScheduleRequestData> srd, int dummy) {
        this.type = type;
        this.srd = srd;
    }

    public Data(int type, List<PermissionRequest> prlist, boolean dummy) {
        this.type = type;
        this.PRList = prlist;
    }

    public Data(int type, String query) {
        this.type = type;
        this.query = query;
    }

    public Data(int type, ScheduleBoardUpdater clientUpdater, InstituteTeacherLoad instituteTeacherLoad) {
        this.type = type;
        this.clientUpdater = clientUpdater;
        this.instituteTeacherLoad = instituteTeacherLoad;
    }

    public Data(int type, String dummy, List<SerializableModel> scheduleModel) {
        this.type = type;
        this.scheduleModel = scheduleModel;
    }

    public Data(int type, List<QueryData> q, ClientAccount acc) {
        this.type = type;
        this.queryData = q;
        this.account = acc;
    }

    public Data(int type, SerializableImage img) {
        this.type = type;
        this.img = img;
    }

    public Data(int type, Message message) {
        this.message = message;
        this.type = type;
    }

    public Data(int type, Cell cell) {
        this.type = type;
        this.cell = cell;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Data(int type) {
        this.type = type;
    }

    public Data(int type, List<Room> r) {
        this.type = type;
        this.rooms = r;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public SerializableImage getImg() {
        return img;
    }

    public Message getMessage() {
        return message;
    }

    public List<SerializableModel> getScheduleModel() {
        return scheduleModel;
    }

    public String getQuery() {
        return query;
    }

    public ScheduleBoardUpdater getClientUpdater() {
        return clientUpdater;
    }

    public String getSender() {
        return sender;
    }

    public List<QueryData> getQueryData() {
        return queryData;
    }

    public int getType() {
        return type;
    }

    public ClientAccount getAccount() {
        return account;
    }

    public List<ScheduleRequestData> getSrd() {
        return srd;
    }

    public List<PermissionRequest> getPRList() {
        return PRList;
    }

    public InstituteTeacherLoad getInstituteTeacherLoad() {
        return instituteTeacherLoad;
    }

}
