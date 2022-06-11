package com.capstone.eatspression;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<String> imageList;
    private ArrayList<Uri> mData;
    private boolean customize;
    private boolean isTest;
    public ViewPagerAdapter(Context context, ArrayList<String> imageList, ArrayList<Uri> mData, boolean customize)
    {
        this.mContext = context;
        this.imageList = imageList;
        this.mData = mData;
        this.customize = customize;
        this.isTest = true;
        Log.i("tag", "is Test: " + isTest);
    }
    public ViewPagerAdapter(Context context, ArrayList<String> imageList, ArrayList<Uri> mData, boolean customize, boolean isTest)
    {
        this.mContext = context;
        this.imageList = imageList;
        this.mData = mData;
        this.customize = customize;
        this.isTest = isTest;
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide_item, null);
        Log.i("tag", "position: " + position);
        ImageView imgView = view.findViewById(R.id.imageViewSlide);
        if (isTest) {
            if (position < 3) {
                switch (position) {
                    case 0:
                        Glide.with(container).load(R.drawable.count_3).into(imgView);
                        break;
                    case 1:
                        Glide.with(container).load(R.drawable.count_2).into(imgView);
                        break;
                    case 2:
                        Glide.with(container).load(R.drawable.count_1).into(imgView);
                        break;
                }
            } else if (customize) {
                Glide.with(container).load(mData.get(position - 3)).into(imgView);
            } else {
                Glide.with(container).load((String) imageList.get(position - 3)).into(imgView);
            }
        } else {
            if (customize) {
                Glide.with(container).load(mData.get(position)).into(imgView);
            } else {
                Glide.with(container).load((String) imageList.get(position)).into(imgView);
            }
        }
        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        if (isTest) {
            if (customize)
                return mData.size() + 3;
            else
                return imageList.size() + 3;
        } else {
            if (customize)
                return mData.size();
            else
                return imageList.size();
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == (View)o);
    }

    public void setIsTest(boolean flag) {
        this.isTest = flag;
    }
}
