package com.yuxin.carinfocollectionapp;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yuxin Zhang on 21/4/19
 * Monash University,
 * yuxin.zhang@monash.edu.
 */
public class ExitApplication extends Application {

    private List<Activity> activityList = new LinkedList<Activity>();
    private static ExitApplication instance;
    private ExitApplication(){

    }
    public static ExitApplication getInstance(){
        if(instance == null){
            instance = new ExitApplication();
        }
        return instance;
    }
    public void addActivity(Activity activity){
        activityList.add(activity);
    }

    public void exit(){
        for(Activity activity:activityList){
            activity.finish();
        }
        System.exit(0);
    }

}



