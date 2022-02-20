package com.example.user.store3c

import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class OrderFormActivity : AppCompatActivity() , View.OnClickListener{

    companion object {
        private var orderFormList: String = "購買資料: "
    }

    private var menuItem = "DISH"
    private var upMenuItem = ""; private var searchItem = ""
    private lateinit var orderText:TextView
    private lateinit var userRef: DatabaseReference
    private var orderFromFullData: String = "購買明細資料: "
    private var notification_list = ""
    private var preTask: AppTask? = null
    private var upActivityName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_form)

        val bundle = intent.extras
        if (bundle != null) {
            notification_list = bundle.getString("Notification").toString()
            if (notification_list != "") {   // notification promotion product
                orderFormList = orderFormList + "\n\n" + PromotionFirebaseMessagingService.orderMessageText
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val tasks = am.appTasks
                    if (tasks.size > 1) {
                        preTask = tasks[1] // Should be the main task
                    }
                    Log.i("=====> ", "task size: " + tasks.size)
                    var appActivity: String
                    var numActivity: Int
                    var eachTask: AppTask
                    for (i in tasks.indices) {
                        eachTask = tasks[i]
                        Log.i("Message Task Num ===> ", "num: $i")
                        numActivity = eachTask.taskInfo.numActivities
                        Log.i("NumActivity ===> ", "NumActivity: $numActivity")
                        if (eachTask.taskInfo.baseActivity != null) {
                            appActivity = (eachTask.taskInfo.baseActivity)!!.shortClassName.substring(1)
                            Log.i("BaseActivity ===> ", "BaseActivity: $appActivity")
                        }
                        if (eachTask.taskInfo.topActivity != null) {
                            appActivity = (eachTask.taskInfo.topActivity)!!.shortClassName.substring(1)
                            Log.i("TopActivity ===> ", "TopActivity: $appActivity")
                        }
                    }

                    val upActivity = tasks[0].taskInfo.topActivity?.shortClassName
                    if (upActivity != null) {
                        upActivityName = upActivity.substring(1)
                        Log.i("=====> ", "activity: $upActivityName")
                       // Toast.makeText(
                       //     this@OrderFormActivity,
                       //     "TopActivity: " + upActivityName,
                       //     Toast.LENGTH_SHORT
                       // ).show()
                    }
                    else {
                        upActivityName = "MainActivity"
                    }
                    when (upActivityName) {
                        "MainActivity" -> menuItem = "DISH"
                        "CakeActivity" -> {
                            menuItem = "CAKE"
                            if (YouTubeFragment.YPlayer != null) {
                                try {
                                    if (YouTubeFragment.YPlayer.isPlaying) {
                                        YouTubeFragment.YPlayer.pause()
                                        Toast.makeText(this@OrderFormActivity, "play pause", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@OrderFormActivity,
                                        "YPayer have released: " + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        "PhoneActivity" -> menuItem = "PHONE"
                        "CameraActivity" -> {
                            menuItem = "CAMERA"
                            if (YouTubeFragment.YPlayer != null) {
                                try {
                                    if (YouTubeFragment.YPlayer.isPlaying) {
                                        YouTubeFragment.YPlayer.pause()
                                        Toast.makeText(this@OrderFormActivity, "play pause", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@OrderFormActivity,
                                        "YPayer have released: " + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        "BookActivity" -> menuItem = "BOOK"
                        "MemoActivity" -> {
                            menuItem = "MEMO"
                            upMenuItem = "DISH"
                        }
                        "SearchActivity" -> {
                            searchItem = "SEARCH"
                            menuItem = "DISH"
                        }
                        "UserActivity" -> {
                            menuItem = "USER"
                            upMenuItem = "DISH"
                        }
                        "PositionActivity" -> {
                            menuItem = "POSITION"
                            upMenuItem = "DISH"
                            if (YouTubeFragment.YPlayer != null) {
                                try {
                                    if (YouTubeFragment.YPlayer.isPlaying) {
                                        YouTubeFragment.YPlayer.pause()
                                        Toast.makeText(this@OrderFormActivity, "play pause", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@OrderFormActivity,
                                        "YPayer have released: " + e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        "ProductActivity" -> {
                            menuItem = "PRODUCT"
                            upMenuItem = "DISH"
                        }
                        "MapsActivity" -> {
                            menuItem = "MAP"
                            upMenuItem = "DISH"
                        }
                        "LoginActivity" -> {
                            menuItem = "LOGIN"
                            upMenuItem = "DISH"
                        }
                        "PageActivity" -> {
                            menuItem = "PAGE"
                            upMenuItem = "DISH"
                        }
                        else -> menuItem = "DISH"
                    }
                }
                else {
                    menuItem = "DISH"
                }
            }
            else {
                if (bundle.getString("Menu") != null)
                    menuItem = bundle.getString("Menu")!!
                if (bundle.getString("upMenu") != null)
                    upMenuItem = bundle.getString("upMenu")!!
            }
        }
        else {
            Toast.makeText(this@OrderFormActivity,"orderForm bundle null ", Toast.LENGTH_SHORT).show()
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbarOrderForm)
        setSupportActionBar(toolbar)
        val editTitle = "購物車資料"
        supportActionBar?.setLogo(R.drawable.store_logo)
        supportActionBar?.title = editTitle

        orderText = findViewById(R.id.orderFormItem_id)
        val retButton:Button = findViewById(R.id.orderFormReturnBtn_id)

        userRef = Firebase.database.reference.child("user")
        userRef.child("Uid").get().addOnSuccessListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                if (!currentUser.isAnonymous) {
                    val key:String = currentUser.uid
                    var findOrderData = false
                    var orderFormEmail:String ; var orderFormName:String ; var orderFromDate:String ; var orderFormPrice:String
                    var orderFormData:String ; var orderFormProductData:String
                    val divideLine = "        ------------------------------------------"

                    for (uid in it.children) {
                        val fbUid: String? = uid.key
                        //Log.i("Firebase ==>", "Firebase Uid is: " + fbUid);
                        if (fbUid != null) {
                            if (fbUid == key) {
                                if (uid.child("orderList").exists()) {
                                    //Log.i("firebase", "Got value ${uid.child("orderList").value}")
                                    findOrderData = true
                                    for (dataItem in uid.child("orderList").children) {
                                        orderFormName = "姓名: " + dataItem.child("userName").value
                                        orderFormEmail = "帳號: " + dataItem.child("emailAccount").value
                                        orderFromDate = "日期: " + dataItem.child("listDate").value
                                        orderFormPrice = "        總金額: " + dataItem.child("totalPrice").value + "元"
                                        orderFormData = orderFormName + "\n" + orderFormEmail + "\n" + orderFromDate
                                        orderFormProductData = "產品項目: "
                                        for (listItem in dataItem.child("listItem").children) {
                                            val item:ListItem? = listItem.getValue<ListItem>()
                                            orderFormProductData = orderFormProductData + "\n" + "        " + item!!.getName() + " :  " + item.getPrice()
                                        }
                                        orderFromFullData = orderFromFullData + "\n\n" + orderFormData + "\n" + orderFormProductData + "\n" + divideLine + "\n" + orderFormPrice
                                    }
                                    orderText.setTextIsSelectable(true)
                                    orderText.text = orderFromFullData
                                }
                            }
                        }
                    }
                    if (!findOrderData) {
                        orderFromFullData = "尚未購買產品喔 !"
                        orderText.text = orderFromFullData
                    }
                } else {
                    orderText.text = orderFormList
                    Toast.makeText(this@OrderFormActivity, "請先登入, 再查詢完整的訂購單 !", Toast.LENGTH_LONG).show()
                }
            } else {
                orderText.text = orderFormList
                Toast.makeText(this@OrderFormActivity, "請先登入, 再查詢完整的訂購單 !", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener{
            orderText.text = orderFormList
            Log.e("firebase", "Error getting data", it)
        }

        retButton.setOnClickListener(this)

    }

    override fun onBackPressed() {
        val intent = Intent()
        val bundle: Bundle?
        bundle = Bundle()
        if (searchItem != "") {
            bundle.putString("Search", searchItem)
        }
        bundle.putString("Menu", menuItem)
        if (upMenuItem != "") {
            bundle.putString("upMenu", upMenuItem)
        }
        intent.putExtras(bundle)
        intent.setClass(this@OrderFormActivity, OrderActivity::class.java)

        if (notification_list != "") {
            if (notification_list == "IN_APP") {
                if (preTask != null) {
                    preTask?.moveToFront()
                    intent.replaceExtras(Bundle())
                    this@OrderFormActivity.finish()
                }
                else {
                    startActivity(intent)
                    this@OrderFormActivity.finish()
                }
            } else {
                startActivity(intent)
                this@OrderFormActivity.finish()
            }
        } else {
            startActivity(intent)
            this@OrderFormActivity.finish()
        }

    }

    override fun onClick(v: View?) {
        if (v != null) {
            if (v.id == R.id.orderFormReturnBtn_id) {
                onBackPressed()
            }
        }

    }

}

