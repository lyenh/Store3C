package com.example.user.store3c;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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
     * {@link FragmentStateAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link androidx.viewpager2.adapter.FragmentStateAdapter}.
     */
    private PagePagerAdapter mPagerAdapter;

    /**
     * The {@link ViewPager2} that will host the section contents.
     */
    private ViewPager2 mViewPager;
    private static int bookPosition;
    private static final ArrayList<String> bookPageVolume = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        Toolbar toolbar = findViewById(R.id.toolbarPage);
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
                                mPagerAdapter = new PagePagerAdapter(getSupportFragmentManager(), getLifecycle());
                                mViewPager = findViewById(R.id.container);
                                mViewPager.setAdapter(mPagerAdapter);
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
        mPagerAdapter.notifyDataSetChanged();
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
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null) {
                fragmentPosition = getArguments().getInt(ARG_SECTION_NUMBER);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_page, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

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
     * A {@link FragmentStateAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private static class PagePagerAdapter extends FragmentStateAdapter {

        public PagePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getItemCount() {
            return bookPageVolume.size();
        }
    }
}
