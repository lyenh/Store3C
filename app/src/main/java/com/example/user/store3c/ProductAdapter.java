package com.example.user.store3c;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
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
 * Created by user on 2016/11/19.
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>{
    private ArrayList<ProductItem> ProductData;
    private Context mContext;
    private int screenWidth;
    private String menuItem;
    private MainActivity activity = null;

    ProductAdapter(ArrayList<ProductItem> ProductData, Context c, String menuItem) {
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

        private ViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);

            ProductImage = itemView.findViewById(R.id.dishImg_id);
            ProductName = itemView.findViewById(R.id.dishName_id);

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


    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dish_item_view, parent, false);

        return new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                Intent intentItem = new Intent();
                Bundle bundle = new Bundle();

                bundle.putString("Menu",menuItem);
                bundle.putByteArray("Pic",Bitmap2Bytes(ProductData.get(position).getImg()));
                bundle.putString("Name",ProductData.get(position).getName());
                bundle.putString("Price",ProductData.get(position).getPrice());
                bundle.putString("Intro",ProductData.get(position).getIntro());

                intentItem.putExtras(bundle);
                intentItem.setClass(mContext, ProductActivity.class);
                if (menuItem.equals("DISH")) {
                    activity.TimerThread = 0;
                }
                mContext.startActivity(intentItem);
                ((Activity)mContext).finish();

                //Log.i("position => ", Integer.toString(position));
            }
        });

    }

    @Override
    public void onBindViewHolder(ProductAdapter.ViewHolder holder, int position) {
        int imgWidth, imgHeight;
        ProductItem product = ProductData.get(position);

        imgWidth = (screenWidth / 2) - 8;
        imgHeight = imgWidth - 3;
        holder.ProductImage.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));
        holder.ProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.ProductImage.setPadding(1,1,1,1);

        holder.ProductImage.setImageBitmap(product.getImg());
        holder.ProductName.setText(product.getName());
    }

    @Override
    public int getItemCount() {
        return ProductData.size();
    }

    private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }


}
