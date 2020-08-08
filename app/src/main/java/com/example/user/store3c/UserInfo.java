package com.example.user.store3c;

import androidx.annotation.Keep;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@Keep
@IgnoreExtraProperties
public class UserInfo {
    public String userLatestLoginTime;
    public String userLatestToken;
    public String userEmail;
    public String userName;
    public String userPassword;
    public boolean userAccountExist;
    public UserDeviceInfo userDeviceInfo;

    public UserInfo() {

    }

    public UserInfo (String loginTime, String token, String email, String name, String password, boolean account, UserDeviceInfo deviceInfo) {
        this.userLatestLoginTime = loginTime;
        this.userLatestToken = token;
        this.userEmail = email;
        this.userName = name;
        this.userPassword = password;
        this.userAccountExist = account;
        this.userDeviceInfo = deviceInfo;
    }

    public String getUserLatestLoginTime() {
        return userLatestLoginTime;
    }

    public void setUserLatestLoginTime(String userLatestLoginTime) {
        this.userLatestLoginTime = userLatestLoginTime;
    }

    public String getUserLatestToken() {
        return userLatestToken;
    }

    public void setUserLatestToken(String userLatestToken) {
        this.userLatestToken = userLatestToken;
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

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public boolean isUserAccountExist() {
        return userAccountExist;
    }

    public void setUserAccountExist(boolean userAccountExist) {
        this.userAccountExist = userAccountExist;
    }

    public UserDeviceInfo getUserDeviceInfo() {
        return userDeviceInfo;
    }

    public void setUserDeviceInfo(UserDeviceInfo userDeviceInfo) {
        this.userDeviceInfo = userDeviceInfo;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userLatestLoginTime", this.userLatestLoginTime);
        result.put("userLatestToken", this.userLatestToken);
        result.put("userEmail", this.userEmail);
        result.put("userName", this.userName);
        result.put("userPassword", this.userPassword);
        result.put("userAccountExist", this.userAccountExist);
        result.put("userDeviceInfo", this.userDeviceInfo.toMap());
        return result;
    }

    @Keep
    @IgnoreExtraProperties
    public static class UserDeviceInfo {
        public String deviceManufacturer;
        public String deviceModel;
        public int deviceSDKversion;

        public UserDeviceInfo() {

        }

        public UserDeviceInfo(String manufacturer, String model, int SDKversion) {
            this.deviceManufacturer = manufacturer;
            this.deviceModel = model;
            this.deviceSDKversion = SDKversion;
        }

        public String getDeviceManufacturer() {
            return deviceManufacturer;
        }

        public void setDeviceManufacturer(String deviceManufacturer) {
            this.deviceManufacturer = deviceManufacturer;
        }

        public String getDeviceModel() {
            return deviceModel;
        }

        public void setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
        }

        public int getDeviceSDKversion() {
            return deviceSDKversion;
        }

        public void setDeviceSDKversion(int deviceSDKversion) {
            this.deviceSDKversion = deviceSDKversion;
        }

        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("deviceManufacturer", this.deviceManufacturer);
            result.put("deviceModel", this.deviceModel);
            result.put("deviceSDKversion", this.deviceSDKversion);
            return result;
        }
    }

}
