package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.util.Animations;
import com.di7ak.spaces.forum.util.SpImageGetter;


public class CommentReplyView extends LinearLayout implements View.OnClickListener {
    View view;
    View textLayout;
    boolean show;
    TextView replyTextView;
    int height;
    
    public CommentReplyView(Context context, String to, String text) {
        super(context);
        LayoutInflater li = LayoutInflater.from(context);
        view = li.inflate(R.layout.reply, this, true);
        ((TextView)view.findViewById(R.id.reply_to)).setText(to);
        view.findViewById(R.id.layout_reply).setOnClickListener(this);
        replyTextView = (TextView)view.findViewById(R.id.reply_text);
        replyTextView.setText(Html.fromHtml(text, new SpImageGetter(replyTextView), null));
        textLayout = view.findViewById(R.id.layout_reply_text);
        height = replyTextView.getMeasuredHeight();
        textLayout.getLayoutParams().height = 1;
        show = false;
    }
    
    @Override
    public void onClick(View v) {
        show = !show;
        if(show) Animations.expand(textLayout, height, 200);
        else Animations.collapse(textLayout, 200);
    }
}
