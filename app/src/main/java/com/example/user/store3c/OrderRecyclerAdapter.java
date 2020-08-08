package com.example.user.store3c;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class OrderRecyclerAdapter extends  RecyclerView.Adapter<OrderRecyclerAdapter.ViewHolder>{
    private ArrayList<ProductItem> orderTable;
    private OrderActivity orderActivity;
    private String menuItem;
    public static int screenWidth;

    OrderRecyclerAdapter(OrderActivity a, ArrayList<ProductItem> productData, String menuItem) {
        this.orderTable = productData;
        this.orderActivity = a;
        this.menuItem = menuItem;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView ImgView;
        private TextView NameView;
        private TextView PriceView;
        private MemoReorderImageView BtnView;

        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener, int viewType) {
            super(itemView);

            if (viewType == 1) {
                ImgView = itemView.findViewById(R.id.imgOrderItem_id);
                NameView = itemView.findViewById(R.id.nameOrderItem_id);
                PriceView = itemView.findViewById(R.id.priceOrderItem_id);
                BtnView = itemView.findViewById(R.id.reorderOrderItem_id);
            }
            else {
                ImgView = itemView.findViewById(R.id.imgOrder_id);
                NameView = itemView.findViewById(R.id.nameOrder_id);
                PriceView = itemView.findViewById(R.id.priceOrder_id);
                BtnView = itemView.findViewById(R.id.reorderOrder_id);
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
    public OrderRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view;

        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_order_item, parent, false);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.activity_order_view, parent, false);
        }

        return new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                Intent intentItem = new Intent();
                Bundle bundle = new Bundle();

                bundle.putString("Menu", menuItem);
                bundle.putByteArray("Pic", Bitmap2Bytes(orderTable.get(position).getImg()));
                bundle.putString("Name", orderTable.get(position).getName());
                bundle.putString("Price", orderTable.get(position).getPrice());
                bundle.putString("Intro", orderTable.get(position).getIntro());
                bundle.putString("Order", "ORDER");
                intentItem.putExtras(bundle);
                intentItem.setClass(orderActivity, ProductActivity.class);
                orderActivity.startActivity(intentItem);
                orderActivity.finish();

                //Log.i("position => ", Integer.toString(position));
            }
        }, viewType);
    }

    @Override
    public void onBindViewHolder(OrderRecyclerAdapter.ViewHolder holder, int position) {
        ProductItem productData = orderTable.get(position);
        final OrderRecyclerAdapter.ViewHolder touchHolder = holder;
        int imgWidth, imgHeight;

        if (productData != null) {
            if (orderTable.get(position).getIntro().equals("待採購產品")) {
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

        holder.BtnView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    orderActivity.ith.startDrag(touchHolder);
                }
                else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    touchHolder.BtnView.performClick();
                    return true;
                }

                return false;
            }

        });

    }

    @Override
    public int getItemCount() {
        return orderTable.size();
    }

    @Override
    public int getItemViewType(int position) {

        //Log.v("ViewType position ==>",Integer.toString(position));
        //Log.v("ViewType  ==>",Integer.toString(super.getItemViewType(position)));

        if (orderTable.get(position).getIntro().equals("待採購產品")) {
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

