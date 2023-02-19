package com.example.user.store3c;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;

/**
 * Created by user on 2017/1/4.
 */

public class DishAdapter extends  RecyclerView.Adapter<DishAdapter.ViewHolder>{
    private final ArrayList<ProductItem> ProductData;
    private final Context mContext;
    public static int screenWidth;
    private final String menuItem;
    private static MainActivity activity = null;

    DishAdapter(ArrayList<ProductItem> ProductData, Context c, String menuItem) {
        this.ProductData = ProductData;
        this.menuItem = menuItem;
        mContext = c;
        if (menuItem.equals("DISH")) {
            activity = (MainActivity) c;
        }
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ImageView ProductImage;
        private final TextView ProductName;
        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener, int viewType) {
            super(itemView);

            if (viewType == 1) {
                ProductImage = null;
                ProductName = null;
            }
            else {
                ProductImage = itemView.findViewById(R.id.dishImg_id);
                ProductName = itemView.findViewById(R.id.dishName_id);
            }
            mListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.clickOnView(v, getLayoutPosition());
        }

        public interface MyViewHolderClick {
            void clickOnView(View v, int position);
        }

    }

    @NonNull
    @Override
    public DishAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view;
        ViewHolder viewHolder;

        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.content_ad_view, parent, false);
            if (MainActivity.adapterLayout == 1) {
                activity.iniUpperPage(activity, activity.getLifecycle(), view);
            }
            viewHolder = new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                    //Log.i("position => ", Integer.toString(position));
                }
            }, viewType);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.dish_item_view, parent, false);
            viewHolder = new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                    Intent intentItem = new Intent();
                    Bundle bundle = new Bundle();

                    bundle.putString("Menu",menuItem);
                    bundle.putByteArray("Pic",Bitmap2Bytes(ProductData.get(position-1).getImg()));
                    bundle.putString("Name",ProductData.get(position-1).getName());
                    bundle.putString("Price",ProductData.get(position-1).getPrice());
                    bundle.putString("Intro",ProductData.get(position-1).getIntro());

                    intentItem.putExtras(bundle);
                    intentItem.setClass(activity, ProductActivity.class);
                    if (menuItem.equals("DISH")) {
                        activity.TimerThread = 0;
                    }
                    mContext.startActivity(intentItem);
                    activity.finish();

                    //Log.i("position => ", Integer.toString(position));
                }
            }, viewType);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DishAdapter.ViewHolder holder, int position) {
        int imgWidth, imgHeight;

        if (position == 0 ) {
            imgHeight = (screenWidth / 4) * 3;
            holder.itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imgHeight));
        }
        else if (position > 0) {
            ProductItem product = ProductData.get(position-1);
            imgWidth = (screenWidth / 2) - 8;
            imgHeight = imgWidth - 1;
            holder.ProductImage.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));
            holder.ProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.ProductImage.setPadding(1, 1, 1, 1);

            holder.ProductImage.setImageBitmap(product.getImg());
            holder.ProductName.setText(product.getName());
        }
    }

    @Override
    public int getItemCount() {
        return (ProductData.size()+1);
    }

    @Override
    public int getItemViewType(int position) {

        //Log.v("ViewType position ==>",Integer.toString(position));
        //Log.v("ViewType  ==>",Integer.toString(super.getItemViewType(position)));

        if (position == 0) {
            return 1;
        }
        else {
            return super.getItemViewType(position);
        }
    }

    private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

}

