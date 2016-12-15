package com.di7ak.spaces.forum;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.di7ak.spaces.forum.adapters.MessageAdapter;
import com.di7ak.spaces.forum.api.Request;
import com.di7ak.spaces.forum.api.RequestListener;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.api.SpacesException;
import com.rey.material.widget.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogActivity extends AppCompatActivity implements 
        Authenticator.OnResult,
        RequestListener, View.OnClickListener,
        NotificationManager.OnNewNotification {
    Toolbar toolbar;
    ListView mMessageList;
    MessageAdapter mAdapter;
    Session mSession;
    FloatingActionButton mBtnSend;
    EditText mMessageBox;
    int mContact;
    String mAction;
    Uri mUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onBackPressed();
                }
            });
        
        mMessageList = (ListView) findViewById(R.id.messages);
        mBtnSend = (FloatingActionButton) findViewById(R.id.fab_send);
        mBtnSend.setOnClickListener(this);
        mMessageBox = (EditText) findViewById(R.id.message);
        mMessageBox.setEnabled(false);
        mMessageBox.setFocusable(false);
        mBtnSend.setEnabled(false);
        Authenticator.getSession(this, this);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Application.notificationManager.removeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Application.notificationManager.addListener(this);
    }
    
    @Override
    public void onNewNotification(JSONObject message) {
        android.util.Log.d("lol", message.toString());
        /*try {
            if (message.has("text")) {
                JSONObject text = message.getJSONObject("text");
                if (text.has("act")) {
                    int act = text.getInt("act");
                    if (act == 40) {
                        if(text.has("objectId")) {
                            int objectId = text.getInt("objectId");
                            if(objectId == mObjectId) updateComments();
                        }
                    } 
                }
            }
        } catch (JSONException e) {

        }*/
    }
    
    
    @Override
    public void onAuthenticatorResult(Session session) {
        if(session == null) finish();
        else {
            mSession = session;
            mUri = getIntent().getData();
            getMessages(mUri);
        }
	}
    
    private void getMessages(Uri uri) {
        Request request = new Request(uri);
        request.executeWithListener(this);
    }
    
    @Override
    public void onClick(View v) {
        String text = mMessageBox.getText().toString();
        mAdapter.appendMessage(text);
        mMessageBox.setText("");
        String post =     "sid=" + mSession.sid
                        + "&CK=" + mSession.ck
                        + "&texttT=" + Uri.encode(text)
                        + "&cfms=" + Uri.encode("Отправить")
                        + "&Contact=" + mContact;
        Request request = new Request(Uri.parse(mAction));
        request.setPost(post);
        request.executeWithListener(new RequestListener() {

                @Override
                public void onSuccess(JSONObject json) {
                    
                    getMessages(mUri);
                }

                @Override
                public void onError(SpacesException e) {
                    Toast.makeText(DialogActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    
                }
                
            
        });
    }
    
    @Override
    public void onSuccess(JSONObject json) {
        
        try {
            JSONObject contact = json.getJSONObject("contact_info");
            mContact = contact.getInt("nid");
            if(json.has("new_msg_form")) {
                mAction = json.getJSONObject("new_msg_form").getString("action");
                if(!mMessageBox.isFocusable()) {
                    mMessageBox.setEnabled(true);
                    mMessageBox.setFocusableInTouchMode(true);
                    mMessageBox.setFocusable(true);
                    mBtnSend.setEnabled(true);
                }
            } 
            String addr = contact.getString("text_addr");
            setTitle(addr);
            
            if(mAdapter == null) {
                mAdapter = new MessageAdapter(this, json, mSession);
                mMessageList.setAdapter(mAdapter);
                mMessageList.setDivider(null);
                mMessageList.setSelection(mAdapter.getCount() - 1);
            } else {
                mAdapter.setData(json);
                mAdapter.notifyDataSetChanged();
            }
            if(mMessageList.canScrollVertically(1)) {
                mMessageList.setSelection(mAdapter.getCount() - 1);
            }
        } catch(JSONException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(SpacesException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
