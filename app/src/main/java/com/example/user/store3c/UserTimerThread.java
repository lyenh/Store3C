package com.example.user.store3c;

/**
 * Created by user on 2016/10/20.
 */

public class UserTimerThread extends Thread{
    MainActivity activity;
    private int what = 1;

    UserTimerThread(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        //super.run();
        while(MainActivity.TimerThread == 1) {
            MainActivity.userAdHandler.sendEmptyMessage((what++) % 5);
            try{
                Thread.sleep(4000);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Log.v("Thread ===>","TimerThread: " + activity.TimerThread);
    }

}
