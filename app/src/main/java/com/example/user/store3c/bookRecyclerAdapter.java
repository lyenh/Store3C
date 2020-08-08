package com.example.user.store3c;

import android.app.Activity;
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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by user on 2016/10/24.
 */

public class bookRecyclerAdapter extends  RecyclerView.Adapter<bookRecyclerAdapter.ViewHolder>{
    private ArrayList<ProductItem> BookData;
    private Context mContext;
    public static int screenWidth;
    private String menuItem;
    private int buyBtnNum;

    bookRecyclerAdapter(ArrayList<ProductItem> BookData, Context c, String menuItem) {
        this.BookData = BookData;
        this.menuItem = menuItem;
        mContext = c;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ImageView bookImage;
        private final TextView bookName;
        private final TextView bookPrice;
        private final TextView bookIntro;
        private final ImageView bookBuy;
        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);

            bookImage = itemView.findViewById(R.id.bookImg_id);
            bookName = itemView.findViewById(R.id.bookName_id);
            bookPrice = itemView.findViewById(R.id.bookPrice_id);
            bookIntro = itemView.findViewById(R.id.bookIntro_id);
            bookBuy = itemView.findViewById(R.id.buyBook_id);

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
    public @NonNull bookRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.book_item_view, parent, false);

        return new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                if (InternetConnection.checkConnection(mContext)) {
                    Intent intentItem = new Intent();
                    Bundle bundle = new Bundle();

                    bundle.putInt("Position", position);
                    intentItem.putExtras(bundle);
                    intentItem.setClass(mContext, PageActivity.class);
                    mContext.startActivity(intentItem);
                    ((Activity) mContext).finish();
                    //Log.i("position => ", Integer.toString(position));
                }
                else {
                    Toast.makeText(mContext, "網路未連線! ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(bookRecyclerAdapter.ViewHolder holder, int position) {
        int imgWidth, imgHeight;
        ProductItem book = BookData.get(position);

        imgWidth = screenWidth - 100;
        imgHeight = imgWidth - 10;
        holder.bookImage.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));
        holder.bookImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.bookImage.setPadding(2, 2, 2, 2);

        holder.bookImage.setImageBitmap(book.getImg());
        holder.bookName.setText(book.getName());
        holder.bookPrice.setText(book.getPrice());
        holder.bookIntro.setText(book.getIntro());

        holder.bookBuy.setTag(position);
        holder.bookBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.buyBook_id) {
                    buyBtnNum = (int) view.findViewById(R.id.buyBook_id).getTag();
                    ProductItem bookItem = BookData.get(buyBtnNum);
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();

                    bundle.putString("Menu", menuItem);
                    bundle.putByteArray("Pic", Bitmap2Bytes(bookItem.getImg()));
                    bundle.putString("Name", bookItem.getName());
                    bundle.putString("Price", bookItem.getPrice());
                    bundle.putString("Intro", bookItem.getIntro());
                    intent.putExtras(bundle);

                    Toast.makeText(mContext, "已加入購物車!", Toast.LENGTH_SHORT).show();
                    intent.setClass(mContext, OrderActivity.class);
                    mContext.startActivity(intent);
                    ((Activity)mContext).finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return BookData.size();
    }


    private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

}
