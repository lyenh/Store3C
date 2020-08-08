package com.example.user.store3c;

import androidx.annotation.Keep;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Keep
@IgnoreExtraProperties
public class PromotionList {
    public String emailAccount;
    public String userName;
    public String listDate;
    public String totalPrice;
    public String userToken;
    public ArrayList<ListItem> listItem;

    public PromotionList() {

    }

    public PromotionList(String Account, String Name, String Date, String Price, String Token, ArrayList<ListItem> Item) {
        this.emailAccount = Account;
        this.userName = Name;
        this.listDate = Date;
        this.totalPrice = Price;
        this.userToken = Token;
        this.listItem = Item;
    }

    public String getEmailAccount() {
        return emailAccount;
    }

    public String getUserName() {
        return userName;
    }

    public String getListDate() {
        return listDate;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getUserToken() {
        return userToken;
    }

    public ArrayList<ListItem> getListItem() {
        return listItem;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("emailAccount", this.emailAccount);
        result.put("userName", this.userName);
        result.put("listDate", this.listDate);
        result.put("totalPrice", this.totalPrice);
        result.put("userToken", this.userToken);
        result.put("listItem", this.listItem);
        return result;
    }

}
