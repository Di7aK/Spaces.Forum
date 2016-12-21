package com.di7ak.spaces.forum.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import com.di7ak.spaces.forum.fragments.DialogFragment;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends BaseAdapter implements SpinnerAdapter {
    private List<DialogFragment> mDialogs;
    
    public ContactAdapter() {
        super();
        mDialogs = new ArrayList<DialogFragment>();
    }
    
    public void add(DialogFragment dialog) {
        mDialogs.add(dialog);
    }
    
    @Override
    public int getCount() {
        return mDialogs.size();
    }

    @Override
    public DialogFragment getItem(int i) {
        return mDialogs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View v, ViewGroup p) {
        return getItem(i).getTitleView(v, p);
    }
    
    @Override
    public View getDropDownView(int i, View v, ViewGroup p) {
        return getItem(i).getDropDownTitleView(v, p);
    }
}
