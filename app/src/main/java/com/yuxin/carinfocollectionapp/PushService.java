package com.yuxin.carinfocollectionapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.Time;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Yuxin Zhang on 21/4/19
 * Monash University,
 * yuxin.zhang@monash.edu.
 */
public class PushService extends Service {

    private static Context sContext = null;
    public static Context getsContext(){
        return sContext;
    }

    /**
     * Modified by Du Kaiying on 2016/12/13
     */
    //Special Time Period
    int StartHour = 22;
    int StartMin = 0;
    int EndHour = 8;
    int EndMin = 0;

    static Timer timer = null;
    //Execute Notification Period Every 30 minutes
    static long period = 30*60*1000;
    //static long period = 10*1000;
    public static void cleanAllNotification(){
        NotificationManager notificationManager = (NotificationManager)UploadActivity.getsContext().
                getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        //notificationManager.cancel(0);

        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }
    public static void addNotification(int delayTime, String tickerText, String contentTitle, String contentText){
        Intent intent = new Intent(UploadActivity.getsContext(),PushService.class);
        intent.putExtra("delayTime", delayTime);
        intent.putExtra("tickerText", tickerText);
        intent.putExtra("contentTitle", contentTitle);
        intent.putExtra("contentText", contentText);
        UploadActivity.getsContext().startService(intent);
    }
    public void onCreate(){

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public int onStartCommand(final  Intent intent, int flags, int startId){

        long delay = intent.getIntExtra("delayTime",0);

        /*if(isCurrentInTimeScope(StartHour,StartMin,EndHour,EndMin)){
            delay = getTimeExpand(EndHour,EndMin);
        }*/

        if(timer == null){
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager notificationManager = (NotificationManager) PushService.this.getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(PushService.this);
                Intent notificationIntent = new Intent(PushService.this, RegistrationActivity.class);

                //New Task
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //New Task
                PendingIntent contentIntent = PendingIntent.getActivity(PushService.this, 0, notificationIntent, 0);
                builder.setContentIntent(contentIntent);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setTicker(intent.getStringExtra("tickerText"));
                builder.setContentText(intent.getStringExtra("contentText"));
                builder.setContentTitle(intent.getStringExtra("contentTitle"));
                builder.setAutoCancel(true);
                builder.setDefaults(Notification.DEFAULT_ALL);
                Notification notification = builder.build();
                notificationManager.notify((int) System.currentTimeMillis(), notification);

            }
        },delay,period);
        return super.onStartCommand(intent,flags,startId);
    }

    /**
     * Special Time Period / Created by Kaiying Du 2016.12.12
     */
    public long getTimeExpand(int EndHour,int EndMin){
        long currentTimeMillis = System.currentTimeMillis();
        long timeExpand = 0;
        long aDayInMillis = 1000 * 60 * 60 * 24;
        Time now = new Time();
        now.set(currentTimeMillis);

        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = EndHour;
        endTime.minute = EndMin;

        if (!now.before(endTime)) {
            //跨天
            timeExpand = aDayInMillis - now.toMillis(true) + endTime.toMillis(true);
        }
        else{
            timeExpand = endTime.toMillis(true) - now.toMillis(true) ;
        }

        return timeExpand;

    }
    public static boolean isCurrentInTimeScope(int beginHour, int beginMin, int endHour, int endMin) {
        boolean result = false;
        final long aDayInMillis = 1000 * 60 * 60 * 24;
        final long currentTimeMillis = System.currentTimeMillis();

        Time now = new Time();
        now.set(currentTimeMillis);

        Time startTime = new Time();
        startTime.set(currentTimeMillis);
        startTime.hour = beginHour;
        startTime.minute = beginMin;

        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = endHour;
        endTime.minute = endMin;

        if (!startTime.before(endTime)) {
            // 跨天的特殊情况（比如22:00-8:00）
            startTime.set(startTime.toMillis(true) - aDayInMillis);
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
            Time startTimeInThisDay = new Time();
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
            if (!now.before(startTimeInThisDay)) {
                result = true;
            }
        } else {
            // 普通情况(比如 8:00 - 14:00)
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
        }
        return result;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.purge();
        timer.cancel();
        timer = null;
        stopSelf();
    }
}
