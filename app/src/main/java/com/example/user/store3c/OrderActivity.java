package com.example.user.store3c;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.example.user.store3c.MainActivity.mAuth;

public class OrderActivity extends AppCompatActivity implements View.OnClickListener{
    private AccountDbAdapter dbhelper = null;
    TextView orderText;
    private String menu_item = "DISH", up_menu_item = "", search_list = "";
    String orderTextList = "";
    private OrderRecyclerAdapter adapter = null;
    static float total_price = 0;
    private ArrayList<ProductItem> orderTable = new ArrayList<>();
    private boolean recentTask = false;
    public static ArrayList<ListItem> promotionListItem = new ArrayList<>();

    ItemTouchHelper ith;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Toolbar toolbar = findViewById(R.id.toolbarOrder);
        setSupportActionBar(toolbar);
        String edit_Title = "購物車資料";
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.store_logo);
            getSupportActionBar().setTitle(edit_Title);
        }

        dbhelper = new AccountDbAdapter(this);
        RecyclerView OrderRecyclerView;
        Button ret_b, promotion_b, resetCheckBox_b;
        String product_name, product_price, product_intro;
        String str_price;
        String[] tokens;
        byte[] product_pic;
        float price = 0;
        //int imgHeight, screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int index;
        ArrayList<String> checkBoxNameList, checkBoxPriceList;

        Bundle bundle = getIntent().getExtras();
        SortedMap<Integer, ProductItem> itemText = new TreeMap<>();

        orderText = findViewById(R.id.orderItem_id);
        ret_b = findViewById(R.id.orderReturnBtn_id);
        promotion_b = findViewById(R.id.promotionBtn_id);
        resetCheckBox_b = findViewById(R.id.orderCheckBoxClearBtn_id);
        OrderRecyclerView = findViewById(R.id.orderRecyclerView_id);

        String retainRecentTask;
        if (bundle != null) {
            if (bundle.getString("Name") != null) {
                if (bundle.getString("Search") != null) {
                    search_list = bundle.getString("Search");
                }
                retainRecentTask = bundle.getString("RetainRecentTask");
                if (retainRecentTask != null) {
                    if (retainRecentTask.equals("RECENT_ACTIVITY")) {       // productActivity task
                        recentTask = true;
                    }
                }
                if (bundle.getString("Menu") != null) {
                    menu_item = bundle.getString("Menu");
                }
                if (bundle.getString("upMenu") != null) {
                    up_menu_item = bundle.getString("upMenu");
                }
                product_pic = bundle.getByteArray("Pic");
                product_name = bundle.getString("Name");
                product_price = bundle.getString("Price");
                product_intro = bundle.getString("Intro");
                if (product_pic != null && product_name != null && product_price != null && product_intro != null) {
                    int size = dbhelper.DbOrderAmount();
                    if (dbhelper.createOrder(size, product_pic, product_name, product_price, product_intro) == 0)
                        Log.i("db", "Insert   fail" + product_name + product_price + product_intro);
                }
                else {
                    Toast.makeText(OrderActivity.this, "欲購買的產品無資料 !", Toast.LENGTH_SHORT).show();
                }
            } else if (bundle.getStringArrayList("MultiName") != null) {
                checkBoxNameList = bundle.getStringArrayList("MultiName");
                checkBoxPriceList = bundle.getStringArrayList("MultiPrice");
                product_pic = bundle.getByteArray("Pic");
                product_intro = bundle.getString("Intro");
                if (bundle.getString("Menu") != null) {
                    menu_item = bundle.getString("Menu");
                }
                if (bundle.getString("upMenu") != null) {
                    up_menu_item = bundle.getString("upMenu");
                }
                int size;
                for (int i = 0; i< Objects.requireNonNull(checkBoxNameList).size(); i++) {
                    product_name = checkBoxNameList.get(i);
                    product_price = Objects.requireNonNull(checkBoxPriceList).get(i);
                    if (product_pic != null && product_name != null && product_price != null && product_intro != null) {
                        size = dbhelper.DbOrderAmount();
                        product_price = product_price + "元";
                        if (dbhelper.createOrder(size, product_pic, product_name, product_price, product_intro) == 0)
                            Log.i("db", "Insert   fail" + product_name + product_price + product_intro);
                    }
                    else {
                        Toast.makeText(OrderActivity.this, "欲購買的產品無資料 !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else if (bundle.getString("Menu") != null) {
                menu_item = bundle.getString("Menu");
                if (bundle.getString("Search") != null) {   // press notification button on search page
                    search_list = bundle.getString("Search");
                }
                if (bundle.getString("upMenu") != null) {
                    up_menu_item = bundle.getString("upMenu");
                }
                retainRecentTask = bundle.getString("RetainRecentTask");
                if (retainRecentTask != null) {
                    if (retainRecentTask.equals("RECENT_ACTIVITY")) {       // orderFormActivity task
                        recentTask = true;
                    }
                }
            }
        }

        if(dbhelper.DbOrderAmount() > 0){
            try {
                Cursor cursor = dbhelper.listAllOrder();
                if (cursor.getCount() > 0) {
                    do {
                        index = Integer.parseInt(cursor.getString(1));
                        byte[] product_img = cursor.getBlob(2);
                        itemText.put(index, new ProductItem(BitmapFactory.decodeByteArray(product_img, 0 , product_img.length), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
                        str_price = cursor.getString(4);
                        tokens = str_price.split("元");
                        for (String token:tokens) {
                            price = Float.parseFloat(token);
                        }
                        //total_price = total_price + price;
                    } while (cursor.moveToNext());

                }
             } catch (SQLException e) {
                e.printStackTrace();
             }

            for (int i=0;i<itemText.size();i++) {
                orderTable.add(i,itemText.get(i));
            }
        }

        orderTextList = "總共 " + ((int)total_price) + " 元 !";

        adapter = new OrderRecyclerAdapter(OrderActivity.this, orderTable, menu_item, up_menu_item);
    /* if (screenWidth > 800) {
            imgHeight = 430;
            OrderRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
        } */
        OrderRecyclerView.setAdapter(adapter);
        OrderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Extend the Callback class
        ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {
            //and in your implementation of  get the viewHolder's and target's positions in your adapter data, swap them
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                final int toPosition = target.getAbsoluteAdapterPosition();

                if (!dbhelper.moveOrder(fromPosition, toPosition)) {
                    Log.i("move Order item: ", "no data change!");
                }

                ProductItem sourceItem = orderTable.remove(fromPosition);
                orderTable.add(toPosition, sourceItem);
                adapter.ReorderCheckBoxList(fromPosition, toPosition);
                // and notify the adapter that its dataset has changed
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int index = viewHolder.getAbsoluteAdapterPosition();

                if (dbhelper.deleteOrder(index, orderTable.size()) == 0) {
                    Log.i("delete Order: ", "no data change!");
                }

                if (OrderRecyclerAdapter.checkBoxList.containsKey(index)) {
                    total_price = total_price - Integer.parseInt(orderTable.get(index).getPrice().split("元")[0]);
                    orderTextList = "總共 " + ((int)total_price) + " 元 !";
                    orderText.setText(orderTextList);
                }
                adapter.RemoveCheckBox(index);
                orderTable.remove(index);
                adapter.notifyItemRemoved(index);
                //Toast.makeText(OrderActivity.this, "The delete number: " + favDeleteBtn, Toast.LENGTH_SHORT).show();
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                int swipeFlags = ItemTouchHelper.LEFT;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                adapter.notifyItemRangeChanged(0,orderTable.size());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //return super.isLongPressDragEnabled();
                return false;
            }
        };

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ith = new ItemTouchHelper(ithCallback);
        ith.attachToRecyclerView(OrderRecyclerView);

        orderText.setText(orderTextList);
        ret_b.setOnClickListener(this);
        promotion_b.setOnClickListener(this);
        resetCheckBox_b.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig){
        OrderRecyclerAdapter.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        adapter.notifyDataSetChanged();
        Log.v("===>","ORIENTATION");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        dbhelper.close();
        OrderRecyclerAdapter.checkBoxList.clear();
        adapter.ResetCheckBox();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.promotionBtn_id:
                Intent intentItem = new Intent();
                if (InternetConnection.checkConnection(OrderActivity.this)) {
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null && !currentUser.isAnonymous()) {
                        if (total_price > 0) {
                            Bundle bundle = new Bundle();
                            promotionListItem.clear();
                            for (int i=0; i < orderTable.size(); i++) {
                                if (OrderRecyclerAdapter.checkBoxList.containsKey(i)) {
                                    ListItem item = new ListItem(orderTable.get(i).getName(), orderTable.get(i).getPrice());
                                    promotionListItem.add(item);
                                }
                            }
                            Log.i("Selected CheckBox => ", Integer.toString(promotionListItem.size()));
                            if (promotionListItem.size() != 0 ) {
                                bundle.putString("totalPrice", String.valueOf(total_price));
                                bundle.putInt("orderTableSize", orderTable.size());
                                ArrayList<Integer> orderSet = new ArrayList<>();
                                for (int i=0; i < orderTable.size(); i++) {
                                    if (OrderRecyclerAdapter.checkBoxList.containsKey(i)) {
                                        orderSet.add(i);
                                    }
                                }
                                bundle.putIntegerArrayList("orderSet", orderSet);
                                bundle.putString("Menu", menu_item);
                                if (!up_menu_item.equals("")) {
                                    bundle.putString("upMenu", up_menu_item);
                                }
                                intentItem.putExtras(bundle);
                                intentItem.setClass(OrderActivity.this, PromotionActivity.class);
                                startActivity(intentItem);
                                OrderActivity.this.finish();
                            }
                            else {
                                Toast.makeText(OrderActivity.this, "Selected CheckBox error !", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(OrderActivity.this, "商品先加入購物車, 才可以寄送簡訊 !", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(OrderActivity.this, "請先登入, 才可以寄送簡訊 !", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(OrderActivity.this, "網路未連線 !", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.orderCheckBoxClearBtn_id:
                total_price = 0;
                orderTextList = "總共 " + ((int)total_price) + " 元 !";
                orderText.setText(orderTextList);
                adapter.ResetCheckBox();
                break;
            case R.id.orderReturnBtn_id:
                onBackPressed();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle;

        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (search_list.equals("SEARCH")) {
            bundle = new Bundle();
            bundle.putString("Menu", menu_item);
            if (!up_menu_item.equals("")) {
                bundle.putString("upMenu", up_menu_item);
            }
            intent.putExtras(bundle);
            intent.setClass(OrderActivity.this, SearchActivity.class);
        }
        else {
            switch (menu_item) {
                case "DISH":
                    intent.setClass(OrderActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    if (recentTask) {
                        intent = Intent.makeRestartActivityTask (new ComponentName(getApplicationContext(), MainActivity.class));
                        intent.setPackage(getApplicationContext().getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        Bundle retainRecentTaskBundle = new Bundle();
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                        intent.putExtras(retainRecentTaskBundle);
                    }
                    break;
                case "CAKE":
                    intent.setClass(OrderActivity.this, CakeActivity.class);
                    break;
                case "PHONE":
                    intent.setClass(OrderActivity.this, PhoneActivity.class);
                    break;
                case "CAMERA":
                    intent.setClass(OrderActivity.this, CameraActivity.class);
                    break;
                case "BOOK":
                    intent.setClass(OrderActivity.this, BookActivity.class);
                    break;
                case "MEMO":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, MemoActivity.class);
                    break;
                case "USER":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, UserActivity.class);
                    break;
                case "POSITION":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, PositionActivity.class);
                    break;
                case "PRODUCT":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, ProductActivity.class);
                    break;
                case "MAP":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, MapsActivity.class);
                    break;
                case "LOGIN":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, LoginActivity.class);
                    break;
                case "PAGE":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, PageActivity.class);
                    break;
                case "ORDER_FORM":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(OrderActivity.this, com.example.user.store3c.OrderFormActivity.class);
                    break;
                default:
                    Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                    intent.setClass(OrderActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    if (recentTask) {
                        intent = Intent.makeRestartActivityTask (new ComponentName(getApplicationContext(), MainActivity.class));
                        intent.setPackage(getApplicationContext().getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        Bundle retainRecentTaskBundle = new Bundle();
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_TASK");
                        intent.putExtras(retainRecentTaskBundle);
                    }
            }
        }
        startActivity(intent);
        OrderActivity.this.finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        Bundle bundle;
        int id = item.getItemId();
        if (id == R.id.action_order_form) {
            intent = new Intent();
            bundle = new Bundle();
            bundle.putString("Menu", menu_item);
            if (!up_menu_item.equals("")) {
                bundle.putString("upMenu", up_menu_item);
            }
            intent.putExtras(bundle);
            intent.setClass(OrderActivity.this, com.example.user.store3c.OrderFormActivity.class);
            startActivity(intent);
            OrderActivity.this.finish();

        }

        return super.onOptionsItemSelected(item);
    }
}
