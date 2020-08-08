package com.example.user.store3c;

import android.graphics.Bitmap;

/**
 * Created by user on 2016/11/19.
 */

public class ProductItem {
    private Bitmap img;
    private String name;
    private String price;
    private String intro;

    ProductItem(Bitmap img, String name, String price, String intro) {
        this.img = img;
        this.name = name;
        this.price = price;
        this.intro = intro;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
