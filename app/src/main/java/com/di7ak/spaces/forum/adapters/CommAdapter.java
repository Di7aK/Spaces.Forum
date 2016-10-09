package com.di7ak.spaces.forum.adapters;

import android.view.*;

import android.content.Context;
import android.widget.BaseAdapter;
import com.di7ak.spaces.forum.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.TextView;
import java.util.List;
import com.di7ak.spaces.forum.api.Comm;

public class CommAdapter extends BaseAdapter {
	private Context context;
		private List<Comm> items;

        public CommAdapter(Context context, List<Comm> items) {
			this.context = context;
			this.items = items;
       }
		
		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
                v = LayoutInflater.from(context).inflate(R.layout.comm_item, null);
            }

			((TextView)v.findViewById(R.id.tv_name)).setText(items.get(position).name);
			
			return v;
		}
		
    }
