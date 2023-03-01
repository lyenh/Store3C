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
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = am.appTasks
                if (tasks.size > 1) {
                    preTask = tasks[tasks.size - 1]// Should be the main task
                    preTask = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        for (i in tasks.indices) {
                            if (tasks[i].taskInfo.taskId == MainActivity.taskIdMainActivity) {
                                preTask = tasks[i]      // Should be the main task
                            }
                        }
                    } else {
                        for (i in tasks.indices) {
                            if (tasks[i].taskInfo.persistentId == MainActivity.taskIdMainActivity) {
                                preTask = tasks[i]      // Should be the main task
                            }
                        }
                    }
                    if (preTask == null) {
                        preTask = tasks[tasks.size - 1]
                        Toast.makeText(
                            this@OrderFormActivity,
                            "MainActivity taskId is not found !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
                menuItem = "DISH"
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
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val tasks = am.appTasks
            if (tasks.size > 1) {
                preTask = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    for (i in tasks.indices) {
                        if (tasks[i].taskInfo.taskId == MainActivity.taskIdMainActivity) {
                            preTask = tasks[i]      // Should be the main task
                        }
                    }
                } else {
                    for (i in tasks.indices) {
                        if (tasks[i].taskInfo.persistentId == MainActivity.taskIdMainActivity) {
                            preTask = tasks[i]      // Should be the main task
                        }
                    }
                }
                if (preTask == null) {
                    preTask = tasks[tasks.size - 1]
                    Toast.makeText(
                        this@OrderFormActivity,
                        "MainActivity taskId is not found !",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (preTask != null) {
                try {
                    preTask!!.moveToFront()
                    intent.replaceExtras(Bundle())
                    intent.setAction("")
                    intent.setData(null)
                    intent.setFlags(0)
                    finishAndRemoveTask()
                } catch (e: Exception) {      // user has removed the task from the recent screen (task)
                    val tasksRemain = am.appTasks
                    if (tasksRemain.size > 1) {
                        preTask = null
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            for (i in tasksRemain.indices) {
                                if (tasksRemain[i].taskInfo.taskId == MainActivity.taskIdMainActivity) {
                                    preTask = tasksRemain[i]      // Should be the main task
                                }
                            }
                        } else {
                            for (i in tasksRemain.indices) {
                                if (tasksRemain[i].taskInfo.persistentId == MainActivity.taskIdMainActivity) {
                                    preTask = tasksRemain[i]      // Should be the main task
                                }
                            }
                        }
                        if (preTask == null) {
                            preTask = tasksRemain[tasksRemain.size - 1]
                            Toast.makeText(this@OrderFormActivity,"MainActivity taskId is not found !", Toast.LENGTH_SHORT).show()
                        }
                        preTask!!.moveToFront()
                        intent.replaceExtras(Bundle())
                        intent.setAction("")
                        intent.setData(null)
                        intent.setFlags(0)
                        finishAndRemoveTask()
                    } else {
                        intent.flags = 0
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        this@OrderFormActivity.finish()
                    }
                }
            } else {
                Log.i("PreTask===> ", "null !") //default value, have only one task
                intent.flags = 0
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
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

