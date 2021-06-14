package com.example.user.store3c;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener{

    private String menu_item, up_menu_item = "", product_name, product_price, product_intro, order_list = "", search_list = "";
    private byte[] product_pic;

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
            //product_pic = bundle.getInt("Pic");
            //pic.setImageResource(product_pic);
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
        if (screenWidth > 800) {
            imgHeight = (product_intro.length() / 27 + 1) * 60;
            introView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));

        }

        intro.setText(product_intro);
        ret_b.setOnClickListener(this);
        buy_b.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.productReturnBtn_id:
                Intent intentItem = new Intent();
                if (order_list.equals("ORDER")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    if (!up_menu_item.equals("")) {
                        bundle.putString("upMenu", up_menu_item);
                    }
                    intentItem.putExtras(bundle);
                    intentItem.setClass(ProductActivity.this, OrderActivity.class);
                }
                else if (search_list.equals("SEARCH")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("Menu", menu_item);
                    if (!up_menu_item.equals("")) {
                        bundle.putString("upMenu", up_menu_item);
                    }
                    intentItem.putExtras(bundle);
                    intentItem.setClass(ProductActivity.this, SearchActivity.class);
                }
                else {
                    switch (menu_item) {
                        case "DISH":
                            intentItem.setClass(ProductActivity.this, MainActivity.class);
                            break;
                        case "CAKE":
                            intentItem.setClass(ProductActivity.this, CakeActivity.class);
                            break;
                        case "PHONE":
                            intentItem.setClass(ProductActivity.this, PhoneActivity.class);
                            break;
                        case "CAMERA":
                            intentItem.setClass(ProductActivity.this, CameraActivity.class);
                            break;
                        case "BOOK":
                            intentItem.setClass(ProductActivity.this, BookActivity.class);
                            break;
                        default:
                            Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                            intentItem.setClass(ProductActivity.this, MainActivity.class);
                    }
                }
                startActivity(intentItem);
                ProductActivity.this.finish();
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
                startActivity(intent);
                ProductActivity.this.finish();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if (order_list.equals("ORDER")) {
            Bundle bundle = new Bundle();
            bundle.putString("Menu", menu_item);
            if (!up_menu_item.equals("")) {
                bundle.putString("upMenu", up_menu_item);
            }
            intent.putExtras(bundle);
            intent.setClass(ProductActivity.this, OrderActivity.class);
        }
        else if (search_list.equals("SEARCH")) {
            Bundle bundle = new Bundle();
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
                default:
                    Toast.makeText(this.getBaseContext(), "Return to main menu ! ", Toast.LENGTH_SHORT).show();
                    intent.setClass(ProductActivity.this, MainActivity.class);
            }
        }
        startActivity(intent);
        ProductActivity.this.finish();

    }

}
