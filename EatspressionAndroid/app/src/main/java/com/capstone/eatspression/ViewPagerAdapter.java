package com.capstone.eatspression;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> imageList;
    private ArrayList<Uri> mData;
    private boolean customize;
    public ViewPagerAdapter(Context context, ArrayList<String> imageList, ArrayList<Uri> mData, boolean customize)
    {
        this.mContext = context;
        this.imageList = imageList;
        this.mData = mData;
        this.customize = customize;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide_item, null);

        ImageView imageView = view.findViewById(R.id.imageView);
        if (customize) {
            Glide.with(container).load(mData.get(position)).into(imageView);
        } else {
            Glide.with(container).load(imageList.get(position)).into(imageView);
        }
        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        if (customize)
            return mData.size();
        else
            return imageList.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == (View)o);
    }
}
