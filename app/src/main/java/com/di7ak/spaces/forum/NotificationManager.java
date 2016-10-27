package com.di7ak.spaces.forum;

import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationManager extends WebSocketClient {
    String channel;
    List<OnNewNotification> listeners;

    public NotificationManager(String channel) throws URISyntaxException {
        super(new URI("ws://lp03.spaces.ru/ws/" + channel), new Draft_10());
        this.channel = channel;
        listeners = new ArrayList<OnNewNotification>();
        super.connect();
    }
    
    public void addListener(OnNewNotification listener) {
        synchronized(listener) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(OnNewNotification listener) {
        synchronized(listener) {
            listeners.remove(listener);
        }
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        
    }

    @Override
    public void onMessage(String message) {
        
        try {
            JSONObject json = new JSONObject(message);
            for(OnNewNotification listener : listeners) {
                listener.onNewNotification(json);
            }
        } catch (JSONException e) {
            
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        
    }

    @Override
    public void onError(Exception ex) {
        
    }

    public interface OnNewNotification {
        public void onNewNotification(JSONObject message);
    }
}

