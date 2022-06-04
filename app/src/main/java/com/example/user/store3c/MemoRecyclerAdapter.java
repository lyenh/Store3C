package com.example.user.store3c;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class MemoRecyclerAdapter extends  RecyclerView.Adapter<MemoRecyclerAdapter.ViewHolder>{
    private final ArrayList<String> memoTable, memoPriceTable;
    private final MemoActivity memoActivity;
    public static SortedMap<Integer, CheckBox> checkBoxList = new TreeMap<>();
    private static int totalPrice = 0;
    private final CheckBoxHandler cbHandler;

    MemoRecyclerAdapter(MemoActivity a, ArrayList<String> memoData, ArrayList<String> memoPriceData) {
        this.memoTable = memoData;
        this.memoPriceTable = memoPriceData;
        this.memoActivity = a;
        cbHandler = new CheckBoxHandler();
    }

    static class CheckBoxHandler extends Handler {
        int cbListCount = 0;

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            cbListCount = cbListCount + msg.what;
            if (cbListCount == checkBoxList.size()) {
                checkBoxList.clear();
                totalPrice = 0;
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
                    Toast.makeText(memoActivity, "Runnable fail ! ", Toast.LENGTH_SHORT).show();
                }
                //Log.i("Reset key =>  ", String.valueOf(keyNum));
                //Log.i("Reset value =>  ", Objects.requireNonNull(Objects.requireNonNull(checkBoxList.get(keyNum)).toString()));
            }
        }
        else {
            totalPrice = 0;
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
        for (int i = 0; i < memoTable.size(); i++) {
            if (checkBoxList.containsKey(i) && checkBoxList.get(i) == null) {
                checkBoxList.remove(i);
            }
        }
    }

    public void RemoveCheckBox(int index) {
        boolean shift = false;
        int memoTableCount = memoTable.size();

        if (checkBoxList.containsKey(index)) {
            String removeMemoPrice = memoPriceTable.get(index);
            totalPrice = totalPrice - Integer.parseInt(removeMemoPrice);
            memoActivity.updateMemoPrice = String.valueOf(totalPrice);
            memoActivity.memoPrice.setText(memoActivity.updateMemoPrice);
        }

        checkBoxList.remove(index);
        for (int i=0;index < memoTableCount-1;i++,index++) {
            CheckBox cb = checkBoxList.get(index+1);

            if (i == 0) {
                shift = true;
                checkBoxList.put(index, cb);
            }
            else {
                checkBoxList.put(index, cb);
            }
        }
        if (shift && index == memoTableCount-1) {
            checkBoxList.remove(index);
        }
        for (int i=0; i<memoTableCount; i++) {
            if (checkBoxList.containsKey(i) && checkBoxList.get(i) == null) {
                checkBoxList.remove(i);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView memoListText, memoListPrice;
        private CheckBox memoCheckBox;
        private ReorderImageView reorderBtn;
        public MyViewHolderClick mListener;

        ViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);

            memoListText = itemView.findViewById(R.id.memoTextList_id);
            memoListPrice = itemView.findViewById(R.id.memoPriceList_id);
            reorderBtn = itemView.findViewById(R.id.memoReorder_id);
            memoCheckBox = itemView.findViewById(R.id.memoCheckBox_id);
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
    public void onBindViewHolder(@NonNull final MemoRecyclerAdapter.ViewHolder holder, int position) {
        String memoData = memoTable.get(position), memoPriceData = memoPriceTable.get(position);

        if (memoData != null) {
            holder.memoListText.setText(memoData);
            String price = memoPriceData + "元";
            holder.memoListPrice.setText(price);
            holder.memoCheckBox.setTag(position);
            if (checkBoxList.containsKey(position)) {
                holder.memoCheckBox.setChecked(true);
                checkBoxList.put(position, holder.memoCheckBox);
                //Log.i("onBind position =>  ", String.valueOf(position));
                //Log.i("onBind value =>  ", holder.memoCheckBox.toString());
            }
            else {
                holder.memoCheckBox.setChecked(false);
            }
        }
        else {
            Log.i("position => error", Integer.toString(position));
        }

        holder.memoCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.memoCheckBox_id) {
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.memoCheckBox_id);
                    int checkBoxNum = (int) checkBox.getTag();

                    if (checkBox.isChecked()) {
                        String addMemoPrice = memoPriceTable.get(checkBoxNum);
                        totalPrice = totalPrice + Integer.parseInt(addMemoPrice);
                        memoActivity.updateMemoPrice = String.valueOf(totalPrice);
                        memoActivity.memoPrice.setText(memoActivity.updateMemoPrice);
                        checkBoxList.put(checkBoxNum, checkBox);
                        //Log.i("onClick key =>  ", String.valueOf(checkBoxNum));
                        //Log.i("onClick value =>  ", checkBox.toString());
                    }
                    else {
                        String addMemoPrice = memoPriceTable.get(checkBoxNum);
                        totalPrice = totalPrice - Integer.parseInt(addMemoPrice);
                        memoActivity.updateMemoPrice = String.valueOf(totalPrice);
                        memoActivity.memoPrice.setText(memoActivity.updateMemoPrice);
                        checkBoxList.remove(checkBoxNum);
                    }
                }
            }
        });

        holder.reorderBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    memoActivity.ith.startDrag(holder);
                }
                else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    holder.reorderBtn.performClick();
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

}


