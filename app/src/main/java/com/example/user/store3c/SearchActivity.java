package com.example.user.store3c;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
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
    private Boolean finding = false, findingText = false, findChineseItem = false, findChineseMatching = false;
    private Boolean findEnglishItem = false, findEnglishMatching = false;
    private final int MaxStringIndex = 1000;
    private int findingIndex = MaxStringIndex;
    private SearchRecyclerAdapter adapter = null;
    private static ArrayList<ProductItem> resultTable = new ArrayList<>();
    private ArrayList<String> wordTextE, prodNameTextE;
    private DatabaseReference dishRef;
    private StorageReference mStorageRef;
    private FirebaseDatabase db = null;
    private static String DishName = "", dNumber = "", dPrice = "", dIntro = "";
    private final long ONE_MEGABYTE = 1024 * 1024;

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

    private String getMarkText(String str) {
        Pattern pc = Pattern.compile("[\u4E00-\u9FA5]");
        Pattern pe = Pattern.compile("[a-zA-Z 　]");
        String compareString, markString = "";

        for (int i=0;i<str.length();i++) {
            compareString = str.charAt(i) + "";
            if (!pc.matcher(compareString).find() && !pe.matcher(compareString).find()) {
                markString = markString.concat(compareString);
            }
        }
        return markString;
    }

    private String getChineseText(String str) {
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

    private ArrayList<String> getEnglishText(String str) {
        Pattern pe = Pattern.compile("[a-zA-Z]");
        int startPosition = MaxStringIndex, endPosition = MaxStringIndex, index = 0;
        String compareString;
        boolean startWord = false, findWord = false;
        ArrayList<String> stringList = new ArrayList<>();

        do {
            for (int i = index; i < str.length(); i++) {
                compareString = str.charAt(i) + "";
                if (pe.matcher(compareString).find()) {
                    startPosition = i;
                    startWord = true;
                    break;
                }
            }
            if (startWord) {
                for (int i = startPosition + 1; i < str.length(); i++) {
                    compareString = str.charAt(i) + "";
                    if (!pe.matcher(compareString).find()) {
                        endPosition = i;
                        break;
                    }
                }
                findWord = true;
                if (endPosition == MaxStringIndex) {
                    endPosition = str.length();
                    findWord = false;
                }
                startWord = false;
                index = endPosition;
                stringList.add(str.substring(startPosition, endPosition));
            }
            else {
                findWord = false;
            }
            endPosition = MaxStringIndex;
        } while (findWord);

        return stringList;
    }

    private String getNumberText(String str) {
        Pattern pn = Pattern.compile("[0-9]");
        String compareString, textString = "";

        for (int i=0;i<str.length();i++) {
            compareString = str.charAt(i) + "";
            if (pn.matcher(compareString).find()) {
                textString = textString.concat(compareString);
            }
        }
        return textString;
    }

    @Override
    public void onClick(View v) {
        Map<Integer, String> findText = new HashMap<>(), findPrice = new HashMap<>();
        Map<Integer, Integer> findIndex = new HashMap<>();
        ArrayList<Integer> findIndexArray = new ArrayList<>(), findIndexSorted = new ArrayList<>();
        int countChinese = 0, countEnglish = 0, countMark = 0, countMatching = 0;

        switch (v.getId()) {
            case R.id.searchBtn_id:
                dbHelper = new AccountDbAdapter(this);
                String Word = keyWord.getText().toString();
                finding = false;
                if (!Word.equals("")) {
                    if (db == null) {
                        db = FirebaseDatabase.getInstance();
                    }
                    try {
                        if (!MainActivity.setFirebaseDbPersistence) {
                            MainActivity.setFirebaseDbPersistence = true;
                            db.setPersistenceEnabled(true);
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(SearchActivity.this, "Reload data.", Toast.LENGTH_SHORT).show();
                        Log.i("Pick image timeout: " , "reload data.");
                    }
                    dishRef = db.getReference("dish");
                    Query queryDish = dishRef.child("dishName").orderByValue().equalTo(Word);
                    queryDish.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String dName = "";
                            Query queryPrice;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    if (snapshot.getValue() != null) {
                                        DishName = snapshot.getValue().toString();
                                    }
                                    if (snapshot.getKey() != null) {
                                        dName = snapshot.getKey();
                                        dNumber = getNumberText(dName);
                                    }
                                    if (!dNumber.equals("")) {
                                        final String dPriceName = "p".concat(dNumber);
                                        queryPrice = dishRef.child("dishPrice").orderByKey().equalTo(dPriceName);
                                        queryPrice.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Query queryIntro;
                                                if (snapshot.child(dPriceName).getValue() != null) {
                                                    dPrice = Objects.requireNonNull(snapshot.child(dPriceName).getValue()).toString();
                                                    final String dIntroName = "i".concat(dNumber);
                                                    queryIntro = dishRef.child("dishIntro").orderByKey().equalTo(dIntroName);
                                                    queryIntro.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Query queryImage;
                                                            if (snapshot.child(dIntroName).getValue() != null) {
                                                                dIntro = Objects.requireNonNull(snapshot.child(dIntroName).getValue()).toString();
                                                                final String dImage = "img".concat(dNumber);
                                                                queryImage = dishRef.child("dishImgName").orderByKey().equalTo(dImage);
                                                                queryImage.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        String dImgName = "";
                                                                        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
                                                                        if (snapshot.child(dImage).getValue() != null) {
                                                                            dImgName = Objects.requireNonNull(snapshot.child(dImage).getValue()).toString();
                                                                            String dishImageName = "dish/" + dImgName;
                                                                            mStorageRef.child(dishImageName).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                                @Override
                                                                                public void onSuccess(@NonNull byte[] bytes) {
                                                                                    if (bytes.length != 0) {
                                                                                        resultTable.add(new ProductItem(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), DishName, dPrice, dIntro));
                                                                                        adapter.notifyDataSetChanged();
                                                                                    }
                                                                                    else {
                                                                                        Toast.makeText(SearchActivity.this, "Database data error!", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception exception) {
                                                                                    Log.i("Firebase ==>", "Download image file fail.");
                                                                                }
                                                                            });
                                                                        }
                                                                        else {
                                                                            Toast.makeText(SearchActivity.this, "Database data error!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                                        Toast.makeText(SearchActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                            else {
                                                                Toast.makeText(SearchActivity.this, "Database data error! ", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(SearchActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                else {
                                                    Toast.makeText(SearchActivity.this, "Database data error! ", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(SearchActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(SearchActivity.this, "Database data error! ", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else {
                                if (!finding) {
                                    Toast.makeText(SearchActivity.this, "Product not find! ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SearchActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (!dbHelper.IsDbMemoEmpty()) {
                        try {
                            Cursor cursor = dbHelper.listAllMemo();
                            cursor.moveToFirst();
                            String wordTextC = getChineseText(Word);
                            wordTextE = getEnglishText(Word);
                            String wordTextM = getMarkText(Word);
                            char[] w = wordTextC.toCharArray(), p, m = wordTextM.toCharArray();
                            String mark = "", compare = "";
                            String prodName, prodNameTextC, prodNameTextM;

                            do {
                                prodName = cursor.getString(2);
                                prodNameTextC = getChineseText(prodName);
                                prodNameTextE = getEnglishText(prodName);
                                prodNameTextM = getMarkText(prodName);

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
                                            countChinese = wordTextC.length() * 2;
                                            finding = true;
                                            findingText = true;
                                            findChineseMatching = true;
                                        }
                                    }
                                    if (!findChineseItem) {
                                        p = prodNameTextC.toCharArray();
                                        for (int i=0;i<wordTextC.length();i++) {
                                            for (int j=0;j<prodNameTextC.length();j++) {
                                                if (w[i] == p[j]) {
                                                    countChinese = countChinese + 2;
                                                    finding = true;
                                                    findingText = true;
                                                    p[j] = 'f';     //finding
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (prodNameTextE.size() != 0 && wordTextE.size() != 0) {
                                    if (prodNameTextE.size() == wordTextE.size()) {
                                        findEnglishItem = true;
                                        for (int i=0;i<wordTextE.size();i++) {
                                            if (!wordTextE.get(i).equalsIgnoreCase(prodNameTextE.get(i))) {
                                                findEnglishItem = false;
                                            }
                                        }
                                        if (findEnglishItem) {
                                            countEnglish = wordTextE.size() * 2;
                                            finding = true;
                                            findingText = true;
                                            findEnglishMatching = true;
                                        }
                                    }
                                    if (!findEnglishItem) {
                                        for (int i = 0; i < wordTextE.size(); i++) {
                                            for (int j = 0; j < prodNameTextE.size(); j++) {
                                                if (wordTextE.get(i).equalsIgnoreCase(prodNameTextE.get(j))) {
                                                    countEnglish = countEnglish + 2;
                                                    finding = true;
                                                    findingText = true;
                                                    prodNameTextE.set(j, "");   //finding
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!prodNameTextM.equals("") && !wordTextM.equals("")) {
                                    p = prodNameTextM.toCharArray();
                                    for (int i=0;i<wordTextM.length();i++) {
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
                                                    countMark = countMark + 1;
                                                    finding = true;
                                                    findingText = true;
                                                    p[j-1] = 'f';   //finding
                                                    p[j] = 'f';
                                                    break;
                                                }
                                            }
                                        }
                                        else {
                                            for (int j = 0; j < prodNameTextM.length(); j++) {
                                                if (m[i] == p[j]) {
                                                    countMark = countMark + 1;
                                                    finding = true;
                                                    findingText = true;
                                                    p[j] = 'f';
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (countChinese > 0 || countMark > 0 || countEnglish > 0) {
                                    findIndexArray.add(cursor.getInt(1));
                                    findIndex.put(cursor.getInt(1), countChinese + countMark + countEnglish);
                                    findText.put(cursor.getInt(1), cursor.getString(2));
                                    findPrice.put(cursor.getInt(1), cursor.getString(3));
                                    countChinese = 0;
                                    countEnglish = 0;
                                }
                                if (wordTextC.equals("") && prodNameTextC.equals("") && findEnglishMatching) {
                                    if (countMark == 0 && countMatching == 0) {
                                        if (wordTextM.equals("") && prodNameTextM.equals("")) {
                                            findingIndex = cursor.getInt(1);
                                        }
                                        else if (findingIndex == MaxStringIndex) {
                                            findingIndex = cursor.getInt(1);
                                        }
                                    }
                                    if (countMark > countMatching) {
                                        findingIndex = cursor.getInt(1);
                                        countMatching = countMark;
                                    }
                                }
                                if (wordTextE.size() == 0 && prodNameTextE.size() == 0 && findChineseMatching) {
                                    if (countMark == 0 && countMatching == 0) {
                                        if (wordTextM.equals("") && prodNameTextM.equals("")) {
                                            findingIndex = cursor.getInt(1);
                                        }
                                        else if (findingIndex == MaxStringIndex) {
                                            findingIndex = cursor.getInt(1);
                                        }
                                    }
                                    if (countMark > countMatching) {
                                        findingIndex = cursor.getInt(1);
                                        countMatching = countMark;
                                    }
                                }
                                if (!wordTextC.equals("") && wordTextE.size() != 0 && findChineseMatching && findEnglishMatching) {
                                    if (countMark == 0 && countMatching == 0) {
                                        if (wordTextM.equals("") && prodNameTextM.equals("")) {
                                            findingIndex = cursor.getInt(1);
                                        }
                                        else if (findingIndex == MaxStringIndex) {
                                            findingIndex = cursor.getInt(1);
                                        }
                                    }
                                    if (countMark > countMatching) {
                                        findingIndex = cursor.getInt(1);
                                        countMatching = countMark;
                                    }
                                }
                                countMark = 0;
                                findChineseMatching = false;
                                findEnglishMatching = false;
                                findChineseItem = false;
                                findEnglishItem = false;
                                prodNameTextE.clear();
                                cursor.moveToNext();
                            } while (!cursor.isAfterLast());
                            if (findingText) {
                                if (findingIndex != MaxStringIndex) {
                                    resultTable.add(new ProductItem(BitmapFactory.decodeResource(getResources(), R.drawable.store_item), findText.get(findingIndex), findPrice.get(findingIndex), "待採購產品"));
                                }
                                int index, value;
                                boolean insert = false;
                                try {
                                    for (int i=0;i<findIndexArray.size();i++) {
                                        index = findIndexArray.get(i);
                                        value = findIndex.get(index);
                                        if (i==0) {
                                            findIndexSorted.add(index);
                                        }
                                        else {
                                            for (int j=0; j<findIndexSorted.size(); j++) {
                                                if (value > findIndex.get(findIndexSorted.get(j))) {
                                                    findIndexSorted.add(j, index);
                                                    insert = true;
                                                    break;
                                                }
                                            }
                                            if (!insert) {
                                                findIndexSorted.add(index);
                                            }
                                        }
                                        insert = false;
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(SearchActivity.this, "Sort error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                for (int i=0; i<findIndexSorted.size();i++) {
                                    if (findIndexSorted.get(i) != findingIndex) {
                                        resultTable.add(new ProductItem(BitmapFactory.decodeResource(getResources(), R.drawable.store_item), findText.get(findIndexSorted.get(i)), findPrice.get(findIndexSorted.get(i)), "待採購產品"));
                                    }
                                }
                                adapter.notifyDataSetChanged();
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
                    findingText = false;
                    findChineseItem = false;
                    findEnglishItem = false;
                    findingIndex = MaxStringIndex;
                    findIndex.clear();
                    findText.clear();
                    findPrice.clear();
                    findIndexArray.clear();
                    findIndexSorted.clear();
                    wordTextE.clear();
                    prodNameTextE.clear();
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
