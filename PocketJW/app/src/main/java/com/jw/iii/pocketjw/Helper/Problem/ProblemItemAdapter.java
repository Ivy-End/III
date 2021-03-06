package com.jw.iii.pocketjw.Helper.Problem;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jw.iii.pocketjw.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by End on 2015/9/28.
 */
public class ProblemItemAdapter extends BaseAdapter {

    public ProblemItemAdapter(ArrayList<ProblemItem> arrayList, Context context, ImageLoader imageLoader) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (arrayList == null) {
            return 0;
        } else {
            return arrayList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (arrayList == null) {
            return null;
        } else {
            return arrayList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.listviewitem_problem, null, false);
            holder.titleTextView = (TextView)convertView.findViewById(R.id.titleTextView);
            holder.descTextView = (TextView)convertView.findViewById(R.id.descTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        if (this.arrayList != null) {
            ProblemItem problemItem = arrayList.get(position);
            if (holder.titleTextView != null) {
                holder.titleTextView.setText(problemItem.getProblemTitle());
            }
            if (holder.descTextView != null) {
                holder.descTextView.setText(problemItem.getProblemDesc());
            }
        }

        return convertView;
    }

    private class ViewHolder {
        TextView titleTextView;
        TextView descTextView;
    }

    private ArrayList<ProblemItem> arrayList;
    private Context context;
}
