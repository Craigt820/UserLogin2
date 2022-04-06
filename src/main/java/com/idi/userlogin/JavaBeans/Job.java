package com.idi.userlogin.JavaBeans;

public class Job {
    private String job_id;
    private String project;
    private int id;
    private boolean userEntry;
    private boolean complete;
    private String folder_Struct;
    private int loc_id;
    private int client_id;
    private String uid;
    private String groupCol;
    private String description;

    public Job(int id, String job_id, String project, boolean entry, boolean complete, String uid, String groupCol) {
        this.id = id;
        this.job_id = job_id;
        this.project = project;
        this.uid = uid;
        this.groupCol = groupCol;
        this.userEntry = entry;
        this.complete = complete;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isUserEntry() {
        return userEntry;
    }

    public void setUserEntry(boolean userEntry) {
        this.userEntry = userEntry;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getFolder_Struct() {
        return folder_Struct;
    }

    public void setFolder_Struct(String folder_Struct) {
        this.folder_Struct = folder_Struct;
    }

    public int getLoc_id() {
        return loc_id;
    }

    public void setLoc_id(int loc_id) {
        this.loc_id = loc_id;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGroupCol() {
        return groupCol;
    }

    public void setGroupCol(String groupCol) {
        this.groupCol = groupCol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
