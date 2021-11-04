package com.example.user.store3c;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class CameraAdapter extends  RecyclerView.Adapter<CameraAdapter.ViewHolder>{
    private ArrayList<ProductItem> ProductData;
    private Context mContext;
    private int screenWidth;
    private String menuItem;
    private CameraActivity activity;
    private YouTubeFragment YouTubeF;
    private String[][] cameraVideoId = { {"jHzU8Ixa75w", "I2W0Opbtrpg", "zywGqoPIc0k"},
                                        {"Nd1wc8XMarg", "mzwPuyfQcrA", "ui525Iwe-cQ"},
                                        {"dUbZBlbmNng", "P_noJ0Ati60", "D8AvEstX_3E"}};
    private int videoPlayBtn;

    CameraAdapter(ArrayList<ProductItem> ProductData, Context c, String menuItem, YouTubeFragment YouTubeF) {
        this.ProductData = ProductData;
        this.menuItem = menuItem;
        this.YouTubeF = YouTubeF;
        mContext = c;
        activity = (CameraActivity) c;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ImageView cameraImage, videoPlay;
        private final TextView cameraName;
        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);

            cameraImage = itemView.findViewById(R.id.cameraImg_id);
            cameraName = itemView.findViewById(R.id.cameraName_id);
            videoPlay = itemView.findViewById(R.id.videoPlay_id);
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
    public @NonNull CameraAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.camera_item_view, parent, false);

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
                mContext.startActivity(intentItem);
                ((Activity)mContext).finish();
                //Log.i("position => ", Integer.toString(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(CameraAdapter.ViewHolder holder, int position) {
        int imgWidth, imgHeight;
        ProductItem product = ProductData.get(position);

        imgWidth = (screenWidth / 2) - 8;
        imgHeight = imgWidth - 3;
        holder.cameraImage.setLayoutParams(new LinearLayout.LayoutParams(imgWidth, imgHeight));
        holder.cameraImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.cameraImage.setPadding(1,1,1,1);

        holder.cameraImage.setImageBitmap(product.getImg());
        holder.cameraName.setText(product.getName());
        holder.videoPlay.setImageResource(R.drawable.video_play);
        holder.videoPlay.setTag(position);
        holder.videoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.videoPlay_id:
                        videoPlayBtn = (int) view.findViewById(R.id.videoPlay_id).getTag();
                        if (!YouTubeF.youTubePlayerBoolean) {
                            String cameraName = ProductData.get(videoPlayBtn).getName();
                            String[] tokens = cameraName.split(" ");
                            String brand = tokens[0];
                            int brandId = 0;
                            switch (brand) {
                                case "Nikon":
                                    break;
                                case "Canon":
                                    brandId = 1;
                                    break;
                                case "Sony":
                                    brandId = 2;
                                    break;
                                default:
                                    break;
                            }
                            YouTubeFragment.YPlayer.loadVideo(cameraVideoId[brandId][videoPlayBtn]);
                            YouTubeFragment.YPlayer.play();
                        }
                        //Toast.makeText(activity, "The number: " + videoPlayBtn, Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
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

