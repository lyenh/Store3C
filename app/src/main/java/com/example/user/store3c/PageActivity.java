package com.example.user.store3c;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.user.store3c.BookActivity.BookData;

public class PageActivity extends AppCompatActivity {

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link androidx.fragment.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static int bookPosition;
    private static ArrayList<String> bookPageVolume = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.store_logo);
        }
        Intent Intent = getIntent();
        Bundle bundle = Intent.getExtras();

        try {
            if (bundle != null) {
                bookPosition = bundle.getInt("Position");
            }
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://apptech.website/store3c/query.php";

            bookPageVolume.clear();
            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST, url, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            String pageUrl = "http://apptech.website/store3c/image/book/";
                            try {
                                JSONArray jArray = new JSONArray (response);
                                if (!jArray.isNull(0)) {
                                    JSONObject book = jArray.getJSONObject(0);
                                    if (book != null) {
                                        for (int i = 0; i < book.getInt("pageAmount"); i++) {
                                            String pageNumber = "page" + (i + 1);
                                            bookPageVolume.add(pageUrl + book.getString(pageNumber));
                                        }
                                    }
                                    Log.v("data=", response);
                                }
                                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
                                mViewPager = findViewById(R.id.container);
                                mViewPager.setAdapter(mSectionsPagerAdapter);
                            }catch (JSONException e) {
                                Toast.makeText(PageActivity.this, "getJsonObject error :  " + e.getClass().getName(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            Toast.makeText(PageActivity.this, "StringRequest error :  " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            error.printStackTrace();
                        }

                    })
                    {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> map = new HashMap<>();
                            int value = bookPosition + 1;
                            String jsonValue = String.valueOf(value);
                            map.put("bookSNumber", jsonValue);
                            return map;
                        }

                    };
            queue.getCache().clear();
            queue.add(stringRequest);
        } catch (Throwable e) {
            Toast.makeText(PageActivity.this, "error message:  " + e.getClass().getName(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig){
        mSectionsPagerAdapter.notifyDataSetChanged();
        Log.v("===>","ORIENTATION");
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(PageActivity.this, BookActivity.class);
        startActivity(intent);
        PageActivity.this.finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_return) {
            Intent intentItem = new Intent();
            intentItem.setClass(PageActivity.this, BookActivity.class);
            startActivity(intentItem);
            PageActivity.this.finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private int fragmentPosition;
        private TextView bookName;
        private ImageView bookImg, bookPage;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_page, container, false);

            if (getArguments() != null) {
                fragmentPosition = getArguments().getInt(ARG_SECTION_NUMBER);
            }
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (getView() != null) {
                bookImg = getView().findViewById(R.id.bookImgPage_id);
                bookName = getView().findViewById(R.id.bookNamePage_id);
                bookPage = getView().findViewById(R.id.bookPage_id);
            }
            bookImg.setImageBitmap(BookData.get(bookPosition).getImg());
            bookName.setText(BookData.get(bookPosition).getName());

            Glide.with(this)
                    .load(bookPageVolume.get(fragmentPosition-1))
                    .into(bookPage);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private static class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public @NonNull Fragment getItem( int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show total pages amount.
            return bookPageVolume.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
                case 3:
                    return "SECTION 4";
                case 4:
                    return "SECTION 5";
                default:
                    return  "SECTION";
            }

        }
    }
}
