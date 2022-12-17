package com.example.user.store3c;

import android.app.ActivityManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Objects;

import static com.example.user.store3c.MainActivity.isTab;
import static com.example.user.store3c.MainActivity.rotationScreenWidth;
import static com.example.user.store3c.MainActivity.rotationTabScreenWidth;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener{

    private String menu_item = "DISH", up_menu_item = "", product_name, product_price, product_intro, order_list = "", search_list = "";
    private String notification_list = "";
    private byte[] product_pic;
    private ActivityManager.AppTask preTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        ImageView pic, buy_b;
        TextView name,price,intro;
        Button ret_b;
        ScrollView introView;
        Bitmap product_img;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.store_logo);
            getSupportActionBar().setTitle("產品簡介");
        }
        pic = findViewById(R.id.productImg_id);
        name = findViewById(R.id.productName_id);
        price = findViewById(R.id.productPrice_id);
        intro = findViewById(R.id.productIntro_id);
        introView = findViewById(R.id.productIntroView_id);
        ret_b = findViewById(R.id.productReturnBtn_id);
        buy_b = findViewById(R.id.buyBtn_id);

        Intent Intent = getIntent();
        Bundle bundle = Intent.getExtras();
        if (bundle != null) {
            notification_list = bundle.getString("Notification");
            if (notification_list != null) {   // notification promotion product
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.AppTask> tasks = am.getAppTasks();
                Log.i("TaskListSize ===> ", "num: " + am.getAppTasks().size());
                if (tasks.size() > 1) {
                    preTask = tasks.get(tasks.size()-1); // Should be the main task
                }
                menu_item = "DISH";
            }
            else {
                if (bundle.getString("Order") != null) {
                    order_list = bundle.getString("Order");
                }
                if (bundle.getString("Search") != null) {
                    search_list = bundle.getString("Search");
                }
                menu_item = bundle.getString("Menu");
                if (bundle.getString("upMenu") != null) {
                    up_menu_item = bundle.getString("upMenu");
                }
            }
            product_pic = bundle.getByteArray("Pic");
            if (product_pic != null) {
                product_img = BitmapFactory.decodeByteArray(product_pic, 0, product_pic.length);
                pic.setImageBitmap(product_img);
            }
            product_name = bundle.getString("Name");
            name.setText(product_name);
            product_price = bundle.getString("Price");
            price.setText(product_price);
            product_intro = bundle.getString("Intro");
        }

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int imgHeight;
        if (isTab) {
            if (screenWidth > rotationTabScreenWidth) {
                imgHeight = (product_intro.length() / 27 + 1) * 100;
                introView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            }
        }
        else {
            if (screenWidth > rotationScreenWidth) {
                imgHeight = (product_intro.length() / 27 + 1) * 60;
                introView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            }
        }
        intro.setText(product_intro);
        ret_b.setOnClickListener(this);
        buy_b.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.productReturnBtn_id:
                onBackPressed();
                break;

            case R.id.buyBtn_id:
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                if (search_list.equals("SEARCH")) {
                    bundle.putString("Search", "SEARCH");
                }
                bundle.putString("Menu", menu_item);
                if (!up_menu_item.equals("")) {
                    bundle.putString("upMenu", up_menu_item);
                }
                bundle.putByteArray("Pic", product_pic);
                bundle.putString("Name", product_name);
                bundle.putString("Price", product_price);
                bundle.putString("Intro", product_intro);
                intent.putExtras(bundle);
                Toast.makeText(this, "已加入購物車!", Toast.LENGTH_SHORT).show();

                intent.setClass(ProductActivity.this, OrderActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                if (notification_list != null) {
                    if (notification_list.equals("IN_APP")) {
                        if (preTask != null) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_FROM_BACKGROUND);
                            preTask.startActivity(this, intent, bundle);
                        }
                        else {
                            startActivity(intent);
                            Toast.makeText(this, "preTask null!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (notification_list.equals("UPPER_APP")) {
                        if (preTask != null) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_FROM_BACKGROUND);
                            preTask.startActivity(this, intent, bundle);
                        }
                        else {
                            startActivity(intent);
                        }
                    }
                    else {
                        startActivity(intent);
                    }
                }
                else {
                    startActivity(intent);
                }
                ProductActivity.this.finish();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle;

        if (order_list.equals("ORDER")) {
            bundle = new Bundle();
            bundle.putString("Menu", menu_item);
            if (!up_menu_item.equals("")) {
                bundle.putString("upMenu", up_menu_item);
            }
            intent.putExtras(bundle);
            intent.setClass(ProductActivity.this, OrderActivity.class);
        }
        else if (search_list.equals("SEARCH")) {
            bundle = new Bundle();
            bundle.putString("Menu", menu_item);
            if (!up_menu_item.equals("")) {
                bundle.putString("upMenu", up_menu_item);
            }
            intent.putExtras(bundle);
            intent.setClass(ProductActivity.this, SearchActivity.class);
        }
        else {
            switch (menu_item) {
                case "DISH":
                    intent.setClass(ProductActivity.this, MainActivity.class);
                    break;
                case "CAKE":
                    intent.setClass(ProductActivity.this, CakeActivity.class);
                    break;
                case "PHONE":
                    intent.setClass(ProductActivity.this, PhoneActivity.class);
                    break;
                case "CAMERA":
                    intent.setClass(ProductActivity.this, CameraActivity.class);
                    break;
                case "BOOK":
                    intent.setClass(ProductActivity.this, BookActivity.class);
                    break;
                case "MEMO":
                    bundle = new Bundle();
                    bundle.putString("Menu", up_menu_item);
                    intent.putExtras(bundle);
                    intent.setClass(ProductActivity.this, MemoActivity.class);
                    break;
                default:
                    intent.setClass(ProductActivity.this, MainActivity.class);
            }
        }
        if (notification_list != null) {
            if (preTask != null) {
                try {
                    preTask.moveToFront();
                    intent.replaceExtras(new Bundle());
                    finishAndRemoveTask();
                }catch (Exception e) {      // user has removed the task from the recent screen (task)
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.AppTask> tasks = am.getAppTasks();
                    if (tasks.size() > 1) {
                        preTask = tasks.get(tasks.size()-1);
                        preTask.moveToFront();
                        intent.replaceExtras(new Bundle());
                        finishAndRemoveTask();
                    }
                    else {
                        startActivity(intent);
                        ProductActivity.this.finish();
                    }
                }
            }
            else {
                Log.i("PreTask===> ", "null !");        //default value, have only one task
                startActivity(intent);
                ProductActivity.this.finish();
            }
        }
        else {
            startActivity(intent);
            ProductActivity.this.finish();
        }

    }

}
