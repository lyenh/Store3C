package com.example.user.store3c;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SearchRecyclerAdapter extends  RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder>{
    private ArrayList<ProductItem> resultTable;
    private SearchActivity searchActivity;
    private String menuItem;
    public static int screenWidth;

    SearchRecyclerAdapter(SearchActivity a, ArrayList<ProductItem> productData, String menuItem) {
        this.resultTable = productData;
        this.searchActivity = a;
        this.menuItem = menuItem;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView ImgView;
        private TextView NameView;
        private TextView PriceView;

        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener, int viewType) {
            super(itemView);

            if (viewType == 1) {
                ImgView = itemView.findViewById(R.id.imgSearchItem_id);
                NameView = itemView.findViewById(R.id.nameSearchItem_id);
                PriceView = itemView.findViewById(R.id.priceSearchItem_id);
            }
            else {
                ImgView = itemView.findViewById(R.id.imgSearch_id);
                NameView = itemView.findViewById(R.id.nameSearch_id);
                PriceView = itemView.findViewById(R.id.priceSearch_id);
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

    @Override
    public SearchRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view;

        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_search_item, parent, false);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.activity_search_view, parent, false);
        }

        return new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                Intent intentItem = new Intent();
                Bundle bundle = new Bundle();

                bundle.putString("Menu", menuItem);
                bundle.putByteArray("Pic", Bitmap2Bytes(resultTable.get(position).getImg()));
                bundle.putString("Name", resultTable.get(position).getName());
                bundle.putString("Price", resultTable.get(position).getPrice());
                bundle.putString("Intro", resultTable.get(position).getIntro());
                bundle.putString("Search", "SEARCH");
                intentItem.putExtras(bundle);
                intentItem.setClass(searchActivity, ProductActivity.class);
                searchActivity.startActivity(intentItem);
                searchActivity.finish();

                //Log.i("position => ", Integer.toString(position));
            }
        }, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRecyclerAdapter.ViewHolder holder, int position) {
        ProductItem productData = resultTable.get(position);
        int imgWidth, imgHeight;

        if (productData != null) {
            if (resultTable.get(position).getIntro().equals("待採購產品")) {
                holder.ImgView.setPadding(3, 3, 3, 3);
            }else {
                imgWidth = ((screenWidth * 2) / 3) - 6;
                imgHeight = imgWidth - 10;
                String brand[] = productData.getName().split(" ");
                switch (brand[0]) {
                    case "HTC":
                    case "ZenFone":
                    case "Galaxy":
                        imgWidth = 300;
                        imgHeight = 500;
                        break;
                    default:
                        break;
                }
                holder.ImgView.setPadding(30, 30, 30, 30);
                holder.ImgView.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));
            }

            holder.ImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.ImgView.setImageBitmap(productData.getImg());
            holder.NameView.setText(productData.getName());
            holder.PriceView.setText(productData.getPrice());
        }

    }

    @Override
    public int getItemCount() {
        return resultTable.size();
    }

    @Override
    public int getItemViewType(int position) {

        //Log.v("ViewType position ==>",Integer.toString(position));
        //Log.v("ViewType  ==>",Integer.toString(super.getItemViewType(position)));

        if (resultTable.get(position).getIntro().equals("待採購產品")) {
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