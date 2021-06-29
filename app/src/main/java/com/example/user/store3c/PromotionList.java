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
    public String listDate;
    public ArrayList<ListItem> listItem;
    public String totalPrice;
    public String userId;
    public String userName;
    public String userToken;


    public PromotionList() {

    }

    public PromotionList(String Account, String Date, ArrayList<ListItem> Item, String Price, String Id, String Name, String Token) {
        this.emailAccount = Account;
        this.listDate = Date;
        this.listItem = Item;
        this.totalPrice = Price;
        this.userId = Id;
        this.userName = Name;
        this.userToken = Token;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<ListItem> getListItem() {
        return listItem;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("emailAccount", this.emailAccount);
        result.put("listDate", this.listDate);
        result.put("listItem", this.listItem);
        result.put("totalPrice", this.totalPrice);
        result.put("userId", this.userId);
        result.put("userName", this.userName);
        result.put("userToken", this.userToken);
        return result;
    }

}
