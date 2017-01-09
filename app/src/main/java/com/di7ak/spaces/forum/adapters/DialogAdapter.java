package com.di7ak.spaces.forum.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.di7ak.spaces.forum.fragments.DialogFragment;
import java.util.ArrayList;
import java.util.List;

public class DialogAdapter extends FragmentPagerAdapter {
    private List<DialogFragment> mDialogs;
    
    public DialogAdapter(FragmentManager fm) {
        super(fm);
        mDialogs = new ArrayList<DialogFragment>();
    }
    
    public int indexOf(int contact) {
        for(int i = 0; i < mDialogs.size(); i ++) {
            if(mDialogs.get(i).contact.id == contact || mDialogs.get(i).contact.talkId == contact) {
                return i;
            }
        }
        return -1;
    }
    
    public int appendDialog(DialogFragment dialog) {
        int count = mDialogs.size();
        mDialogs.add(dialog);
        return count;
    }
    
    public void removeDialog(int index) {
        mDialogs.remove(index);
    }
    
    public void removeDialog(DialogFragment dialog) {
        mDialogs.remove(dialog);
    }
    
    @Override
    public Fragment getItem(int position) {
        return mDialogs.get(position);
    }
    
    @Override
    public int getCount() {
        return mDialogs.size();
    }
}
