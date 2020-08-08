package com.example.user.store3c;

import androidx.annotation.Keep;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@Keep
@IgnoreExtraProperties
public class UserItem {
    public String userToken;
    public String userEmail;
    public String userName;

    public UserItem() {

    }

    public UserItem (String token, String email, String name) {
        this.userToken = token;
        this.userEmail = email;
        this.userName = name;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userToken", this.userToken);
        result.put("userEmail", this.userEmail);
        result.put("userName", this.userName);
        return result;
    }

}
