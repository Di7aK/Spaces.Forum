package com.di7ak.spaces.forum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import java.util.HashMap;
import java.util.List;

public class ViewerActivity extends AppCompatActivity {
    SliderLayout mDemoSlider;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.gallery);
        mDemoSlider = (SliderLayout)findViewById(R.id.slider);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        List<String> names = extra.getStringArrayList("names");
        List<String> urls = extra.getStringArrayList("urls");
        String current = extra.getString("current");
        int index = urls.size() - urls.indexOf(current) - 1;
        HashMap<String,String> url_maps = new HashMap<String, String>();
        for(int i = names.size() - 1; i >= 0; i --) {
            url_maps.put(names.get(i), urls.get(i));
        }
        for(String name : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.CenterInside);
                    //.setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                        .putString("extra",name);

           mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Top);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setBackgroundColor(0xff000000);
        mDemoSlider.stopAutoCycle();
        mDemoSlider.setCurrentPosition(index);
        
        //mDemoSlider.setDuration(4000);
        //mDemoSlider.addOnPageChangeListener(this);
    }
}
