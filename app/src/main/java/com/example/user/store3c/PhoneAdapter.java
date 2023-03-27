package com.example.user.store3c;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.ViewHolder>{
    private ArrayList<ProductItem> ProductData;
    private Context mContext;
    private PhoneActivity activityPhone;
    private TabFragment tabFragment;
    private int productAmount;
    private static boolean[] tabLoadAll = {true, true, true};
    private int framePosition;
    int screenWidth;

    PhoneAdapter(ArrayList<ProductItem> ProductData, Context c, TabFragment f, int position) {
        this.ProductData = ProductData;
        this.tabFragment = f;
        this.productAmount = ProductData.size();
        this.framePosition = position;
        mContext = c;
        activityPhone = (PhoneActivity) c;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ImageView ProductImage, RefreshImage, MoreImage;
        private final TextView ProductName, RefreshText;
        public MyViewHolderClick mListener;

        private ViewHolder(View itemView, MyViewHolderClick listener, int viewType) {
            super(itemView);

            if (viewType == 1) {
                RefreshText = itemView.findViewById(R.id.refreshText_id);
                RefreshImage = itemView.findViewById(R.id.listAllPhone_id);
                ProductImage = null;
                ProductName = null;
                MoreImage = null;
            }
            else {
                RefreshText = null;
                RefreshImage = null;
                ProductImage = itemView.findViewById(R.id.dishImg_id);
                ProductName = itemView.findViewById(R.id.dishName_id);
                MoreImage = itemView.findViewById(R.id.moreImg_id);
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
    public @NonNull PhoneAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view;
        ViewHolder viewHolder;

        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.phone_last_item_view, parent, false);
            viewHolder = new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                    //Toast.makeText(activityPhone, "The product amount is " + position, Toast.LENGTH_SHORT).show();
                    //Log.i("position => ", Integer.toString(position));
                }
            }, viewType);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.phone_item_view, parent, false);
            viewHolder = new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                    Intent intentItem = new Intent();
                    Bundle bundle = new Bundle();

                    bundle.putString("Menu", "PHONE");
                    bundle.putByteArray("Pic", Bitmap2Bytes(ProductData.get(position).getImg()));
                    bundle.putString("Name", ProductData.get(position).getName());
                    bundle.putString("Price", ProductData.get(position).getPrice());
                    bundle.putString("Intro", ProductData.get(position).getIntro());

                    intentItem.putExtras(bundle);
                    intentItem.setClass(mContext, ProductActivity.class);
                    mContext.startActivity(intentItem);
                    ((Activity) mContext).finish();

                    //Log.i("position => ", Integer.toString(position));
                }
            }, viewType);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneAdapter.ViewHolder holder, int position) {
        int imgWidth, imgHeight;

        if (position == productAmount) {
            if (tabLoadAll[framePosition]) {
                holder.RefreshText.setVisibility(View.GONE);
                holder.RefreshImage.setVisibility(View.GONE);
            }
            else {
                holder.RefreshText.setVisibility(View.VISIBLE);
                holder.RefreshImage.setVisibility(View.VISIBLE);
                holder.RefreshImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getId() == R.id.listAllPhone_id ) {
                            tabFragment.reloadAdapter(framePosition, true, 1);
                            if (PhoneTypeAdapter.selectedTextView.get(framePosition).size() !=0) {
                                TextView selectedTypeName;
                                for (View v : PhoneTypeAdapter.selectedTextView.get(framePosition)) {
                                    selectedTypeName = v.findViewById(R.id.phoneTypeText_id);
                                    selectedTypeName.setTextColor(tabFragment.phoneTypeAdapter.textColor);
                                    selectedTypeName.setTypeface(null, Typeface.NORMAL);
                                }
                                PhoneTypeAdapter.selectedTextView.get(framePosition).clear();
                            }
                            //Toast.makeText(OrderActivity.this, "The delete number: " + favDeleteBtn, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        else {
            ProductItem product = ProductData.get(position);
            imgWidth = (screenWidth / 2) - 8;
            imgHeight = (imgWidth * 3) / 2;
            holder.ProductImage.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));
            holder.ProductImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.ProductImage.setPadding(1, 1, 1, 1);

            holder.ProductImage.setImageBitmap(product.getImg());
            holder.ProductName.setText(product.getName());
            holder.MoreImage.setTag(position);
            holder.MoreImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.getId() == R.id.moreImg_id) {
                        //int phonePosition = (int) view.findViewById(R.id.moreImg_id).getTag();
                        PopupMenu popupMenu = new PopupMenu(activityPhone, view);
                        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                        popupMenu.show();
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Random ran  = new Random();
                                switch (item.getItemId()) {
                                    case R.id.sellAmount_id:
                                        int sellAmount = ran.nextInt(50);
                                        Toast.makeText(activityPhone, "已銷售" + sellAmount + "隻 !", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.saveAmount_id:
                                        int saveAmount = ran.nextInt(50) + 50;
                                        Toast.makeText(activityPhone, "有庫存" + saveAmount + "隻 !", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                            @Override
                            public void onDismiss(PopupMenu menu) {

                            }
                        });
                        //Toast.makeText(activityPhone, "The phone position " + phonePosition, Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

        if (position == productAmount) {
            return 1;
        }
        else {
            return super.getItemViewType(position);
        }
    }

    void setItemList(ArrayList<ProductItem> itemList, boolean[] tabLoadAll) {
        this.ProductData = itemList;
        PhoneAdapter.tabLoadAll = tabLoadAll;
        productAmount = itemList.size();
    }

    private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

}

