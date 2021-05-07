package com.example.user.store3c;

import android.content.Intent;
import android.database.CharArrayBuffer;
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Boolean finding = false, findingChinese = false, findChineseItem = false, findMarkItem = false;
    private int MaxStringIndex = 1000;
    private int findingIndex = MaxStringIndex;
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

    public String getMarkText(String str) {
        Pattern pc = Pattern.compile("[\u4E00-\u9FA5]");
        Pattern pe = Pattern.compile("[a-zA-Z]");
        String compareString, markString = "";

        for (int i=0;i<str.length();i++) {
            compareString = str.charAt(i) + "";
            if (!pc.matcher(compareString).find() && !pe.matcher(compareString).find()) {
                markString = markString.concat(compareString);
            }
        }
        return markString;
    }

    public String getChineseText(String str) {
        Pattern pc = Pattern.compile("[\u4E00-\u9FA5]");
        String compareString, textString = "";

        for (int i=0;i<str.length();i++) {
            compareString = str.charAt(i) + "";
            if (pc.matcher(compareString).find()) {
                textString = textString.concat(compareString);
            }
        }
        return textString;
    }

    public String getEnglishText(String str) {
        Pattern pe = Pattern.compile("[a-zA-Z]");
        int startPosition = MaxStringIndex, endPosition = MaxStringIndex;
        String compareString;
        boolean startWord = false;

        for (int i=0;i<str.length();i++) {
            compareString = str.charAt(i) + "";
            if (pe.matcher(compareString).find()) {
                startPosition = i;
                startWord = true;
                break;
            }
        }
        if (startWord) {
            for (int i=startPosition + 1;i<str.length();i++) {
                compareString = str.charAt(i) + "";
                if (!pe.matcher(compareString).find()) {
                    endPosition = i;
                    break;
                }
            }
            if (endPosition == MaxStringIndex) {
                endPosition = str.length();
            }
        }
        if (startPosition != MaxStringIndex) {
            return str.substring(startPosition, endPosition);
        }
        else {
            return "";
        }
    }

    @Override
    public void onClick(View v) {
        SortedMap<Integer, String> findText = new TreeMap<>(), findPrice = new TreeMap<>();
        ArrayList<Integer> findIndex = new ArrayList<>();

        switch (v.getId()) {
            case R.id.searchBtn_id:
                dbHelper = new AccountDbAdapter(this);
                String Word = keyWord.getText().toString();
                if (!Word.equals("")) {


                    if (!dbHelper.IsDbMemoEmpty()) {
                        try {
                            Cursor cursor = dbHelper.listAllMemo();
                            cursor.moveToFirst();
                            String wordTextC = getChineseText(Word);
                            String wordTextE = getEnglishText(Word);
                            String wordTextM = getMarkText(Word);
                            char[] w = wordTextC.toCharArray(), p, m = wordTextM.toCharArray();
                            String mark = "", compare = "";

                            do {
                                String prodName = cursor.getString(2);
                                String prodNameTextC = getChineseText(prodName);
                                String prodNameTextE = getEnglishText(prodName);
                                String prodNameTextM = getMarkText(prodName);

                                if (!prodNameTextC.equals("") && !wordTextC.equals("")) {
                                    if(wordTextC.length() == prodNameTextC.length()) {
                                        p = prodNameTextC.toCharArray();
                                        findChineseItem = true;
                                        for (int i=0;i<prodNameTextC.length();i++){
                                            if (w[i] != p[i]) {
                                                findChineseItem = false;
                                            }
                                        }
                                        if (findChineseItem) {
                                            findingIndex = cursor.getInt(1);
                                            findIndex.add(findingIndex);
                                            findText.put(findingIndex, cursor.getString(2));
                                            findPrice.put(findingIndex, cursor.getString(3));
                                            finding = true;
                                            findingChinese = true;
                                        }
                                    }
                                    if (!findChineseItem) {
                                        p = prodNameTextC.toCharArray();
                                        for (int i=0;i<wordTextC.length();i++) {
                                            if (findChineseItem)
                                                break;
                                            for (int j=0;j<prodNameTextC.length();j++) {
                                                if (w[i] == p[j]) {
                                                    findIndex.add(cursor.getInt(1));
                                                    findText.put(cursor.getInt(1), cursor.getString(2));
                                                    findPrice.put(cursor.getInt(1), cursor.getString(3));
                                                    findChineseItem = true;
                                                    finding = true;
                                                    findingChinese = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                else if (!prodNameTextE.equals("") && !wordTextE.equals("")) {
                                    if (wordTextE.equalsIgnoreCase(prodNameTextE)) {
                                        Toast.makeText(SearchActivity.this, "find product: " + Word, Toast.LENGTH_SHORT).show();
                                        finding = true;
                                        resultTable.add(new ProductItem(BitmapFactory.decodeResource(getResources(), R.drawable.store_item), cursor.getString(2), cursor.getString(3), "待採購產品"));
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                if (!prodNameTextM.equals("") && !wordTextM.equals("")) {
                                    p = prodNameTextM.toCharArray();
                                    for (int i=0;i<wordTextM.length();i++) {
                                        if (findMarkItem)
                                            break;
                                        if (Character.isSurrogate(m[i])) {
                                            mark = wordTextM.substring(i,i+2);
                                            i = i + 1;
                                            for (int j = 0; j < prodNameTextM.length(); j++) {
                                                if (Character.isSurrogate(p[j])) {
                                                    compare = prodNameTextM.substring(j, j + 2);
                                                    j = j + 1;
                                                }
                                                else {
                                                    continue;
                                                }
                                                if (mark.equals(compare)) {
                                                    findIndex.add(cursor.getInt(1));
                                                    findText.put(cursor.getInt(1), cursor.getString(2));
                                                    findPrice.put(cursor.getInt(1), cursor.getString(3));
                                                    findMarkItem = true;
                                                    finding = true;
                                                    findingChinese = true;
                                                    break;
                                                }
                                            }
                                        }
                                        else {
                                            for (int j = 0; j < prodNameTextM.length(); j++) {
                                                if (m[i] == p[j]) {
                                                    findIndex.add(cursor.getInt(1));
                                                    findText.put(cursor.getInt(1), cursor.getString(2));
                                                    findPrice.put(cursor.getInt(1), cursor.getString(3));
                                                    findMarkItem = true;
                                                    finding = true;
                                                    findingChinese = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                findChineseItem = false;
                                findMarkItem = false;
                                cursor.moveToNext();
                            } while (!cursor.isAfterLast());
                            if (findingChinese) {
                                if (findingIndex != MaxStringIndex) {
                                    resultTable.add(new ProductItem(BitmapFactory.decodeResource(getResources(), R.drawable.store_item), findText.get(findingIndex), findPrice.get(findingIndex), "待採購產品"));
                                }
                                for (int i=0;i<findIndex.size();i++) {
                                    if (findIndex.get(i) != findingIndex) {
                                        resultTable.add(new ProductItem(BitmapFactory.decodeResource(getResources(), R.drawable.store_item), findText.get(findIndex.get(i)), findPrice.get(findIndex.get(i)), "待採購產品"));
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
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
                    findingChinese = false;
                    findChineseItem = false;
                    findMarkItem = false;
                    findingIndex = MaxStringIndex;
                    findIndex.clear();
                    findText.clear();
                    findPrice.clear();
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
