package com.example.user.store3c

import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import android.content.ComponentCallbacks2
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

@Keep
class OrderFormActivity : AppCompatActivity() , View.OnClickListener, ComponentCallbacks2{

    private var menuItem = "DISH"
    private var upMenuItem = ""; private var searchItem = ""
    private lateinit var orderText:TextView
    private lateinit var userRef: DatabaseReference
    private var orderFormList: String = "購買資料: "
    private var orderFromFullData: String = "購買明細資料: "
    private var notification_list = ""; private  var orderMessageText = ""
    private var preTask: AppTask? = null
    private var dbHelper: AccountDbAdapter? = null
    private var DbMainActivityTaskId = -1; private var DbOrderActivityTaskId = -1
    private var systemClearTask = false
    @Volatile private lateinit var am: ActivityManager
    @Volatile private lateinit var tasks: List<AppTask>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_form)

        val bundle = intent.extras
        if (bundle != null) {
            notification_list = bundle.getString("Notification").toString()
            orderMessageText = bundle.getString("OrderMessageText").toString()
            if (notification_list != "") {   // notification promotion product
                orderFormList = orderFormList + "\n\n" + orderMessageText
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

        if (InternetConnection.checkConnection(this@OrderFormActivity)) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                if (!currentUser.isAnonymous) {
                    userRef = Firebase.database.reference.child("user")
                    userRef.child("Uid").get().addOnSuccessListener {
                        val key: String = currentUser.uid
                        var findOrderData = false
                        var orderFormEmail: String;
                        var orderFormName: String;
                        var orderFromDate: String;
                        var orderFormPrice: String
                        var orderFormData: String;
                        var orderFormProductData: String
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
                                            orderFormName =
                                                "姓名: " + dataItem.child("userName").value
                                            orderFormEmail =
                                                "帳號: " + dataItem.child("emailAccount").value
                                            orderFromDate =
                                                "日期: " + dataItem.child("listDate").value
                                            orderFormPrice =
                                                "        總金額: " + dataItem.child("totalPrice").value + "元"
                                            orderFormData =
                                                orderFormName + "\n" + orderFormEmail + "\n" + orderFromDate
                                            orderFormProductData = "產品項目: "
                                            for (listItem in dataItem.child("listItem").children) {
                                                val item: ListItem? = listItem.getValue<ListItem>()
                                                orderFormProductData =
                                                    orderFormProductData + "\n" + "        " + item!!.getName() + " :  " + item.getPrice()
                                            }
                                            orderFromFullData =
                                                orderFromFullData + "\n\n" + orderFormData + "\n" + orderFormProductData + "\n" + divideLine + "\n" + orderFormPrice
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
                    }.addOnFailureListener {
                        orderText.text = orderFormList
                        Log.e("firebase", "Error getting data", it)
                    }
                } else {
                    orderText.text = orderFormList
                    Toast.makeText(
                        this@OrderFormActivity,
                        "請先登入, 再查詢完整的訂購單 !",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                orderText.text = orderFormList
                Toast.makeText(
                    this@OrderFormActivity,
                    "請先登入, 再查詢完整的訂購單 !",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(this@OrderFormActivity, "網路未連線! ", Toast.LENGTH_SHORT).show()
        }
        retButton.setOnClickListener(this)

    }

    @Synchronized
    override fun onTrimMemory(level: Int) {
        am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        tasks = am.appTasks
        val currentTask: AppTask
        var mainTask: AppTask? = null
        currentTask = tasks[0]

        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                //Toast.makeText(this, "ProductActivity: UI_HIDDEN !", Toast.LENGTH_SHORT).show();
                val outInfo = ActivityManager.MemoryInfo()
                am.getMemoryInfo(outInfo)
                if (outInfo.lowMemory) {
                    systemClearTask = true
                    //Toast.makeText(this@OrderFormActivity, "OrderFormActivity: in lowMemory ", Toast.LENGTH_SHORT).show()
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (tasks.size > 3 || systemClearTask) {
                        try {
                            //Toast.makeText(this, "ProductActivity: system clear recentTaskList !", Toast.LENGTH_SHORT).show();
                            Thread.sleep(1000)
                            currentTask.moveToFront()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    if (systemClearTask) {
                        try {
                            //Toast.makeText(this, "OrderFormActivity: system clear recentTaskList: " + tasks.size, Toast.LENGTH_SHORT).show();
                            Thread.sleep(2000)
                            currentTask.moveToFront()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            TRIM_MEMORY_COMPLETE, TRIM_MEMORY_MODERATE, TRIM_MEMORY_RUNNING_CRITICAL -> {
                //Toast.makeText(this, "OrderFormActivity: TRIM_MEMORY_COMPLETE !", Toast.LENGTH_SHORT).show();
                if (dbHelper == null) {
                    dbHelper = AccountDbAdapter(this)
                }
                if (!dbHelper!!.IsDbTaskIdEmpty()) {
                    try {
                        val cursor = dbHelper!!.taskIdList
                        DbMainActivityTaskId = cursor.getInt(2)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                if (tasks.size > 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        for (i in tasks.indices) {
                            if (tasks[i].taskInfo != null && tasks[i].taskInfo.taskId == DbMainActivityTaskId && DbMainActivityTaskId != taskId) {
                                mainTask = tasks[i]
                            }
                        }
                        if (mainTask != null) {
                            try {
                                mainTask.finishAndRemoveTask()
                                //Toast.makeText(this, "OrderFormActivity: Memory is extremely low, free main task !", Toast.LENGTH_LONG).show()
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                //Toast.makeText(this, "OrderFormActivity: catch exception, Memory low !", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            else -> Log.i("ComponentCallbacks2 =>", "default event !")
                //Toast.makeText(this@OrderFormActivity, "OrderFormActivity: default !", Toast.LENGTH_SHORT).show()
        }
        super.onTrimMemory(level)
    }

    override fun onLowMemory() {
        systemClearTask = true
        //Toast.makeText(this, "OrderFormActivity: LowMemory !", Toast.LENGTH_SHORT).show()
        super.onLowMemory()
    }

    override fun onDestroy() {
        dbHelper?.close()
        super.onDestroy()
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
            am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            tasks = am.appTasks
            var currentTask: AppTask? = null
            preTask = null

            if (dbHelper == null) {
                dbHelper = AccountDbAdapter(this)
            }
            try {
                if (!dbHelper!!.IsDbTaskIdEmpty()) {
                    val cursor = dbHelper!!.getTaskIdList()
                    DbMainActivityTaskId = cursor.getInt(2)
                    DbOrderActivityTaskId = cursor.getInt(3)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            if (tasks.size > 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        for (i in tasks.indices) {
                            if (tasks[i].taskInfo.taskId == DbMainActivityTaskId && DbMainActivityTaskId != taskId) {
                                preTask = tasks[i]      // Should be the main task
                            }
                            if (tasks[i].taskInfo.taskId == taskId) {
                                currentTask = tasks[i]
                            }
                        }
                    } catch (e: Exception) {
                        Log.i("Get preTask Id error: ", "main ==>" + e.message)
                    }
                } else {
                    try {
                        for (i in tasks.indices) {
                            if (tasks[i].taskInfo.persistentId == DbMainActivityTaskId && DbMainActivityTaskId != taskId) {
                                preTask = tasks[i]      // Should be the main task
                              //  Toast.makeText(this@OrderFormActivity, "Got mainTask persistentId", Toast.LENGTH_SHORT).show()
                            }
                            if (tasks[i].taskInfo.persistentId == taskId) {
                                currentTask = tasks[i]
                              //  Toast.makeText(this@OrderFormActivity, "Got currentTask persistentId", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        preTask = null
                        currentTask = null
                      //  Toast.makeText(this@OrderFormActivity, "Get task persistentId error!", Toast.LENGTH_SHORT).show()
                    }
                }
                if (preTask == null) {
                    if (DbOrderActivityTaskId != -1) {
                        try {
                            for (i in tasks.indices) {
                                if (tasks[i].taskInfo.persistentId == DbOrderActivityTaskId) {
                                    if (tasks[i].taskInfo.persistentId != taskId) {
                                        preTask = tasks[i]
                                        //Toast.makeText(this, "Got order preTask! ", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.i("Get preTask Id error: ", "order ==>" + e.message)
                        }
                    }
                }
                if (preTask == null) {
                    preTask = tasks[tasks.size - 1]
                    //Toast.makeText(this@OrderFormActivity,"MainActivity and OrderActivity(from notification product buy) taskId is not found !", Toast.LENGTH_SHORT).show()
                }
            }
            if (preTask != null) {
                try {
                    preTask!!.moveToFront()
                    intent.replaceExtras(Bundle())
                    intent.setAction("")
                    intent.setData(null)
                    intent.setFlags(0)
                    if (currentTask != null) {
                        currentTask.finishAndRemoveTask()
                    } else {
                        finishAndRemoveTask()
                    }
                } catch (e: Exception) {      // prevent the system drop the preTask
                    val tasksRemain = am.appTasks
                    preTask = null
                    currentTask = null
                    if (tasksRemain.size > 1) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                for (i in tasksRemain.indices) {
                                    if (tasksRemain[i].taskInfo.taskId == DbMainActivityTaskId && DbMainActivityTaskId != taskId) {
                                        preTask = tasksRemain[i]      // Should be the main task
                                    }
                                    if (tasksRemain[i].taskInfo.taskId == taskId) {
                                        currentTask = tasksRemain[i]
                                    }
                                }
                            } catch (e: Exception) {
                                Log.i("Get preTask Id error: ", "main ==>" + e.message)
                            }
                        } else {
                            try {
                                for (i in tasksRemain.indices) {
                                    if (tasksRemain[i].taskInfo.persistentId == DbMainActivityTaskId && DbMainActivityTaskId != taskId) {
                                        preTask = tasksRemain[i]      // Should be the main task
                                    }
                                    if (tasksRemain[i].taskInfo.persistentId == taskId) {
                                        currentTask = tasksRemain[i]
                                    }
                                }
                            } catch (e: Exception) {
                                preTask = null
                                currentTask = null
                            }
                        }
                        if (preTask == null) {
                            preTask = tasksRemain[tasksRemain.size - 1]
                            //Toast.makeText(this@OrderFormActivity,"MainActivity taskId is not found !", Toast.LENGTH_SHORT).show()
                        }
                        preTask!!.moveToFront()
                        intent.replaceExtras(Bundle())
                        intent.setAction("")
                        intent.setData(null)
                        intent.setFlags(0)
                        if (currentTask != null) {
                            currentTask.finishAndRemoveTask()
                        } else {
                            finishAndRemoveTask()
                        }
                    } else {
                        intent.flags = 0
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        val retainRecentTaskBundle = Bundle()
                        retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY")
                        intent.putExtras(retainRecentTaskBundle)
                        startActivity(intent)
                        this@OrderFormActivity.finish()
                    }
                }
            } else {
                Log.i("PreTask===> ", "null !") //default value, have only one task
                intent.flags = 0
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                val retainRecentTaskBundle = Bundle()
                retainRecentTaskBundle.putString("RetainRecentTask", "RECENT_ACTIVITY")
                intent.putExtras(retainRecentTaskBundle)
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

