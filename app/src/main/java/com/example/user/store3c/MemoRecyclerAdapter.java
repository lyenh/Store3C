package com.example.user.store3c;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MemoRecyclerAdapter extends  RecyclerView.Adapter<MemoRecyclerAdapter.ViewHolder>{
    private ArrayList<String> memoTable, memoPriceTable;
    private int addBtnNum;
    private MemoActivity memoActivity;
    private String menuItem;

    MemoRecyclerAdapter(MemoActivity a, ArrayList<String> memoData, ArrayList<String> memoPriceData, String menuItem) {
        this.memoTable = memoData;
        this.memoPriceTable = memoPriceData;
        this.memoActivity = a;
        this.menuItem = menuItem;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView memoListText, memoListPrice;
        private ImageView addMemoBtn;
        private MemoReorderImageView reorderBtn;
        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);

            memoListText = itemView.findViewById(R.id.memoTextList_id);
            memoListPrice = itemView.findViewById(R.id.memoPriceList_id);
            addMemoBtn = itemView.findViewById(R.id.addMemoText_id);
            reorderBtn = itemView.findViewById(R.id.memoReorder_id);
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
    public @NonNull MemoRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.memo_item_view, parent, false);

        return new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                memoActivity.updateMemo = memoTable.get(position);
                memoActivity.updateMemoPrice = memoPriceTable.get(position);
                memoActivity.updateArrayIndex = position;
                memoActivity.memoText.setText(memoActivity.updateMemo);
                memoActivity.memoPrice.setText(memoActivity.updateMemoPrice);
                memoActivity.update = true;
                //Log.i("position => ", Integer.toString(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull MemoRecyclerAdapter.ViewHolder holder, int position) {
        String memoData = memoTable.get(position), memoPriceData = memoPriceTable.get(position);
        final MemoRecyclerAdapter.ViewHolder touchHolder = holder;
        if (memoData != null) {
            holder.memoListText.setText(memoData);
            String price = memoPriceData + "元";
            holder.memoListPrice.setText(price);
            holder.addMemoBtn.setTag(position);
        }

        holder.addMemoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.addMemoText_id) {
                    addBtnNum = (int) view.findViewById(R.id.addMemoText_id).getTag();
                    String addMemoText = memoTable.get(addBtnNum);
                    String addMemoPrice = memoPriceTable.get(addBtnNum);
                    Bitmap bitmap = BitmapFactory.decodeResource(memoActivity.getResources(), R.drawable.store_item);
                    ProductItem Item = new ProductItem(bitmap, addMemoText,addMemoPrice+"元","待採購產品");
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();

                    memoActivity.update = false;
                    memoActivity.memoText.setText("");
                    memoActivity.memoPrice.setText("");

                    bundle.putString("Menu", "MEMO");
                    bundle.putString("upMenu", menuItem);
                    bundle.putByteArray("Pic", Bitmap2Bytes(bitmap));
                    bundle.putString("Name", Item.getName());
                    bundle.putString("Price", Item.getPrice());
                    bundle.putString("Intro", Item.getIntro());
                    intent.putExtras(bundle);
                    Toast.makeText(memoActivity, "已加入購物車!", Toast.LENGTH_SHORT).show();
                    intent.setClass(memoActivity, OrderActivity.class);
                    memoActivity.startActivity(intent);
                    memoActivity.finish();
                }
            }
        });

        holder.reorderBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    memoActivity.ith.startDrag(touchHolder);
                }
                else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    touchHolder.reorderBtn.performClick();
                    return true;
                }

                return false;
            }

        });

    }

    @Override
    public int getItemCount() {
        return memoTable.size();
    }


    private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }

}


