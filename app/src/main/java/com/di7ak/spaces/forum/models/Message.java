package com.di7ak.spaces.forum.models;

public class Message {
    public static final int TYPE_MY         = 0;
    public static final int TYPE_RECEIVED   = 1;
    public static final int TYPE_SYSTEM     = 2;
    public static final int TYPE_SENDING    = 3;
    
    public String text;
    public String user;
    public String time;
    public String avatar;
    public boolean read;
    public boolean talk;
    public int type;
    public int nid;
}
