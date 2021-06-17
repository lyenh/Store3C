package com.example.user.store3c;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.view.ViewGroup;

import java.util.List;

/**
 * Created by user on 2016/10/20.
 */

public class PageAdapter extends FragmentStateAdapter {
    private List<Fragment> fragments;
    //private static final String ARG_PARAM1 = "Param1";
    private static final String ARG_PARAM2 = "Param2";

    PageAdapter(FragmentManager fm, List<Fragment> fragments, @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragmentItem;
        fragmentItem = this.fragments.get(position);
        //Bundle b = new Bundle();
        //b.putByteArray("param1", Bitmap2Bytes(imageList.get(position)));
        //b.putString(ARG_PARAM2, "frame" + ++position);
        //fragmentItem.setArguments(b);

        synchronized (fragmentItem.getArguments()) {
            fragmentItem.getArguments().putString(ARG_PARAM2, "frame" + ++position);
        }

        return fragmentItem;
    }

    @Override
    public int getItemCount() {
        return this.fragments.size();
    }

    /*private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }*/

}
