package com.example.user.store3c;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by user on 2016/9/18.
 */
class AccountDbAdapter {
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PICTURE = "picture";

    private static final String KEY_ORDER_ID = "orderId";
    private static final String KEY_ORDER_INDEX = "orderIndex";
    private static final String KEY_ORDER_IMG= "orderImage";
    private static final String KEY_ORDER_PRODUCT = "orderProduct";
    private static final String KEY_ORDER_PRICE = "orderPrice";
    private static final String KEY_ORDER_INTRO = "orderIntro";

    private static final String KEY_MEMO_ID = "memoId";
    private static final String KEY_MEMO_INDEX = "memoIndex";
    private static final String KEY_MEMO_TEXT = "memoText";
    private static final String KEY_MEMO_PRICE = "memoPrice";

    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Context mCtx;
    private static final String TABLE_NAME_USER = "UserList";
    private static final String TABLE_NAME_ORDER = "BuyList";
    private static final String TABLE_NAME_MEMO = "MemoList";
    private static final String Mlength = "1000000";

    AccountDbAdapter(Context ctx) {
        this.mCtx = ctx;
        open();
    }

    private void open() {
        mDbHelper = new DBHelper(mCtx);
        try {
            mDb = mDbHelper.getWritableDatabase();
            mDbHelper.onCreate(mDb);
            //mDbHelper.onUpgrade(mDb,1,2);   // can delete table
        }catch (SQLException e) {
            Log.i("db", "Open: " + e.getMessage());
        }
    }

    void close() {
        if (mDbHelper != null) {
            try {
                mDb.close();
                mDbHelper.close();
            }catch (SQLException e) {
                Log.i("db", "Close: " + e.getMessage());
            }
        }
    }

    long createUser(String name, String email, String password, byte[] picture) {
        long createRes = 0;

        ContentValues values = new ContentValues();
        try {
            values.put(KEY_NAME, name);
            values.put(KEY_EMAIL, email);
            values.put(KEY_PASSWORD, password);
            values.put(KEY_PICTURE, picture);
            createRes = mDb.insert(TABLE_NAME_USER, null, values);
            Toast.makeText(mCtx, "新增使用者資料成功!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Create table:" + e.getMessage());
        }
        return  createRes;
    }

    int updateUserSimpleData(int id, String name, String email, String password){
        int updateCon = 0;

        ContentValues values = new ContentValues();
        try {
            values.put(KEY_NAME, name);
            values.put(KEY_EMAIL, email);
            values.put(KEY_PASSWORD, password);
            updateCon = mDb.update(TABLE_NAME_USER, values, " _id=" + id, null);
            Toast.makeText(mCtx, "更新使用者資料成功!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Update User: " + e.getMessage());
        }
        return updateCon;

    }

    int updateUser(int id, String name, String email, String password, byte[] picture){
        int updateCon = 0;

        ContentValues values = new ContentValues();
        try {
            values.put(KEY_NAME, name);
            values.put(KEY_EMAIL, email);
            values.put(KEY_PASSWORD, password);
            values.put(KEY_PICTURE, picture);
            updateCon = mDb.update(TABLE_NAME_USER, values, " _id=" + id, null);
            Toast.makeText(mCtx, "更新使用者資料成功!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Update User: " + e.getMessage());
        }
        return updateCon;

    }

    int deleteUser(int id) {
        int affectedNumber = 0;
        String[] args = {Integer.toString(id)};
        try {
            affectedNumber = mDb.delete(TABLE_NAME_USER, "_id = ?", args);
        }catch (SQLException e) {
            Log.i("db", "Delete User: " + e.getMessage());
        }
        return affectedNumber;

    }

    Cursor queryContactsByName(String inputText) throws SQLException {

        Cursor mCursor = null;
        if (inputText == null  ||  inputText.length () == 0)  {
            try {
                mCursor = mDb.query(TABLE_NAME_USER, new String[]{KEY_ID,
                                KEY_NAME, KEY_EMAIL, KEY_PASSWORD},
                        null, null, null, null, null);
            }catch (SQLException e){
                Log.i("db", "Query User By Name: " + e.getMessage());
            }

        }
        else {
            try {
                mCursor = mDb.query(true, TABLE_NAME_USER, new String[]{KEY_ID,
                                KEY_NAME, KEY_EMAIL, KEY_PASSWORD},
                        KEY_NAME + " like '%" + inputText + "%'", null,
                        null, null, null, null);
            }catch (SQLException e){
                Log.i("db", "Query User By Name: " + e.getMessage());
            }
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    Cursor getSimpleUserData() {
        Cursor mCursor = null;

        try {
            mCursor = mDb.query(TABLE_NAME_USER, new String[]{KEY_ID, KEY_NAME, KEY_EMAIL, KEY_PASSWORD},
                    null, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        }catch (SQLException e) {
            Log.i("db", "GetSimpleUserData: " + e.getMessage());
        }
        return mCursor;
    }

    ArrayList<Cursor> getFullUserData() {
        ArrayList<Cursor> mCursor = new ArrayList<>();
        Cursor largeImgCursor = null, indexCursor = null, dataCursor;
        int index = 0, start= 1, length = 1000000;
        int id;
        String[] argMb = new String[]{Mlength};
        try {
            largeImgCursor = mDb.rawQuery("SELECT _id FROM " + TABLE_NAME_USER + " WHERE " + "length(" + KEY_PICTURE + ") > CAST(? AS INT)", argMb);
            if (largeImgCursor != null) {
                largeImgCursor.moveToFirst();
            }
        }catch (Exception e) {
            Log.i("db", "GetFullUserData: " + e.getMessage());
        }
        if (largeImgCursor != null) {
            if (largeImgCursor.getCount() == 0) {
                largeImgCursor.close();
                try {
                    Cursor cursorItem = mDb.query(TABLE_NAME_USER, new String[]{KEY_ID, KEY_NAME, KEY_EMAIL, KEY_PASSWORD, KEY_PICTURE},
                            null, null, null, null, null);
                    if (cursorItem != null) {
                        cursorItem.moveToFirst();
                    }
                    mCursor.add(0, cursorItem);
                } catch (Exception e) {
                    Log.i("db", "GetFullUserData: " + e.getMessage());
                }
                return mCursor;
            } else {
                id = largeImgCursor.getInt(0);
                largeImgCursor.close();
                String[] argMaxLength, argPieceLength, argRemainLength;
                do {
                    try {
                        argMaxLength = new String[]{String.valueOf((index + 1) * length)};
                        indexCursor = mDb.rawQuery("SELECT _id FROM " + TABLE_NAME_USER + " WHERE length(picture) > CAST(? AS INT)", argMaxLength);
                        if (indexCursor != null) {
                            indexCursor.moveToFirst();
                        }
                    } catch (Exception e) {
                        Log.i("db", "GetFullUserData: " + e.getMessage());
                    }
                    if (indexCursor != null) {
                        if (indexCursor.getCount() > 0) {
                            try {
                                argPieceLength = new String[]{String.valueOf(start), String.valueOf(length), String.valueOf(id)};
                                dataCursor = mDb.rawQuery("SELECT substr(picture, ?, ?) FROM " + TABLE_NAME_USER + " WHERE _id = ?", argPieceLength);
                                if (dataCursor != null) {
                                    dataCursor.moveToFirst();
                                }
                                mCursor.add(index, dataCursor);
                            } catch (Exception e) {
                                Log.i("db", "GetFullUserData: " + e.getMessage());
                            }
                            start = start + length;
                        } else {
                            try {
                                argRemainLength = new String[]{String.valueOf(start), String.valueOf(id)};
                                dataCursor = mDb.rawQuery("SELECT substr(picture, ?) FROM " + TABLE_NAME_USER + " WHERE _id = ?", argRemainLength);
                                if (dataCursor != null) {
                                    dataCursor.moveToFirst();
                                }
                                mCursor.add(index, dataCursor);
                            } catch (Exception e) {
                                Log.i("db", "GetFullUserData: " + e.getMessage());
                            }
                        }
                        indexCursor.close();
                    }
                    index++;
                } while (mCursor.get(index - 1).getBlob(0).length == length);
                try {
                    Cursor cursorItem = mDb.query(TABLE_NAME_USER, new String[]{KEY_ID, KEY_NAME, KEY_EMAIL, KEY_PASSWORD},
                            null, null, null, null, null);
                    if (cursorItem != null) {
                        cursorItem.moveToFirst();
                    }
                    mCursor.add(index, cursorItem);
                } catch (Exception e) {
                    Log.i("db", "GetFullUserData: " + e.getMessage());
                }
                return mCursor;
            }
        }
        return  mCursor;
    }

    boolean IsDbUserEmpty () {

        //mDbHelper.onUpgrade(mDb, 1, 2);
        //String sql = "SELECT * FROM " + TABLE_NAME_USER;
        Cursor cursor;
        int count = 0;
        try {
            cursor = mDb.query(TABLE_NAME_USER, new String[]{KEY_ID, KEY_NAME},
                    null, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                count = cursor.getCount();
                cursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Is Db User Empty: " + e.getMessage());
        }
        return (count == 0);
    }

    long createOrder(int index, byte[] pic, String product, String price, String intro) {
        long createRes = 0;

        ContentValues values = new ContentValues();
        try {
            values.put(KEY_ORDER_INDEX, index);
            values.put(KEY_ORDER_IMG, pic);
            values.put(KEY_ORDER_PRODUCT, product);
            values.put(KEY_ORDER_PRICE, price);
            values.put(KEY_ORDER_INTRO, intro);
            createRes = mDb.insert(TABLE_NAME_ORDER, null, values);
        }catch (SQLException e) {
            Log.i("db", "Create table: " + e.getMessage());
        }
        return  createRes;
    }

    int deleteOrder(int index, int orderSize) {
        int affectedNumber = 0;
        byte[] itemImg;
        String itemProduct, itemPrice, itemIntro;
        boolean shift = false;

        try {
            affectedNumber = mDb.delete(TABLE_NAME_ORDER, " orderIndex=" + index, null);
            //Toast.makeText(mCtx, "訂購已取消!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Delete Order: " + e.getMessage());
        }

        if (affectedNumber > 0) {
            for (int i = 0; index < orderSize - 1; i++, index++) {
                itemImg = getOrderImg(index + 1);
                itemProduct = getOrderProduct(index + 1);
                itemPrice = getOrderPrice(index + 1);
                itemIntro = getOrderIntro(index + 1);
                if (i == 0) {
                    shift = true;
                    if (createOrder(index, itemImg, itemProduct, itemPrice, itemIntro) == 0) {
                        Log.i("create Order: ", "no data change!");
                    }
                } else {
                    if (updateOrder(index, itemImg, itemProduct, itemPrice, itemIntro) == 0) {
                        Log.i("update Order: ", "no data change!");
                    }
                }
            }
            if (shift && index == orderSize - 1) {
                try {
                    mDb.delete(TABLE_NAME_ORDER, " orderIndex=" + index, null);
                    //Toast.makeText(mCtx, "訂購已取消!", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Log.i("db", "Delete Order: " + e.getMessage());
                }
            }
        }

        return affectedNumber;
    }

    int deletePartOrder(int orderTableSize, ArrayList<Integer> orderSet) {
        int affectedNumber = 0;
        byte[] itemImg;
        String itemProduct, itemPrice, itemIntro;

        for (int index = 0; index < orderSet.size(); index++) {
            try {
                affectedNumber = affectedNumber + mDb.delete(TABLE_NAME_ORDER, " orderIndex=" + orderSet.get(index), null);
            } catch (SQLException e) {
                Log.i("db", "Delete Order: " + e.getMessage());
            }
        }
        if (affectedNumber > 0) {
            boolean haveData;
            int indexKey = 0;
            Cursor mCursor;

            for (int i = 0; i < orderTableSize; i++) {
                haveData = true;
                for (int j = 0; j < orderSet.size(); j++) {
                    if (i == orderSet.get(j)) {
                        haveData = false;
                    }
                }
                if (haveData) {
                    if (i > indexKey) {
                        itemImg = getOrderImg(i);
                        itemProduct = getOrderProduct(i);
                        itemPrice = getOrderPrice(i);
                        itemIntro = getOrderIntro(i);
                        try {
                            mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                                    " orderIndex=" + indexKey, null, null, null, null);
                            if (mCursor.getCount() == 0) {
                                if (createOrder(indexKey, itemImg, itemProduct, itemPrice, itemIntro) == 0) {
                                    Log.i("create Order: ", "no data change!");
                                }
                            } else {
                                if (updateOrder(indexKey, itemImg, itemProduct, itemPrice, itemIntro) == 0) {
                                    Log.i("update Order: ", "no data change!");
                                }
                            }
                            mCursor.close();
                        } catch (SQLException e) {
                            Log.i("db", "Get Order Item Img: " + e.getMessage());
                        }
                    }
                    indexKey++;
                }
            }
            for (int i = indexKey; i < orderTableSize; i++) {
                try {
                    mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                            " orderIndex=" + i, null, null, null, null);
                    if (mCursor.getCount() != 0) {
                        mDb.delete(TABLE_NAME_ORDER, " orderIndex=" + i, null);
                    }
                    mCursor.close();
                } catch (SQLException e) {
                    Log.i("db", "Delete Order: " + e.getMessage());
                }
            }
        }

        return affectedNumber;
    }

    boolean deleteAllOrder() {
        if(DbOrderAmount()>0){
            try {
                Cursor mCursor;

                mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                        null, null, null, null, null);
                if (mCursor != null) {
                    mDb.delete(TABLE_NAME_ORDER, null, null);
                    mCursor.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    Cursor listAllOrder() {
        Cursor mCursor = null;

        try {
            mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                    null, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        }catch (SQLException e) {
            Log.i("db", "List All Order: " + e.getMessage());
        }
        return mCursor;
    }

    int DbOrderAmount () {

        //mDbHelper.onUpgrade(mDb, 1, 2);
        //String sql = "SELECT * FROM " + TABLE_NAME_ORDER;
        Cursor cursor;
        int count = 0;

        try {
            cursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                    null, null, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
                cursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Order Amount: " + e.getMessage());
        }
        return count;
    }

    private int updateOrder(int index, byte[] img, String product, String price, String intro){
        int updateCon = 0;

        ContentValues values = new ContentValues();
        try {
            values.put(KEY_ORDER_INDEX, index);
            values.put(KEY_ORDER_IMG, img);
            values.put(KEY_ORDER_PRODUCT, product);
            values.put(KEY_ORDER_PRICE, price);
            values.put(KEY_ORDER_INTRO, intro);
            updateCon = mDb.update(TABLE_NAME_ORDER, values, " orderIndex=" + index, null);
            //Toast.makeText(mCtx, "更新資料成功!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Update Order item: " + e.getMessage());
        }
        return updateCon;

    }

    private byte[] getOrderImg(int index) {
        Cursor mCursor;
        byte[] itemImg, orderImg = null;
        try {
            mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                    " orderIndex=" + index, null, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                itemImg = mCursor.getBlob(2);
                orderImg = new byte[itemImg.length];
                System.arraycopy(itemImg, 0, orderImg, 0, itemImg.length);
                mCursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Get Order Item Img: " + e.getMessage());
        }
        return orderImg;
    }

    private String getOrderProduct(int index) {
        Cursor mCursor;
        String itemProduct = "";
        try {
            mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                    " orderIndex=" + index, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
                itemProduct = mCursor.getString(3);
                mCursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Get Order Item Product: " + e.getMessage());
        }
        return itemProduct;
    }

    private String getOrderPrice(int index) {
        Cursor mCursor;
        String itemPrice = "";
        try {
            mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                    " orderIndex=" + index, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
                itemPrice = mCursor.getString(4);
                mCursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Get Order Item Price: " + e.getMessage());
        }
        return itemPrice;
    }

    private String getOrderIntro(int index) {
        Cursor mCursor;
        String itemIntro = "";
        try {
            mCursor = mDb.query(TABLE_NAME_ORDER, new String[]{KEY_ORDER_ID, KEY_ORDER_INDEX, KEY_ORDER_IMG, KEY_ORDER_PRODUCT, KEY_ORDER_PRICE, KEY_ORDER_INTRO},
                    " orderIndex=" + index, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
                itemIntro = mCursor.getString(5);
                mCursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Get Order Item Intro: " + e.getMessage());
        }
        return itemIntro;
    }

    Boolean moveOrder(int fromIndex, int toIndex) {
        byte[] itemImg, moveImg = getOrderImg(fromIndex);
        String itemProduct, itemPrice, itemIntro;
        String moveProduct = getOrderProduct(fromIndex), movePrice = getOrderPrice(fromIndex), moveIntro = getOrderIntro(fromIndex);
        boolean  move = true;

        if (fromIndex > toIndex) {
            for (int i=fromIndex-1;i>toIndex-1;i--) {
                itemImg = getOrderImg(i);
                itemProduct = getOrderProduct(i);
                itemPrice = getOrderPrice(i);
                itemIntro = getOrderIntro(i);
                if (updateOrder(i + 1, itemImg, itemProduct, itemPrice, itemIntro) == 0) {
                    move = false;
                    Log.i("update Order: ", "no data change!");
                }
            }
        }
        else {
            for (int i=fromIndex;i<toIndex;i++) {
                itemImg = getOrderImg(i+1);
                itemProduct = getOrderProduct(i+1);
                itemPrice = getOrderPrice(i+1);
                itemIntro = getOrderIntro(i+1);
                if (updateOrder(i, itemImg, itemProduct, itemPrice, itemIntro) == 0) {
                    move = false;
                    Log.i("update Order: ", "no data change!");
                }
            }
        }
        if (updateOrder(toIndex, moveImg, moveProduct, movePrice, moveIntro) == 0) {
            move = false;
            Log.i("update Order: ", "no data change!");
        }
        return move;
    }

    long createMemo(int index, String text, String price) {
        long createRes = 0;

        ContentValues values = new ContentValues();
        try {
            values.put(KEY_MEMO_INDEX, index);
            values.put(KEY_MEMO_TEXT, text);
            values.put(KEY_MEMO_PRICE, price);
            createRes = mDb.insert(TABLE_NAME_MEMO, null, values);
            //Toast.makeText(mCtx, "新增資料成功!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Create Memo table: " + e.getMessage());
        }
        return  createRes;
    }

    int updateMemo(int index, String text, String price){
        int updateCon = 0;

        ContentValues values = new ContentValues();
        try {
            values.put(KEY_MEMO_TEXT, text);
            values.put(KEY_MEMO_PRICE, price);
            updateCon = mDb.update(TABLE_NAME_MEMO, values, " memoIndex=" + index, null);
            //Toast.makeText(mCtx, "更新資料成功!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Update Memo: " + e.getMessage());
        }
        return updateCon;

    }

    boolean IsDbMemoEmpty () {

        //mDbHelper.onUpgrade(mDb, 1, 2);
        //String sql = "SELECT * FROM " + TABLE_NAME_ORDER;
        Cursor cursor;
        int count=0;
        try {
            cursor = mDb.query(TABLE_NAME_MEMO, new String[]{KEY_MEMO_ID, KEY_MEMO_INDEX, KEY_MEMO_TEXT, KEY_MEMO_PRICE},
                    null, null, null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
                cursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "IsDbMemoEmpty: " + e.getMessage());
        }
        return (count == 0);
    }

    Cursor listAllMemo() {
        Cursor mCursor = null;

        try {
            mCursor = mDb.query(TABLE_NAME_MEMO, new String[]{KEY_MEMO_ID, KEY_MEMO_INDEX, KEY_MEMO_TEXT, KEY_MEMO_PRICE},
                    null, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        }catch (SQLException e) {
            Log.i("db", "List All Memo: " + e.getMessage());
        }
        return mCursor;
    }

    private String getMemoText(int index) {
        Cursor mCursor;
        String itemText = "";
        try {
            mCursor = mDb.query(TABLE_NAME_MEMO, new String[]{KEY_MEMO_ID, KEY_MEMO_INDEX, KEY_MEMO_TEXT, KEY_MEMO_PRICE},
                    " memoIndex=" + index, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
                itemText = mCursor.getString(2);
                mCursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Get Memo Text: " + e.getMessage());
        }
        return itemText;
    }

    private String getMemoPrice(int index) {
        Cursor mCursor;
        String itemPrice = "";
        try {
            mCursor = mDb.query(TABLE_NAME_MEMO, new String[]{KEY_MEMO_ID, KEY_MEMO_INDEX, KEY_MEMO_TEXT, KEY_MEMO_PRICE},
                    " memoIndex=" + index, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
                itemPrice = mCursor.getString(3);
                mCursor.close();
            }
        }catch (SQLException e) {
            Log.i("db", "Get Memo Price: " + e.getMessage());
        }
        return itemPrice;
    }

    int deleteMemo(int index, int memoSize) {
        int affectedNumber = 0;
        String itemText, itemPrice;
        boolean shift = false;

        try {
            affectedNumber = mDb.delete(TABLE_NAME_MEMO, " memoIndex=" + index, null);
            //Toast.makeText(mCtx, "資料已刪除!", Toast.LENGTH_SHORT).show();
        }catch (SQLException e) {
            Log.i("db", "Delete Memo: " + e.getMessage());
        }

        for (int i=0;index < memoSize-1;i++,index++) {
            itemText = getMemoText(index+1);
            itemPrice = getMemoPrice(index+1);

            if (i == 0) {
                shift = true;
                if (createMemo(index, itemText, itemPrice) == 0) {
                    Log.i("create Memo: ", "no data change!");
                }
            }
            else {
                if (updateMemo(index, itemText, itemPrice) == 0) {
                    Log.i("update Memo: ", "no data change!");
                }
            }
        }
        if (shift && index == memoSize-1) {
            try {
                affectedNumber = mDb.delete(TABLE_NAME_MEMO, " memoIndex=" + index, null);
                //Toast.makeText(mCtx, "資料已刪除!", Toast.LENGTH_SHORT).show();
            } catch (SQLException e) {
                Log.i("db", "Delete Memo: " + e.getMessage());
            }
        }

        return affectedNumber;
    }

    boolean moveMemo(int fromIndex, int toIndex) {
        String itemText, itemPrice, moveText = getMemoText(fromIndex), movePrice = getMemoPrice(fromIndex);
        boolean move = true;

        if (fromIndex > toIndex) {
            for (int i=fromIndex-1;i>toIndex-1;i--) {
                itemText = getMemoText(i);
                itemPrice = getMemoPrice(i);
                if (updateMemo(i + 1, itemText, itemPrice) == 0) {
                    move = false;
                    Log.i("update Memo: ", "no data change!");
                }
            }
        }
        else {
            for (int i=fromIndex;i<toIndex;i++) {
                itemText = getMemoText(i+1);
                itemPrice = getMemoPrice(i+1);
                if (updateMemo(i, itemText, itemPrice) == 0) {
                    move = false;
                    Log.i("update Memo: ", "no data change!");
                }
            }
        }
        if (updateMemo(toIndex, moveText, movePrice) == 0) {
            move = false;
            Log.i("update Memo: ", "no data change!");
        }
        return move;
    }
}
