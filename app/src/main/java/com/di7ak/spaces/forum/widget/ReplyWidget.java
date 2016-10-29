package com.di7ak.spaces.forum.widget;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.util.Animations;
import com.di7ak.spaces.forum.util.PicassoImageGetter;
import com.squareup.picasso.Picasso;

public class ReplyWidget implements View.OnClickListener {
    View view;
    View textLayout;
    boolean show;
    TextView replyTextView;
    int height;
    
    public ReplyWidget(Activity activity, String to, String text, Picasso picasso) {
        LayoutInflater li = activity.getLayoutInflater();
        view = li.inflate(R.layout.reply, null);
        ((TextView)view.findViewById(R.id.reply_to)).setText(to);
        view.findViewById(R.id.layout_reply).setOnClickListener(this);
        replyTextView = (TextView)view.findViewById(R.id.reply_text);
        replyTextView.setText(Html.fromHtml(text, new PicassoImageGetter(replyTextView, activity.getResources(), picasso), null));
        textLayout = view.findViewById(R.id.layout_reply_text);
        height = replyTextView.getMeasuredHeight();
        textLayout.getLayoutParams().height = 1;
        show = false;
    }
    
    public View getView() {
        return view;
    }
    
    @Override
    public void onClick(View v) {
        show = !show;
        if(show) Animations.expand(textLayout, height, 200);
        else Animations.collapse(textLayout, 200);
    }
    
}
