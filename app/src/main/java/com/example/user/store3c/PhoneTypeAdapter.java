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
    private int typeNumber, phoneTypeRecyclerViewWidth;
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
        private final LinearLayout linearLayout;
        public MyViewHolderClick mListener;

        private ViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);

            TypeName = itemView.findViewById(R.id.phoneTypeText_id);
            linearLayout = itemView.findViewById(R.id.phoneTypeLayout_id);
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
                TextView TypeName;
                tabFragment.reloadAdapter(typeNumber, false, position);
                TypeName = v.findViewById(R.id.phoneTypeText_id);

                TypeName.setSelected(true);
                TypeName.setHighlightColor(67);

                //Log.i("position => ", Integer.toString(position));
                //Toast.makeText(activity, "The Type number is " + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBindViewHolder(PhoneTypeAdapter.ViewHolder holder, int position) {
        holder.TypeName.setText(PhoneType[typeNumber][position]);
        layoutWidth = screenWidth / getItemCount();
        if (isTab) {
            if (getItemCount() < 5) {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(layoutWidth, LinearLayout.LayoutParams.MATCH_PARENT));
            }
            else {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
        }
        else {
            if (screenWidth > rotationTabScreenWidth && getItemCount() < 5) {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(layoutWidth, LinearLayout.LayoutParams.MATCH_PARENT));
            } else {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
        }
    }

    @Override
    public int getItemCount() {
        return PhoneType[typeNumber].length;
    }

}


