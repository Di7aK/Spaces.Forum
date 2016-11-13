package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.di7ak.spaces.forum.R;
import com.di7ak.spaces.forum.api.Session;
import com.di7ak.spaces.forum.util.ImageDownloader;
import com.di7ak.spaces.forum.util.SpImageGetter;
import com.rey.material.widget.Button;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentView extends LinearLayout {
    private static final int BUTTON_TYPE_REPLY = 0;
    private static final String[] KOLYA = {
        "ЕБАТЬ ТЫ ЛАХ",
        "ЭГЕГЕГЕЕЕЕЙ ТЫ ПРОСТА ГЕЙ",
        "ВАТ ЭТА ДА ТЫ ТАК САСИРУУУУУЕШЬ",
        "ЧОООООООООООООООООО?? закрываю твою тему нахуй",
        "за то ты можешь сделать супер миниетус"
    };
    
    private OnButtonClick mListener;
    private Context mContext;
    private UserView mAuthor;
    private TextView mText;
    //private TextView mDate;
    private TextView mTime;
    private PictureAttachmentsView mPictureAttachments;
    private FileAttachmentsView mFileAttachments;
    private AvatarView mAvatar;
    private VotingView mVoting;
    private String mDate = "";
    private int mCommentId;
    private int mCommentType;

    public CommentView(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment, this, true);
        mAuthor = (UserView)view.findViewById(R.id.author);
        mText = (TextView)view.findViewById(R.id.text);
        //mDate = (TextView)view.findViewById(R.id.date);
        mTime = (TextView)view.findViewById(R.id.time);
        mAvatar = (AvatarView)view.findViewById(R.id.avatar);
        mPictureAttachments = (PictureAttachmentsView)view.findViewById(R.id.attachments);
        mFileAttachments = (FileAttachmentsView)view.findViewById(R.id.file_attachments);
        mVoting = (VotingView)view.findViewById(R.id.voting);
        
        mText.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    public int getCommentId() {
        return mCommentId;
    }
    
    public String getCommentDate() {
        if(TextUtils.isEmpty(mDate)) return "Сегодня";
        return mDate;
    }

    public void setupData(JSONObject data, Session session) {
        try {
            if (data.has("attaches_widgets") && !data.isNull("attaches_widgets")) {
                JSONObject attaches = data.getJSONObject("attaches_widgets");
                if (attaches.has("tile_items")) {
                    JSONArray items = attaches.getJSONArray("tile_items");
                    mPictureAttachments.setupData(items);
                }
                if (attaches.has("list_items")) {
                    JSONArray items = attaches.getJSONArray("list_items");
                    mFileAttachments.setupData(items);
                }
            }
            if (data.has("avatar") && !data.isNull("avatar")) {
                JSONObject avatar = data.getJSONObject("avatar");
                mAvatar.setupData(avatar);
            }
            if (data.has("comment_type")) mCommentType = data.getInt("comment_type");
            if (data.has("date")) {
                String[] date = data.getString("date").split("в ");
                if (date.length == 2) {
                    mDate = date[0].trim();
                    mTime.setText(date[1].trim());
                } else {
                    mTime.setText(date[0].trim());
                }
            }

            if (data.has("reply_user_name") && !data.isNull("reply_user_name")) {
                String replyUserName = data.getString("reply_user_name");
                if (data.has("reply_comment_text")) {
                    String replyCommentText = data.getString("reply_comment_text");
                    View reply = new CommentReplyView(mContext, replyUserName, replyCommentText);
                    ((android.widget.LinearLayout)findViewById(R.id.comment_block_right)).addView(reply, 1);
                }
            }
            String text ="";
            if (data.has("text") && !data.isNull("text")) {
                text = data.getString("text");
                mText.setText(Html.fromHtml(text, new SpImageGetter(mText), null));
            }
            if (data.has("user")) {
                JSONObject user = data.getJSONObject("user");
                mAuthor.setupData(user);
                if (user.has("siteLink")) {
                    JSONObject siteLink = user.getJSONObject("siteLink");
                    if (siteLink.has("user_name")) {
                        String userName = siteLink.getString("user_name");
                        if(userName.equals("Система")) {
                            mAuthor.setText("Коля");
                            String hash = ImageDownloader.md5("https://pp.vk.me/c521/u320362/d_fbb34cda.jpg");
                            new ImageDownloader(mContext).downloadImage("https://pp.vk.me/c521/u320362/d_fbb34cda.jpg", hash, mAvatar, null);
                            if(text.startsWith("Тема перенесена") && text.toLowerCase().contains("архив")) {
                                String[] txt = Html.fromHtml(text).toString().split(" ");
                                String[] txt2 = Html.fromHtml(text).toString().split(" в ");
                                String pattern = " %1$s перенес твою темку в %2$s";
                                Random r = new Random();
                                pattern = KOLYA[r.nextInt(KOLYA.length)] + pattern;
                                text = String.format(pattern, txt[2], txt2[txt2.length - 1]);
                                mText.setText(text);
                            }
                        }// else mAuthor.setText(userName);
                    }
                }
            }
            
            if(data.has("voting")) {
                JSONObject voting = data.getJSONObject("voting");
                mVoting.setupData(voting, session);
            }
             
            android.widget.LinearLayout buttonBlock = (android.widget.LinearLayout)findViewById(R.id.button_block);
            LayoutInflater li = LayoutInflater.from(mContext);
            View btnResponse = li.inflate(R.layout.btn_response, buttonBlock, true);
            Button btnReply = (Button)btnResponse.findViewById(R.id.btn_reply);
            btnReply.setOnClickListener(new View.OnClickListener() {

             @Override
             public void onClick(View v) {
                 mListener.onButtonClick(mCommentId, BUTTON_TYPE_REPLY);
             }
             });
             
            if (data.has("comment_id")) mCommentId = data.getInt("comment_id");
        } catch (JSONException e) {

        }
    }
    
    public void setOnButtonClickListener(OnButtonClick listener) {
        mListener = listener;
    }
    
    public interface OnButtonClick{
        public void onButtonClick(int commentId, int buttonType);
    }
}
