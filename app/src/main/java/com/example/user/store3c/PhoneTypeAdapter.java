package com.example.user.store3c;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;

public class PhoneTypeAdapter extends RecyclerView.Adapter<PhoneTypeAdapter.ViewHolder>{
    private String[][] PhoneType;
    private int typeNumber;
    private TabFragment tabFragment;
    int layoutWidth, screenWidth;

    PhoneTypeAdapter(String[][] PhoneType, int typeNumber, TabFragment f) {
        this.PhoneType = PhoneType;
        this.typeNumber = typeNumber;
        this.tabFragment = f;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        layoutWidth = screenWidth / getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView TypeName;
        private final LinearLayout LayoutWidth;
        public MyViewHolderClick mListener;

        private ViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);

            TypeName = itemView.findViewById(R.id.phoneTypeText_id);
            LayoutWidth = itemView.findViewById(R.id.phoneTypeLayout_id);
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
    public @NonNull PhoneTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.phone_type_item_view, parent, false);

        return new ViewHolder(view, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                tabFragment.reloadAdapter(typeNumber, false, position);
                //Log.i("position => ", Integer.toString(position));
                //Toast.makeText(activity, "The Type number is " + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBindViewHolder(PhoneTypeAdapter.ViewHolder holder, int position) {
        holder.TypeName.setText(PhoneType[typeNumber][position]);
        if (isTab) {
            if (screenWidth > rotationTabScreenWidth && getItemCount() < 5) {
                holder.LayoutWidth.setLayoutParams(new LinearLayout.LayoutParams(layoutWidth, 130));
            } else {
                holder.LayoutWidth.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 130));
            }
        }
        else {
            if (screenWidth > rotationScreenWidth && getItemCount() < 5) {
                holder.LayoutWidth.setLayoutParams(new LinearLayout.LayoutParams(layoutWidth, 80));
            } else {
                holder.LayoutWidth.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 80));
            }
        }
    }

    @Override
    public int getItemCount() {
        return PhoneType[typeNumber].length;
    }

}


