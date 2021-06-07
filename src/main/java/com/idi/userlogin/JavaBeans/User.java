package com.idi.userlogin.JavaBeans;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class User {

    private String name;
    private int id;
    public static String COMP_NAME;

    static {
        try {
            COMP_NAME = Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public User(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static String getCompName() {
        return COMP_NAME;
    }

}
