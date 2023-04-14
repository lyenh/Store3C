package com.example.user.store3c;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
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

import java.lang.ref.WeakReference;
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
    private String menu_item = "DISH", up_menu_item = "";
    private EditText keyWord;
    private RecyclerView SearchRecyclerView;
    private AccountDbAdapter dbHelper = null;
    private static Boolean finding = false;
    private Boolean findingText = false, findChineseItem = false, findChineseMatching = false;
    private Boolean findEnglishItem = false, findEnglishMatching = false;
    private final int MaxStringIndex = 1000;
    private int findingIndex = MaxStringIndex;
    private SearchRecyclerAdapter adapter = null;
    private static ArrayList<ProductItem> resultTable = new ArrayList<>();
    private ArrayList<String> wordTextE, prodNameTextE;
    private DatabaseReference dishRef, cakeRef, phoneRef, cameraRef, bookRef;
    private StorageReference mStorageRef;
    private static String DishName = "", dishNumber = "", dishPrice = "", dishIntro = "";
    private static String CakeName = "", cakeNumber = "", cakePrice = "", cakeIntro = "";
    private static String PhoneName = "", phoneNumber = "", phonePrice = "", phoneIntro = "";
    private static String CameraName = "", cameraNumber = "", cameraPrice = "", cameraIntro = "";
    private static String BookName = "", bookNumber = "", bookPrice = "", bookIntro = "";
    private final long ONE_MEGABYTE = 1024 * 1024;
    private HandlerSearch handlerSearch;
    private static int searchComplete = 0;
    private final static int productMenuAmount = 5;
    private static boolean findProduct = false;

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
            if (bundleItem.getString("upMenu") != null) {
                up_menu_item = bundleItem.getString("upMenu");
            }
        }
        searchBtn = findViewById(R.id.searchBtn_id);
        searchRtn = findViewById(R.id.searchRtn_id);
        searchClearBtn = findViewById(R.id.searchClear_id);
        keyWord = findViewById(R.id.searchWord_id);
        SearchRecyclerView = findViewById(R.id.searchRecyclerView_id);

        searchBtn.setOnClickListener(this);
        searchRtn.setOnClickListener(this);
        searchClearBtn.setOnClickListener(this);
        adapter = new SearchRecyclerAdapter(SearchActivity.this, resultTable, menu_item, up_menu_item);
        SearchRecyclerView.setAdapter(adapter);
        SearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        handlerSearch = new HandlerSearch(SearchActivity.this);
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

    static class HandlerSearch extends Handler {
        private final WeakReference<SearchActivity> weakRefActivity;

        HandlerSearch(SearchActivity searchActivity) {
            weakRefActivity = new WeakReference<>(searchActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            String messageSearchResult = (String)msg.obj;
            if (messageSearchResult.equals("FindOnlineProduct")) {
                findProduct = true;
                searchComplete++;
            }
            else {
                searchComplete++;
            }
            if (searchComplete == productMenuAmount) {
                if (!findProduct && !finding) {
                    SearchActivity searchActivity = weakRefActivity.get();
                    if (searchActivity != null) {
                        Toast.makeText(searchActivity, "Product not find! ", Toast.LENGTH_SHORT).show();
                    }
                }
                searchComplete = 0;
                findProduct = false;
            }
        }
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
                    dishRef = FirebaseDatabase.getInstance().getReference("dish");
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
                                        dishNumber = getNumberText(dName);
                                    }
                                    if (!dishNumber.equals("")) {
                                        final String dPriceName = "p".concat(dishNumber);
                                        queryPrice = dishRef.child("dishPrice").orderByKey().equalTo(dPriceName);
                                        queryPrice.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Query queryIntro;
                                                if (snapshot.child(dPriceName).getValue() != null) {
                                                    dishPrice = Objects.requireNonNull(snapshot.child(dPriceName).getValue()).toString();
                                                    final String dIntroName = "i".concat(dishNumber);
                                                    queryIntro = dishRef.child("dishIntro").orderByKey().equalTo(dIntroName);
                                                    queryIntro.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Query queryImage;
                                                            if (snapshot.child(dIntroName).getValue() != null) {
                                                                dishIntro = Objects.requireNonNull(snapshot.child(dIntroName).getValue()).toString();
                                                                final String dImage = "img".concat(dishNumber);
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
                                                                                        resultTable.add(new ProductItem(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), DishName, dishPrice, dishIntro));
                                                                                        adapter.notifyDataSetChanged();
                                                                                        Message msg = new Message();
                                                                                        msg.obj = "FindOnlineProduct";
                                                                                        handlerSearch.sendMessage(msg);
                                                                                    } else {
                                                                                        Toast.makeText(SearchActivity.this, "Database data error!", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception exception) {
                                                                                    Log.i("Firebase ==>", "Download image file fail.");
                                                                                }
                                                                            });
                                                                        } else {
                                                                            Toast.makeText(SearchActivity.this, "Database data error!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                                        Toast.makeText(SearchActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            } else {
                                                                Toast.makeText(SearchActivity.this, "Database data error! ", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(SearchActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(SearchActivity.this, "Database data error! ", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(SearchActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SearchActivity.this, "Database data error! ", Toast.LENGTH_SHORT).show();
                                    }
                                    if (dataSnapshot.getChildrenCount() > 1) {
                                        break;
                                    }
                                }
                            }
                            else {
                                Message msg = new Message();
                                msg.obj = "ProductNotFind";
                                handlerSearch.sendMessage(msg);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SearchActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    cakeRef = FirebaseDatabase.getInstance().getReference("cake");
                    Query queryCake = cakeRef.child("cakeName").orderByValue().equalTo(Word);
                    queryCake.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String cName = "";
                            Query queryPrice;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    if (snapshot.getValue() != null) {
                                        CakeName = snapshot.getValue().toString();
                                    }
                                    if (snapshot.getKey() != null) {
                                        cName = snapshot.getKey();
                                        cakeNumber = getNumberText(cName);
                                    }
                                    if (!cakeNumber.equals("")) {
                                        final String cPriceName = "p".concat(cakeNumber);
                                        queryPrice = cakeRef.child("cakePrice").orderByKey().equalTo(cPriceName);
                                        queryPrice.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Query queryIntro;
                                                if (snapshot.child(cPriceName).getValue() != null) {
                                                    cakePrice = Objects.requireNonNull(snapshot.child(cPriceName).getValue()).toString();
                                                    final String cIntroName = "i".concat(cakeNumber);
                                                    queryIntro = cakeRef.child("cakeIntro").orderByKey().equalTo(cIntroName);
                                                    queryIntro.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Query queryImage;
                                                            if (snapshot.child(cIntroName).getValue() != null) {
                                                                cakeIntro = Objects.requireNonNull(snapshot.child(cIntroName).getValue()).toString();
                                                                final String cImage = "img".concat(cakeNumber);
                                                                queryImage = cakeRef.child("cakeImgName").orderByKey().equalTo(cImage);
                                                                queryImage.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        String cImgName = "";
                                                                        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
                                                                        if (snapshot.child(cImage).getValue() != null) {
                                                                            cImgName = Objects.requireNonNull(snapshot.child(cImage).getValue()).toString();
                                                                            String cakeImageName = "cake/" + cImgName;
                                                                            mStorageRef.child(cakeImageName).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                                @Override
                                                                                public void onSuccess(@NonNull byte[] bytes) {
                                                                                    if (bytes.length != 0) {
                                                                                        resultTable.add(new ProductItem(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), CakeName, cakePrice, cakeIntro));
                                                                                        adapter.notifyDataSetChanged();
                                                                                        Message msg = new Message();
                                                                                        msg.obj = "FindOnlineProduct";
                                                                                        handlerSearch.sendMessage(msg);
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
                                    if (dataSnapshot.getChildrenCount() > 1) {
                                        break;
                                    }
                                }
                            }
                            else {
                                Message msg = new Message();
                                msg.obj = "ProductNotFind";
                                handlerSearch.sendMessage(msg);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SearchActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    phoneRef = FirebaseDatabase.getInstance().getReference("phone");
                    Query queryPhone = phoneRef.child("phoneName").orderByValue().equalTo(Word);
                    queryPhone.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String pName = "";
                            Query queryPrice;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    if (snapshot.getValue() != null) {
                                        PhoneName = snapshot.getValue().toString();
                                    }
                                    if (snapshot.getKey() != null) {
                                        pName = snapshot.getKey();
                                        phoneNumber = getNumberText(pName);
                                    }
                                    if (!phoneNumber.equals("")) {
                                        final String pPriceName = "p".concat(phoneNumber);
                                        queryPrice = phoneRef.child("phonePrice").orderByKey().equalTo(pPriceName);
                                        queryPrice.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Query queryIntro;
                                                if (snapshot.child(pPriceName).getValue() != null) {
                                                    phonePrice = Objects.requireNonNull(snapshot.child(pPriceName).getValue()).toString();
                                                    final String pIntroName = "i".concat(phoneNumber);
                                                    queryIntro = phoneRef.child("phoneIntro").orderByKey().equalTo(pIntroName);
                                                    queryIntro.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Query queryImage;
                                                            if (snapshot.child(pIntroName).getValue() != null) {
                                                                phoneIntro = Objects.requireNonNull(snapshot.child(pIntroName).getValue()).toString();
                                                                final String pImage = "img".concat(phoneNumber);
                                                                queryImage = phoneRef.child("phoneImgName").orderByKey().equalTo(pImage);
                                                                queryImage.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        String pImgName = "";
                                                                        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
                                                                        if (snapshot.child(pImage).getValue() != null) {
                                                                            pImgName = Objects.requireNonNull(snapshot.child(pImage).getValue()).toString();
                                                                            String phoneImageName = "phone/" + pImgName;
                                                                            mStorageRef.child(phoneImageName).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                                @Override
                                                                                public void onSuccess(@NonNull byte[] bytes) {
                                                                                    if (bytes.length != 0) {
                                                                                        resultTable.add(new ProductItem(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), PhoneName, phonePrice, phoneIntro));
                                                                                        adapter.notifyDataSetChanged();
                                                                                        Message msg = new Message();
                                                                                        msg.obj = "FindOnlineProduct";
                                                                                        handlerSearch.sendMessage(msg);
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
                                    if (dataSnapshot.getChildrenCount() > 1) {
                                        break;
                                    }
                                }
                            }
                            else {
                                Message msg = new Message();
                                msg.obj = "ProductNotFind";
                                handlerSearch.sendMessage(msg);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SearchActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    cameraRef = FirebaseDatabase.getInstance().getReference("camera");
                    Query queryCamera = cameraRef.child("cameraName").orderByValue().equalTo(Word);
                    queryCamera.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String caName = "";
                            Query queryPrice;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    if (snapshot.getValue() != null) {
                                        CameraName = snapshot.getValue().toString();
                                    }
                                    if (snapshot.getKey() != null) {
                                        caName = snapshot.getKey();
                                        cameraNumber = getNumberText(caName);
                                    }
                                    if (!cameraNumber.equals("")) {
                                        final String cameraPriceName = "p".concat(cameraNumber);
                                        queryPrice = cameraRef.child("cameraPrice").orderByKey().equalTo(cameraPriceName);
                                        queryPrice.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Query queryIntro;
                                                if (snapshot.child(cameraPriceName).getValue() != null) {
                                                    cameraPrice = Objects.requireNonNull(snapshot.child(cameraPriceName).getValue()).toString();
                                                    final String cameraIntroName = "i".concat(cameraNumber);
                                                    queryIntro = cameraRef.child("cameraIntro").orderByKey().equalTo(cameraIntroName);
                                                    queryIntro.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Query queryImage;
                                                            if (snapshot.child(cameraIntroName).getValue() != null) {
                                                                cameraIntro = Objects.requireNonNull(snapshot.child(cameraIntroName).getValue()).toString();
                                                                final String cameraImage = "img".concat(cameraNumber);
                                                                queryImage = cameraRef.child("cameraImgName").orderByKey().equalTo(cameraImage);
                                                                queryImage.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        String cameraImgName = "";
                                                                        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
                                                                        if (snapshot.child(cameraImage).getValue() != null) {
                                                                            cameraImgName = Objects.requireNonNull(snapshot.child(cameraImage).getValue()).toString();
                                                                            String cameraImageName = "camera/" + cameraImgName;
                                                                            mStorageRef.child(cameraImageName).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                                @Override
                                                                                public void onSuccess(@NonNull byte[] bytes) {
                                                                                    if (bytes.length != 0) {
                                                                                        resultTable.add(new ProductItem(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), CameraName, cameraPrice, cameraIntro));
                                                                                        adapter.notifyDataSetChanged();
                                                                                        Message msg = new Message();
                                                                                        msg.obj = "FindOnlineProduct";
                                                                                        handlerSearch.sendMessage(msg);
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
                                    if (dataSnapshot.getChildrenCount() > 1) {
                                        break;
                                    }
                                }
                            }
                            else {
                                Message msg = new Message();
                                msg.obj = "ProductNotFind";
                                handlerSearch.sendMessage(msg);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(SearchActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    bookRef = FirebaseDatabase.getInstance().getReference("book");
                    Query queryBook = bookRef.child("bookName").orderByValue().equalTo(Word);
                    queryBook.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String bName = "";
                            Query queryPrice;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    if (snapshot.getValue() != null) {
                                        BookName = snapshot.getValue().toString();
                                    }
                                    if (snapshot.getKey() != null) {
                                        bName = snapshot.getKey();
                                        bookNumber = getNumberText(bName);
                                    }
                                    if (!bookNumber.equals("")) {
                                        final String bPriceName = "p".concat(bookNumber);
                                        queryPrice = bookRef.child("bookPrice").orderByKey().equalTo(bPriceName);
                                        queryPrice.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Query queryIntro;
                                                if (snapshot.child(bPriceName).getValue() != null) {
                                                    bookPrice = Objects.requireNonNull(snapshot.child(bPriceName).getValue()).toString();
                                                    final String bIntroName = "i".concat(bookNumber);
                                                    queryIntro = bookRef.child("bookIntro").orderByKey().equalTo(bIntroName);
                                                    queryIntro.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Query queryImage;
                                                            if (snapshot.child(bIntroName).getValue() != null) {
                                                                bookIntro = Objects.requireNonNull(snapshot.child(bIntroName).getValue()).toString();
                                                                final String bImage = "img".concat(bookNumber);
                                                                queryImage = bookRef.child("bookImgName").orderByKey().equalTo(bImage);
                                                                queryImage.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        String bImgName = "";
                                                                        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://store3c-137123.appspot.com");
                                                                        if (snapshot.child(bImage).getValue() != null) {
                                                                            bImgName = Objects.requireNonNull(snapshot.child(bImage).getValue()).toString();
                                                                            String bookImageName = "book/" + bImgName;
                                                                            mStorageRef.child(bookImageName).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                                @Override
                                                                                public void onSuccess(@NonNull byte[] bytes) {
                                                                                    if (bytes.length != 0) {
                                                                                        resultTable.add(new ProductItem(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), BookName, bookPrice, bookIntro));
                                                                                        adapter.notifyDataSetChanged();
                                                                                        Message msg = new Message();
                                                                                        msg.obj = "FindOnlineProduct";
                                                                                        handlerSearch.sendMessage(msg);
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
                                    if (dataSnapshot.getChildrenCount() > 1) {
                                        break;
                                    }
                                }
                            }
                            else {
                                Message msg = new Message();
                                msg.obj = "ProductNotFind";
                                handlerSearch.sendMessage(msg);
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
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Toast.makeText(SearchActivity.this, "db get error", Toast.LENGTH_SHORT).show();
                        }
                    }
                    keyWord.setText("");
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
                    if (wordTextE != null ) {
                        wordTextE.clear();
                    }
                    if (prodNameTextE != null) {
                        prodNameTextE.clear();
                    }
                }
                break;
            case R.id.searchRtn_id:
                onBackPressed();
                break;
            case R.id.searchClear_id:
                keyWord.setText("");
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
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
            case "MEMO":
                Bundle bundleItem;
                bundleItem = new Bundle();
                if (!up_menu_item.equals("")) {
                    bundleItem.putString("Menu", up_menu_item);
                    intent.putExtras(bundleItem);
                }
                intent.setClass(SearchActivity.this, MemoActivity.class);
                break;
            default:
                Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                intent.setClass(SearchActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        startActivity(intent);
        SearchActivity.this.finish();

    }
}
