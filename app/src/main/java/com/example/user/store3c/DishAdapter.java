package com.example.user.store3c;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by user on 2017/1/4.
 */

public class DishAdapter extends  RecyclerView.Adapter<DishAdapter.ViewHolder>{
    private ArrayList<ProductItem> ProductData;
    private Context mContext;
    public static int screenWidth;
    private String menuItem;
    private static MainActivity activity = null;
    private ImageView dot1, dot2, dot3, dot4, dot5;
    static userAdapterHandler userAdAdapterHandler;

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


    @Override
    public DishAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewPager pager;
        PagerAdapter mPagerAdapter;
        Context context = parent.getContext();
        View view;
        ViewHolder viewHolder;

        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.content_ad_view, parent, false);

            if (MainActivity.adapterLayout == 1) {
                /*List<Fragment> fragments = new Vector<Fragment>();
                fragments.add(Fragment.instantiate(activity, Slide1Fragment.class.getName()));
                fragments.add(Fragment.instantiate(activity, Slide2Fragment.class.getName()));
                fragments.add(Fragment.instantiate(activity, Slide3Fragment.class.getName()));
                fragments.add(Fragment.instantiate(activity, Slide4Fragment.class.getName()));
                fragments.add(Fragment.instantiate(activity, Slide5Fragment.class.getName()));*/

                pager = view.findViewById(R.id.viewPager_id);
                userAdAdapterHandler = new userAdapterHandler(pager);
                mPagerAdapter = new PageAdapter(activity.getSupportFragmentManager(), MainActivity.fragments);
                pager.setAdapter(mPagerAdapter);

                UserTimerThread adTimerThread = new UserTimerThread(activity);
                activity.TimerThread = 1;
                adTimerThread.start();

                dot1 = view.findViewById(R.id.imgIcon1_id);
                dot2 = view.findViewById(R.id.imgIcon2_id);
                dot3 = view.findViewById(R.id.imgIcon3_id);
                dot4 = view.findViewById(R.id.imgIcon4_id);
                dot5 = view.findViewById(R.id.imgIcon5_id);

                pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        //invalidateOptionsMenu();

                        switch (position) {
                            case 0:
                                dot1.setImageResource(R.drawable.dot1);
                                dot2.setImageResource(R.drawable.dot2);
                                dot3.setImageResource(R.drawable.dot2);
                                dot4.setImageResource(R.drawable.dot2);
                                dot5.setImageResource(R.drawable.dot2);
                                break;
                            case 1:
                                dot1.setImageResource(R.drawable.dot2);
                                dot2.setImageResource(R.drawable.dot1);
                                dot3.setImageResource(R.drawable.dot2);
                                dot4.setImageResource(R.drawable.dot2);
                                dot5.setImageResource(R.drawable.dot2);
                                break;
                            case 2:
                                dot1.setImageResource(R.drawable.dot2);
                                dot2.setImageResource(R.drawable.dot2);
                                dot3.setImageResource(R.drawable.dot1);
                                dot4.setImageResource(R.drawable.dot2);
                                dot5.setImageResource(R.drawable.dot2);
                                break;
                            case 3:
                                dot1.setImageResource(R.drawable.dot2);
                                dot2.setImageResource(R.drawable.dot2);
                                dot3.setImageResource(R.drawable.dot2);
                                dot4.setImageResource(R.drawable.dot1);
                                dot5.setImageResource(R.drawable.dot2);
                                break;
                            case 4:
                                dot1.setImageResource(R.drawable.dot2);
                                dot2.setImageResource(R.drawable.dot2);
                                dot3.setImageResource(R.drawable.dot2);
                                dot4.setImageResource(R.drawable.dot2);
                                dot5.setImageResource(R.drawable.dot1);
                                break;
                        }
                        //Toast.makeText(MainActivity.this, "The top fragment "+ position, Toast.LENGTH_SHORT).show();
                    }
                });

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
                    //mContext.startActivity(intentItem);
                    //((Activity)mContext).finish();

                    //Log.i("position => ", Integer.toString(position));
                }
            }, viewType);
        }

        return viewHolder;
    }

    public static class userAdapterHandler extends Handler {
        private WeakReference<ViewPager> weakRefPager;

        userAdapterHandler (ViewPager hPager) {
            weakRefPager = new WeakReference<>(hPager);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity.appRntTimer = msg.what;
            ViewPager hmPager = weakRefPager.get();
            if (hmPager != null) {
                switch (msg.what) {
                    case 0:
                        //Toast.makeText(MainActivity.this, "The picture number is 1" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(0);
                        break;
                    case 1:
                        //Toast.makeText(MainActivity.this, "The picture number is 2" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(1);
                        break;
                    case 2:
                        //Toast.makeText(MainActivity.this, "The picture number is 3" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(2);
                        break;
                    case 3:
                        //Toast.makeText(MainActivity.this, "The picture number is 4" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(3);
                        break;
                    case 4:
                        //Toast.makeText(MainActivity.this, "The picture number is 5" , Toast.LENGTH_SHORT).show();
                        hmPager.setCurrentItem(4);
                        break;
                }
                if (MainActivity.appRntTimer == (MainActivity.returnApp % 5)) {
                    MainActivity.returnApp = 0;
                }
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public void onBindViewHolder(DishAdapter.ViewHolder holder, int position) {
        int imgWidth, imgHeight;

        if (position > 0) {
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

