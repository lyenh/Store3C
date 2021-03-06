package com.example.user.store3c;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class MemoActivity extends AppCompatActivity implements View.OnClickListener{
    private String menu_item;

    String updateMemo, updateMemoPrice;
    AccountDbAdapter dbhelper = null;
    MemoRecyclerAdapter memoAdapter = null;
    ArrayList<String> memoList = new ArrayList<>(), memoPriceList = new ArrayList<>();
    EditText memoText, memoPrice;
    boolean update = false;
    int updateArrayIndex = 0;
    ItemTouchHelper ith;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button memoReturnBtn, memoSaveBtn, memoClearBtn;
        RecyclerView MemoRecyclerView;
        setContentView(R.layout.activity_memo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentItem = getIntent();
        Bundle bundleItem = intentItem.getExtras();
        String dbMemoText, dbMemoPrice;
        int index;
        SortedMap<Integer, String> itemText = new TreeMap<>(), itemPrice = new TreeMap<>();
        if (bundleItem != null) {
            menu_item = bundleItem.getString("Menu");
        }
        dbhelper = new AccountDbAdapter(this);

        memoText = findViewById(R.id.memoText_id);
        memoPrice = findViewById(R.id.memoPrice_id);
        memoClearBtn = findViewById(R.id.memoClear_id);
        memoSaveBtn = findViewById(R.id.memoSave_id);
        memoReturnBtn = findViewById(R.id.memoRtb_id);
        MemoRecyclerView = findViewById(R.id.memoRecyclerView_id);

        memoText.setText("");
        memoPrice.setText("");

        if (!(dbhelper.IsDbMemoEmpty())) {
            try {
                Cursor cursor = dbhelper.listAllMemo();
                if (cursor.getCount() > 0) {
                    do {
                        index = Integer.valueOf(cursor.getString(1));
                        dbMemoText = cursor.getString(2);
                        dbMemoPrice = cursor.getString(3);
                        itemText.put(index, dbMemoText);
                        itemPrice.put(index, dbMemoPrice);
                    } while (cursor.moveToNext());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (int i=0;i<itemText.size();i++) {
                memoList.add(i,itemText.get(i));
                memoPriceList.add(i,itemPrice.get(i));
            }
        }

        memoAdapter = new MemoRecyclerAdapter(MemoActivity.this, memoList, memoPriceList, menu_item);
        MemoRecyclerView.setAdapter(memoAdapter);
        MemoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        MemoRecyclerView.setHasFixedSize(true);

        // Extend the Callback class
        ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {
            //and in your imlpementaion of  get the viewHolder's and target's positions in your adapter data, swap them
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();

                if (!dbhelper.moveMemo(fromPosition, toPosition)) {
                    Log.i("move Memo: ", "no data change!");
                }
                String sourceText = memoList.remove(fromPosition), sourcePrice = memoPriceList.remove(fromPosition);
                memoList.add(toPosition, sourceText);
                memoPriceList.add(toPosition, sourcePrice);

                // and notify the adapter that its dataset has changed
                memoAdapter.notifyItemMoved(fromPosition, toPosition);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int index = viewHolder.getAdapterPosition();
                if (dbhelper.deleteMemo(index, memoList.size()) == 0) {
                    Log.i("delete Memo: ", "no data change!");
                }
                memoList.remove(index);
                memoPriceList.remove(index);
                update = false;
                memoText.setText("");
                memoPrice.setText("");
                memoAdapter.notifyItemRemoved(index);
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                int swipeFlags = ItemTouchHelper.LEFT;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,@NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                memoAdapter.notifyItemRangeChanged(0,memoList.size());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //return super.isLongPressDragEnabled();
                return false;
            }
        };

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ith = new ItemTouchHelper(ithCallback);
        ith.attachToRecyclerView(MemoRecyclerView);

        memoClearBtn.setOnClickListener(this);
        memoSaveBtn.setOnClickListener(this);
        memoReturnBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.memoClear_id:
                memoText.setText("");
                memoPrice.setText("");
                update = false;
                break;
            case R.id.memoSave_id:
                if (update) {
                    if (!memoText.getText().toString().equals("") && !memoPrice.getText().toString().equals("")) {
                        if (dbhelper.updateMemo(updateArrayIndex, memoText.getText().toString(), memoPrice.getText().toString()) == 0) {
                            Log.i("update Memo: ", "no data change!");
                        }
                        memoList.set(updateArrayIndex, memoText.getText().toString());
                        memoPriceList.set(updateArrayIndex, memoPrice.getText().toString());
                        memoText.setText("");
                        memoPrice.setText("");
                        memoAdapter.notifyDataSetChanged();
                        update = false;
                    }else {
                        Toast.makeText(this.getBaseContext(), "請輸入資料! ", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if (!memoText.getText().toString().equals("") && !memoPrice.getText().toString().equals("")) {
                        if (dbhelper.createMemo(memoList.size(), memoText.getText().toString(), memoPrice.getText().toString()) == -1) {
                            Log.i("create Memo: ", "fail!");
                        }
                        memoList.add(memoText.getText().toString());
                        memoPriceList.add(memoPrice.getText().toString());
                        memoText.setText("");
                        memoPrice.setText("");
                        memoAdapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(this.getBaseContext(), "請輸入資料! ", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.memoRtb_id:
                Intent intentItem = new Intent();
                switch(menu_item) {
                    case "DISH":
                        intentItem.setClass(MemoActivity.this, MainActivity.class);
                        break;
                    case "CAKE":
                        intentItem.setClass(MemoActivity.this, CakeActivity.class);
                        break;
                    case "PHONE":
                        intentItem.setClass(MemoActivity.this, PhoneActivity.class);
                        break;
                    case "CAMERA":
                        intentItem.setClass(MemoActivity.this, CameraActivity.class);
                        break;
                    case "BOOK":
                        intentItem.setClass(MemoActivity.this, BookActivity.class);
                        break;
                    default:
                        Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                        intentItem.setClass(MemoActivity.this, MainActivity.class);
                }

                startActivity(intentItem);
                MemoActivity.this.finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbhelper.close();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        switch (menu_item) {
            case "DISH":
                intent.setClass(MemoActivity.this, MainActivity.class);
                break;
            case "CAKE":
                intent.setClass(MemoActivity.this, CakeActivity.class);
                break;
            case "PHONE":
                intent.setClass(MemoActivity.this, PhoneActivity.class);
                break;
            case "CAMERA":
                intent.setClass(MemoActivity.this, CameraActivity.class);
                break;
            case "BOOK":
                intent.setClass(MemoActivity.this, BookActivity.class);
                break;
            default:
                Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                intent.setClass(MemoActivity.this, MainActivity.class);
        }

        startActivity(intent);
        MemoActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_memo, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.action_login_status).setIconTintList(Color.BLUE);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Bundle bundle;
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.action_shopping_car) {
            intent = new Intent();
            bundle = new Bundle();
            bundle.putString("Menu", "MEMO");
            bundle.putString("upMenu", menu_item);
            intent.putExtras(bundle);
            intent.setClass(MemoActivity.this, OrderActivity.class);
            startActivity(intent);
            MemoActivity.this.finish();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
