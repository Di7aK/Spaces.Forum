package com.di7ak.spaces.forum.api;

import android.net.Uri;
import java.util.List;
import com.di7ak.spaces.forum.Application;

public class Api {
    
    public static Request getMessageById(int contact, int id) {
        String post =     "sid=" + Application.getSession().sid
            + "&MeSsages=" + id
            + "&Pag=0"
            + "&method=getMessagesByIds"
            + "&Contact=" + contact;
        Request request = new Request(Uri.parse("http://spaces.ru/neoapi/mail/"));
        request.setPost(post);
        return request;
    }
}
