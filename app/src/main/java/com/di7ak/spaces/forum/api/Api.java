package com.di7ak.spaces.forum.api;

import android.net.Uri;
import com.di7ak.spaces.forum.Application;
import com.di7ak.spaces.forum.models.Message;

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
    
    public static Request sendMessage(Message message) {
        StringBuilder args = new StringBuilder();
        args.append("method=").append("sendMessage")
            .append("&Contact=").append(Integer.toString(message.contact))
            .append("&sid=").append(Uri.encode(Application.getSession().sid))
            .append("&CK=").append(Uri.encode(Application.getSession().ck))
            .append("&texttT=").append(Uri.encode(message.text));
            
        Request request = new Request(Uri.parse("http://spaces.ru/neoapi/mail/"));
        request.setPost(args.toString());
        return request;
    }
    
    public static Request markAsRead(int contact) {
        StringBuilder args = new StringBuilder();
        args.append("method=").append("markContactsAsRead")
            .append("&CoNtacts=").append(Integer.toString(contact))
            .append("&sid=").append(Uri.encode(Application.getSession().sid))
            .append("&CK=").append(Uri.encode(Application.getSession().ck));
        Request request = new Request(Uri.parse("http://spaces.ru/neoapi/mail/"));
        request.setPost(args.toString());
        return request;
    }
}
