package com.di7ak.spaces.forum.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONException;
import org.json.JSONObject;

public class AvatarView extends CircleImageView {
    
    public AvatarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setupData(JSONObject data, Picasso picasso) {
        try {
            if (data.has("previewURL")) {
                String previewUrl = data.getString("previewURL");
                //replace link to the larger size
                previewUrl = previewUrl.replace("41.40", "81.80");

                picasso.load(previewUrl).into(this);
            }
        } catch (JSONException e) {

        }
    }
}
