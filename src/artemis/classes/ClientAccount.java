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
public class ClientAccount implements Serializable {

    private final int id;
    private final String firstname;
    private final String lastname;
    private final String gender;
    private final String accessType;
    private final String institute;
    private final String instituteDescription;
    private final String accessStatus;
    private final String username;
    private final String password;
    private final String sy;
    private boolean canLogin = true;

    public ClientAccount(int id, String firstname, String lastname, String gender,
            String accessType, String institute, String accessStatus, String username,
            String password, String sy,String instituteDescription) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.gender = gender;
        this.accessType = accessType;
        this.institute = institute;
        this.accessStatus = accessStatus;
        this.username = username;
        this.password = password;
        this.sy = sy;
        this.instituteDescription = instituteDescription;
    }

    public String getInstituteDescription() {
        return instituteDescription;
    }

    public String getSy() {
        return sy;
    }

    public String getInfo() {
        return String.format("<html>"
                + "<b>Userame:</b>%s<br/>"
                + "<b>Fullname:</b>%s<br/>"
                + "<b>Gender:</b>%s</br>"
                + "<b>Access Type:</b>%s</br>"
                + "<b>Institute:</b>%s</br>"
                + "</html>", username, getName(), gender, accessType, institute);
    }

    public boolean isCanLogin() {
        return canLogin;
    }

    public void setCanLogin(boolean canLogin) {
        this.canLogin = canLogin;
    }

    public String getName() {
        return this.firstname + " " + this.lastname;
    }

    public String getAdminName() {
        return this.username + "(" + this.institute + ")";
    }

    public int getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getGender() {
        return gender;
    }

    public String getAccessType() {
        return accessType;
    }

    public String getInstitute() {
        return institute;
    }

    public String getAccessStatus() {
        return accessStatus;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
