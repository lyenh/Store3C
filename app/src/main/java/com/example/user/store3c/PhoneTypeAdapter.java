package com.example.user.store3c;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;

public class PhoneTypeAdapter extends RecyclerView.Adapter<PhoneTypeAdapter.ViewHolder>{
    private final String[][] PhoneType;
    private final int framePosition;
    private final TabFragment tabFragment;
    private int layoutWidth;
    private final int screenWidth;
    public final int textColor = 0xFFEC0FDE, textSelectedColor = 0xFF7826EC;
    public static ArrayList<ArrayList<View>> selectedTextView = new ArrayList<ArrayList<View>>(3);
    static {
        for(int i = 0; i < 3; i++) {
            selectedTextView.add(new ArrayList<View>());
        }
    }

    PhoneTypeAdapter(String[][] PhoneType, int fragmentNumber, TabFragment f) {
        this.PhoneType = PhoneType;
        this.framePosition = fragmentNumber;
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
                tabFragment.reloadAdapter(framePosition, false, position);
                TypeName = v.findViewById(R.id.phoneTypeText_id);

                TypeName.setTextColor(textSelectedColor);
                TypeName.setTypeface(null, Typeface.BOLD_ITALIC);
                if (selectedTextView.get(framePosition).size() !=0) {
                    TextView selectedTypeName;
                    for (View view : selectedTextView.get(framePosition)) {
                        selectedTypeName = view.findViewById(R.id.phoneTypeText_id);
                        if (!selectedTypeName.getText().equals(TypeName.getText())) {
                            selectedTypeName.setTextColor(textColor);
                            selectedTypeName.setTypeface(null, Typeface.NORMAL);
                        }
                    }
                    selectedTextView.get(framePosition).clear();
                }
                selectedTextView.get(framePosition).add(v);
                //Log.i("position => ", Integer.toString(position));
                //Toast.makeText(activity, "The Type number is " + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBindViewHolder(PhoneTypeAdapter.ViewHolder holder, int position) {
        holder.TypeName.setText(PhoneType[framePosition][position]);
        layoutWidth = screenWidth / getItemCount();

        if (selectedTextView.get(framePosition).size() != 0) {
            TextView selectedTypeName;
            for (View view : selectedTextView.get(framePosition)) {
                selectedTypeName = view.findViewById(R.id.phoneTypeText_id);
                if (selectedTypeName.getText().equals(holder.TypeName.getText())) {
                    holder.TypeName.setTextColor(textSelectedColor);
                    holder.TypeName.setTypeface(null, Typeface.BOLD_ITALIC);
                    selectedTextView.get(framePosition).clear();
                    selectedTextView.get(framePosition).add(holder.itemView);
                }
            }
        }

        if (isTab) {
            if (getItemCount() < 5) {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(layoutWidth, LinearLayout.LayoutParams.MATCH_PARENT));
            }
            else {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
        }
        else {
            if (screenWidth > rotationScreenWidth && getItemCount() < 5) {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(layoutWidth, LinearLayout.LayoutParams.MATCH_PARENT));
            } else {
                holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
        }
    }

    @Override
    public int getItemCount() {
        return PhoneType[framePosition].length;
    }

}


