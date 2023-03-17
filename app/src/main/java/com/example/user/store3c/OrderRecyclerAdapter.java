package com.example.user.store3c;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class OrderRecyclerAdapter extends  RecyclerView.Adapter<OrderRecyclerAdapter.ViewHolder>{
    private ArrayList<ProductItem> orderTable;
    private OrderActivity orderActivity;
    private String menuItem, upMenuItem;
    public static int screenWidth;
    public static SortedMap<Integer, CheckBox> checkBoxList = new TreeMap<>();
    private final OrderRecyclerAdapter.CheckBoxHandler cbHandler;

    OrderRecyclerAdapter(OrderActivity a, ArrayList<ProductItem> productData, String menuItem, String upMenuItem) {
        this.orderTable = productData;
        this.orderActivity = a;
        this.menuItem = menuItem;
        this.upMenuItem = upMenuItem;
        cbHandler = new OrderRecyclerAdapter.CheckBoxHandler();
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    static class CheckBoxHandler extends Handler {
        int cbListCount = 0;

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            cbListCount = cbListCount + msg.what;
            if (cbListCount == checkBoxList.size()) {
                checkBoxList.clear();
                OrderActivity.total_price = 0;
                cbListCount = 0;
            }
        }
    }

    public void ResetCheckBox() {
        final int count = 1;

        if (!checkBoxList.isEmpty()) {
            for (final int keyNum : checkBoxList.keySet()) {
                if (!new Handler().post(new Runnable() {
                    public void run () {
                        Objects.requireNonNull(checkBoxList.get(keyNum)).setChecked(false);
                        cbHandler.sendEmptyMessage(count);
                    }
                })) {
                    Toast.makeText(orderActivity, "Runnable fail ! ", Toast.LENGTH_SHORT).show();
                }
                //Log.i("Reset key =>  ", String.valueOf(keyNum));
                //Log.i("Reset value =>  ", Objects.requireNonNull(Objects.requireNonNull(checkBoxList.get(keyNum)).toString()));
            }
        }
        else {
            OrderActivity.total_price = 0;
        }
    }

    public void ReorderCheckBoxList(int fromPosition, int toPosition) {
        CheckBox cbFrom;

        cbFrom = checkBoxList.get(fromPosition);
        if (fromPosition > toPosition) {
            for (int i = fromPosition - 1; i > toPosition - 1; i--) {
                CheckBox cbTemp = checkBoxList.get(i);
                checkBoxList.put(i + 1, cbTemp);
            }
        } else {
            for (int i = fromPosition; i < toPosition; i++) {
                CheckBox cbTemp = checkBoxList.get(i + 1);
                checkBoxList.put(i, cbTemp);
            }
        }
        checkBoxList.put(toPosition, cbFrom);
        for (int i = 0; i < orderTable.size(); i++) {
            if (checkBoxList.containsKey(i) && checkBoxList.get(i) == null) {
                checkBoxList.remove(i);
            }
        }
    }

    public void RemoveCheckBox(int index) {
        boolean shift = false;
        int orderTableCount = orderTable.size();

        checkBoxList.remove(index);
        for (int i=0;index < orderTableCount-1;i++,index++) {
            CheckBox cb = checkBoxList.get(index+1);

            if (i == 0) {
                shift = true;
                checkBoxList.put(index, cb);
            }
            else {
                checkBoxList.put(index, cb);
            }
        }
        if (shift && index == orderTableCount-1) {
            checkBoxList.remove(index);
        }
        for (int i=0; i<orderTableCount; i++) {
            if (checkBoxList.containsKey(i) && checkBoxList.get(i) == null) {
                checkBoxList.remove(i);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView ImgView;
        private TextView NameView;
        private TextView PriceView;
        private ReorderImageView BtnView;
        private CheckBox orderCheckBox;
        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener, int viewType) {
            super(itemView);

            if (viewType == 1) {
                ImgView = itemView.findViewById(R.id.imgOrderItem_id);
                NameView = itemView.findViewById(R.id.nameOrderItem_id);
                PriceView = itemView.findViewById(R.id.priceOrderItem_id);
                BtnView = itemView.findViewById(R.id.reorderOrderItem_id);
                orderCheckBox = itemView.findViewById(R.id.orderItemCheckBox_id);
            }
            else {
                ImgView = itemView.findViewById(R.id.imgOrder_id);
                NameView = itemView.findViewById(R.id.nameOrder_id);
                PriceView = itemView.findViewById(R.id.priceOrder_id);
                BtnView = itemView.findViewById(R.id.reorderOrder_id);
                orderCheckBox = itemView.findViewById(R.id.orderViewCheckBox_id);
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
                if (!upMenuItem.equals("")) {
                    bundle.putString("upMenu", upMenuItem);
                }
                bundle.putByteArray("Pic", Bitmap2Bytes(orderTable.get(position).getImg()));
                bundle.putString("Name", orderTable.get(position).getName());
                bundle.putString("Price", orderTable.get(position).getPrice());
                bundle.putString("Intro", orderTable.get(position).getIntro());
                bundle.putString("Order", "ORDER");
                if (orderActivity.recentTaskOrder) {
                    bundle.putString("RetainRecentTask", "RECENT_ACTIVITY");
                }
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

            holder.orderCheckBox.setTag(position);
            if (checkBoxList.containsKey(position)) {
                holder.orderCheckBox.setChecked(true);
                checkBoxList.put(position, holder.orderCheckBox);
                //Log.i("onBind position =>  ", String.valueOf(position));
                //Log.i("onBind value =>  ", holder.orderCheckBox.toString());
            }
            else {
                holder.orderCheckBox.setChecked(false);
            }
        }

        holder.orderCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checkBoxId = view.getId();
                if (checkBoxId == R.id.orderItemCheckBox_id || checkBoxId == R.id.orderViewCheckBox_id) {
                    CheckBox checkBox = (CheckBox) view.findViewById(checkBoxId);
                    int checkBoxNum = (int) checkBox.getTag();

                    if (checkBox.isChecked()) {
                        String addOrderPrice = orderTable.get(checkBoxNum).getPrice();
                        OrderActivity.total_price = OrderActivity.total_price + Integer.parseInt(addOrderPrice.split("元")[0]);
                        orderActivity.orderTextList = "總共 " + ((int)OrderActivity.total_price) + " 元 !";
                        orderActivity.orderText.setText(orderActivity.orderTextList);
                        checkBoxList.put(checkBoxNum, checkBox);
                        //Log.i("onClick key =>  ", String.valueOf(checkBoxNum));
                        //Log.i("onClick value =>  ", checkBox.toString());
                    }
                    else {
                        String addOrderPrice = orderTable.get(checkBoxNum).getPrice();
                        OrderActivity.total_price = OrderActivity.total_price - Integer.parseInt(addOrderPrice.split("元")[0]);
                        orderActivity.orderTextList = "總共 " + ((int)OrderActivity.total_price) + " 元 !";
                        orderActivity.orderText.setText(orderActivity.orderTextList);
                        checkBoxList.remove(checkBoxNum);
                    }
                }
            }
        });

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

