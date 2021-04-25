package com.example.user.store3c;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    private String menu_item;
    private EditText keyWord;
    private RecyclerView SearchRecyclerView;
    private AccountDbAdapter dbHelper = null;
    private Boolean finding = false;
    private SearchRecyclerAdapter adapter = null;
    private static ArrayList<ProductItem> resultTable = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbarSearch);
        setSupportActionBar(toolbar);
        String edit_Title = "查詢產品";
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.store_logo);
            getSupportActionBar().setTitle(edit_Title);
        }

        Button searchBtn, searchRtn, searchClearBtn;
        Intent intentItem = getIntent();
        Bundle bundleItem = intentItem.getExtras();

        if (bundleItem != null) {
            menu_item = bundleItem.getString("Menu");
        }
        searchBtn = findViewById(R.id.searchBtn_id);
        searchRtn = findViewById(R.id.searchRtn_id);
        searchClearBtn = findViewById(R.id.searchClear_id);
        keyWord = findViewById(R.id.searchWord_id);
        SearchRecyclerView = findViewById(R.id.searchRecyclerView_id);

        searchBtn.setOnClickListener(this);
        searchRtn.setOnClickListener(this);
        searchClearBtn.setOnClickListener(this);
        adapter = new SearchRecyclerAdapter(SearchActivity.this, resultTable, menu_item);
        SearchRecyclerView.setAdapter(adapter);
        SearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchBtn_id:
                dbHelper = new AccountDbAdapter(this);
                String Word = keyWord.getText().toString();
                if (!Word.equals("")) {
                    if (!dbHelper.IsDbMemoEmpty()) {
                        try {
                            Cursor cursor = dbHelper.listAllMemo();
                            cursor.moveToFirst();
                            do {
                                if (Word.equalsIgnoreCase(cursor.getString(2))) {
                                    Toast.makeText(SearchActivity.this, "find product: " + Word, Toast.LENGTH_SHORT).show();
                                    finding = true;
                                    resultTable.add(new ProductItem(BitmapFactory.decodeResource(getResources(), R.drawable.store_item), cursor.getString(2), cursor.getString(3), "待採購產品"));
                                    adapter.notifyDataSetChanged();
                                }
                                cursor.moveToNext();
                            } while (!cursor.isAfterLast());
                            if (!finding) {
                                Toast.makeText(SearchActivity.this, "Product not find !", Toast.LENGTH_SHORT).show();
                            }
                            keyWord.setText("");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Toast.makeText(SearchActivity.this, "db get error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Toast.makeText(SearchActivity.this, "Input can't be empty.", Toast.LENGTH_SHORT).show();
                }
                if (dbHelper != null) {
                    dbHelper.close();
                    finding = false;
                }
                break;
            case R.id.searchRtn_id:
                onBackPressed();
                break;
            case R.id.searchClear_id:
                resultTable.clear();
                adapter.notifyDataSetChanged();
                break;
            default:
                Toast.makeText(SearchActivity.this, "Un know button", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        switch (menu_item) {
            case "DISH":
                intent.setClass(SearchActivity.this, MainActivity.class);
                break;
            case "CAKE":
                intent.setClass(SearchActivity.this, CakeActivity.class);
                break;
            case "PHONE":
                intent.setClass(SearchActivity.this, PhoneActivity.class);
                break;
            case "CAMERA":
                intent.setClass(SearchActivity.this, CameraActivity.class);
                break;
            case "BOOK":
                intent.setClass(SearchActivity.this, BookActivity.class);
                break;
            default:
                Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                intent.setClass(SearchActivity.this, MainActivity.class);
        }

        startActivity(intent);
        SearchActivity.this.finish();

    }
}
