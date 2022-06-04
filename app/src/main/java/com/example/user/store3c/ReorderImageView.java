package com.example.user.store3c;


import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

public class ReorderImageView extends AppCompatImageView {
    public ReorderImageView(Context context) {
        super(context);
    }

    public ReorderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:   //press
                return true;

            case MotionEvent.ACTION_UP:     //click
                performClick();
                return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();

        Toast.makeText(getContext(), "按住可移動!", Toast.LENGTH_SHORT).show();
        return true;
    }

}
