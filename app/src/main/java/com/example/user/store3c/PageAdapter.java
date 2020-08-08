package com.example.user.store3c;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by user on 2016/10/20.
 */

public class PageAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragments;
    //private static final String ARG_PARAM1 = "Param1";
    private static final String ARG_PARAM2 = "Param2";

    PageAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

    }

    @Override
    public Fragment getItem(int position) {
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
    public int getCount() {
        return this.fragments.size();
    }

    /*private byte [] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos =  new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,  100 , baos);
        return  baos.toByteArray();
    }*/

}
