package com.yuxin.carinfocollectionapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadActivity extends AppCompatActivity {

    static String name = null;
    static String model = null;
    static String plate = null;
    static String AC_status = null;
    static String Passengers = null;
    static String fileName = null;

    static boolean isCoolPressed = false;
    static boolean isHeatPressed = false;
    static boolean isStatus0 = false;
    static boolean isStatus1 = false;
    static boolean isStatus2 = false;
    static boolean isStatus3 = false;
    static boolean isStatus_auto = false;
    static boolean isPassenger0 = false;
    static boolean isPassenger1 = false;
    static boolean isPassenger2 = false;
    static boolean isPassenger3 = false;
    static boolean isPassenger4 = false;

    static boolean isNotification = true;
    static boolean isLBSEnable = false;
    static boolean isGPSEnable = false;
    static double latitude;
    static double longitude;
    static long time;
    static float speed;
    static String phoneTime;
    LocationManager locationManager;
    private static final long MIN_DISTANCE_FOR_UPDATES = 0; //10 meters
    private static final long MIN_TIME_BW_UPDATES = 0; //1000*60*1; //1 minute

    private TextView tv_GPS;
    static CheckBox cb_Cool;
    static CheckBox cb_Heat;
    static Button btn_status0;
    static Button btn_status1;
    static Button btn_status2;
    static Button btn_status3;
    static Button btn_status_auto;
    static Button btn_passenger0;
    static Button btn_passenger1;
    static Button btn_passenger2;
    static Button btn_passenger3;
    static Button btn_passenger4;

    //init wifi
    static boolean sendFlag = false;
    static int sendDelay = 10*1000;
    static String sendMsg = "NULL";
    static int sendPort = 8904;
    static String sendIPAddress = "255.255.255.255";

    public UdpHelper udpHelper;

    WifiReceiver wifiReceiver;

    private static Context sContext = null;

    public static Context getsContext() {
        return sContext;
    }

    static int delaytime = 30 * 60 * 1000;
    //static int delaytime = 10*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //change policy
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_upload);

        sContext = this;
        System.out.println("upload activity started!");
        ExitApplication.getInstance().addActivity(this);

        //Get parameters from previous activity
        Bundle bundleFromPreviousActivity = this.getIntent().getExtras();
        name = bundleFromPreviousActivity.getString("name");
        model = bundleFromPreviousActivity.getString("model");
        plate = bundleFromPreviousActivity.getString("plate");
        isNotification = bundleFromPreviousActivity.getBoolean("notification");
        sendIPAddress = bundleFromPreviousActivity.getString("IpAddr");
        sendPort = bundleFromPreviousActivity.getInt("port");
        sendDelay = bundleFromPreviousActivity.getInt("delay");

        //Check wifi and initiate Udphelper

        if(checkWifi().isWifiEnabled()){

//            if(wifiReceiver == null){
//                wifiReceiver = new WifiReceiver();
//            }
//
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//            registerReceiver(wifiReceiver, filter);

            sendFlag = true;
            udpHelper = new UdpHelper(checkWifi());

        }else{
            Toast.makeText(UploadActivity.this, "请连接wifi ", Toast.LENGTH_SHORT).show();
        }

        tv_GPS = (TextView) findViewById(R.id.tv_GPS);
        cb_Cool = (CheckBox) findViewById(R.id.checkBox_Cool);
        cb_Heat = (CheckBox) findViewById(R.id.checkBox_Heat);
        final TextView tv_AC_model = (TextView) findViewById(R.id.AC_model);
        final TextView tv_AC_status = (TextView) findViewById(R.id.AC_status);
        final TextView tv_Passenger = (TextView) findViewById(R.id.passenger);
        final Button btn_log = (Button) findViewById(R.id.btn_log);
        final Button btn_upload = (Button) findViewById(R.id.btn_upload);
        TextView tv_re = (TextView) findViewById(R.id.re_registration);
        TextView tv_re_wifi = findViewById(R.id.re_wifi);

        btn_status0 = (Button) findViewById(R.id.status0);
        btn_status1 = (Button) findViewById(R.id.status1);
        btn_status2 = (Button) findViewById(R.id.status2);
        btn_status3 = (Button) findViewById(R.id.status3);
        btn_status_auto = (Button) findViewById(R.id.status_auto);
        btn_passenger0 = (Button) findViewById(R.id.passenger0);
        btn_passenger1 = (Button) findViewById(R.id.passenger1);
        btn_passenger2 = (Button) findViewById(R.id.passenger2);
        btn_passenger3 = (Button) findViewById(R.id.passenger3);
        btn_passenger4 = (Button) findViewById(R.id.passenger4);

        checkButton();
        //init sendMsg
        sendMsg = SendMsgFormator();

        if (AC_status == "" && Passengers == "") {
            tv_AC_model.setText("空调模式：未选择");
        } else {
            if (isCoolPressed) {
                cb_Cool.setChecked(true);
                tv_AC_model.setText("空调模式：制冷");
            }
            if (isHeatPressed) {
                cb_Heat.setChecked(true);
                tv_AC_model.setText("空调模式：制热");
            }
            if (!isCoolPressed && !isHeatPressed) {
                tv_AC_model.setText("空调模式：关闭");
            }
        }

        if (isStatus0) {
            btn_status0.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_AC_status.setText("空调档位：" + AC_status + "档");
        }
        if (isStatus1) {
            btn_status1.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_AC_status.setText("空调档位：" + AC_status + "档");
        }
        if (isStatus2) {
            btn_status2.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_AC_status.setText("空调档位：" + AC_status + "档");
        }
        if (isStatus3) {
            btn_status3.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_AC_status.setText("空调档位：" + AC_status + "档");
        }
        if (isStatus_auto) {
            btn_status_auto.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_AC_status.setText("空调档位：" + "自动档");
        }
        if (isPassenger0) {
            btn_passenger0.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_Passenger.setText("乘客人数：" + Passengers + "人");
        }
        if (isPassenger1) {
            btn_passenger1.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_Passenger.setText("乘客人数：" + Passengers + "人");
        }
        if (isPassenger2) {
            btn_passenger2.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_Passenger.setText("乘客人数：" + Passengers + "人");
        }
        if (isPassenger3) {
            btn_passenger3.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_Passenger.setText("乘客人数：" + Passengers + "人");
        }
        if (isPassenger4) {
            btn_passenger4.setBackground(getResources().getDrawable(R.drawable.shape_red));
            tv_Passenger.setText("乘客人数：" + Passengers + "人");
        }

        if (isNotification) {
            if (PushService.timer != null) {
                String AC = null;
                if (isCoolPressed) {
                    AC = "制冷";
                } else if (isHeatPressed) {
                    AC = "制热";
                } else {
                    AC = "关闭";
                }

                String status = null;
                switch (AC_status) {
                    case "0":
                        status = "0";
                        break;
                    case "1":
                        status = "1";
                        break;
                    case "2":
                        status = "2";
                        break;
                    case "3":
                        status = "3";
                        break;
                    case "A":
                        status = "自动";
                        break;
                    default:
                        status = "";
                        break;
                }

                PushService.addNotification(delaytime, "中国工况提醒您！", "中国工况信息采集APP", "请您输入车辆信息！当前状态：" + AC + ", " + status + "档" + ", " + Passengers + "人");
            } else {
                NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                        getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
                UploadActivity.getsContext().stopService(intent);

                String AC = null;
                if (isCoolPressed) {
                    AC = "制冷";
                } else if (isHeatPressed) {
                    AC = "制热";
                } else {
                    AC = "关闭";
                }

                String status = null;
                switch (AC_status) {
                    case "0":
                        status = "0";
                        break;
                    case "1":
                        status = "1";
                        break;
                    case "2":
                        status = "2";
                        break;
                    case "3":
                        status = "3";
                        break;
                    case "A":
                        status = "自动";
                        break;
                    default:
                        status = "";
                        break;
                }

                PushService.addNotification(delaytime, "中国工况提醒您！", "中国工况信息采集APP", "请您输入车辆信息！当前状态：" + AC + ", " + status + "档" + ", " + Passengers + "人");
            }
        } else {
            NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                    getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
            UploadActivity.getsContext().stopService(intent);
        }

        cb_Cool.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    isCoolPressed = true;
                    isHeatPressed = false;
                    cb_Heat.setChecked(false);
                    sendMsg = SendMsgFormator();
                    tv_AC_model.setText("空调模式：制冷");
                } else {
                    sendMsg = SendMsgFormator();
                    isCoolPressed = false;
                }
                if (!isCoolPressed && !isHeatPressed) {
                    sendMsg = SendMsgFormator();
                    tv_AC_model.setText("空调模式：关闭");
                }

            }
        });
        cb_Heat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    isHeatPressed = true;
                    isCoolPressed = false;
                    cb_Cool.setChecked(false);
                    sendMsg = SendMsgFormator();
                    tv_AC_model.setText("空调模式：制热");
                } else {
                    sendMsg = SendMsgFormator();
                    isHeatPressed = false;
                }
                if (!isCoolPressed && !isHeatPressed) {
                    sendMsg = SendMsgFormator();
                    tv_AC_model.setText("空调模式：关闭");
                }
            }
        });

        btn_status0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_AC_status.setText("空调档位：" + "0" + "档");
                AC_status = "0";
                isStatus0 = true;
                isStatus1 = false;
                isStatus2 = false;
                isStatus3 = false;
                isStatus_auto = false;

                btn_status0.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_status1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status3.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status_auto.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                for(int i = 0; i < 3; i++){
                    refreshData(SendMsgFormator(),sendPort,sendIPAddress);
                }
            }
        });

        btn_status1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_AC_status.setText("空调档位：" + "1" + "档");
                AC_status = "1";
                isStatus1 = true;
                isStatus0 = false;
                isStatus2 = false;
                isStatus3 = false;
                isStatus_auto = false;

                btn_status1.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_status0.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status3.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status_auto.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();

                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });
        btn_status2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_AC_status.setText("空调档位：" + "2" + "档");
                AC_status = "2";
                isStatus2 = true;
                isStatus1 = false;
                isStatus0 = false;
                isStatus3 = false;
                isStatus_auto = false;

                btn_status2.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_status1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status0.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status3.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status_auto.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();

                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });
        btn_status3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_AC_status.setText("空调档位：" + "3" + "档");
                AC_status = "3";
                isStatus3 = true;
                isStatus1 = false;
                isStatus2 = false;
                isStatus0 = false;
                isStatus_auto = false;

                btn_status3.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_status1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status0.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status_auto.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();

                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });
        btn_status_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_AC_status.setText("空调档位：" + "自动" + "档");
                AC_status = "A";
                isStatus_auto = true;
                isStatus0 = false;
                isStatus1 = false;
                isStatus2 = false;
                isStatus3 = false;

                btn_status_auto.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_status0.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_status3.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();

                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });

        btn_passenger0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_Passenger.setText("乘客人数： " + "0" + "人");
                Passengers = "0";
                isPassenger0 = true;
                isPassenger1 = false;
                isPassenger2 = false;
                isPassenger3 = false;
                isPassenger4 = false;

                btn_passenger0.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_passenger1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger3.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger4.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();

                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });
        btn_passenger1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_Passenger.setText("乘客人数： " + "1" + "人");
                Passengers = "1";
                isPassenger1 = true;
                isPassenger0 = false;
                isPassenger2 = false;
                isPassenger3 = false;
                isPassenger4 = false;

                btn_passenger1.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_passenger0.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger3.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger4.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();

                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });
        btn_passenger2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_Passenger.setText("乘客人数： " + "2" + "人");
                Passengers = "2";
                isPassenger2 = true;
                isPassenger1 = false;
                isPassenger0 = false;
                isPassenger3 = false;
                isPassenger4 = false;

                btn_passenger2.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_passenger1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger0.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger3.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger4.setBackground(getResources().getDrawable(R.drawable.shape_gray));


                sendMsg = SendMsgFormator();
                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });
        btn_passenger3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_Passenger.setText("乘客人数： " + "3" + "人");
                Passengers = "3";
                isPassenger3 = true;
                isPassenger1 = false;
                isPassenger2 = false;
                isPassenger0 = false;
                isPassenger4 = false;

                btn_passenger3.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_passenger1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger0.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger4.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();
                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });
        btn_passenger4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_Passenger.setText("乘客人数： " + "4" + "人");
                Passengers = "4";
                isPassenger4 = true;
                isPassenger1 = false;
                isPassenger2 = false;
                isPassenger3 = false;
                isPassenger0 = false;

                btn_passenger4.setBackground(getResources().getDrawable(R.drawable.shape_red));
                btn_passenger1.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger2.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger3.setBackground(getResources().getDrawable(R.drawable.shape_gray));
                btn_passenger0.setBackground(getResources().getDrawable(R.drawable.shape_gray));

                sendMsg = SendMsgFormator();
                for(int i = 0; i < 3; i++){
                    refreshData(sendMsg,sendPort,sendIPAddress);
                }
            }
        });

        initLocationManager();

        //Location Data Fusion Test

        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        phoneTime = formatter.format(curDate);
        System.out.println("phonetime: " + phoneTime);
        final String dateTime = phoneTime.substring(0, 10);
        System.out.println("dataTime: " + dateTime);
        System.out.println("name: " + name);

        btn_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNotification) {
                    NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                            getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
                    UploadActivity.getsContext().stopService(intent);

                    String AC = null;
                    if (isCoolPressed) {
                        AC = "制冷";
                    } else if (isHeatPressed) {
                        AC = "制热";
                    } else {
                        AC = "关闭";
                    }

                    String status = null;
                    switch (AC_status) {
                        case "0":
                            status = "0";
                            break;
                        case "1":
                            status = "1";
                            break;
                        case "2":
                            status = "2";
                            break;
                        case "3":
                            status = "3";
                            break;
                        case "A":
                            status = "自动";
                            break;
                        default:
                            status = "";
                            break;
                    }
                    PushService.addNotification(delaytime, "中国工况提醒您！", "中国工况信息采集APP", "请您录入车辆信息！当前状态：" + AC + ", " + status + "档" + ", " + Passengers + "人");
                } else {
                    NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                            getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
                    UploadActivity.getsContext().stopService(intent);
                }

                if (!isGrantExternalRW(UploadActivity.this)) {
                    Toast.makeText(UploadActivity.this, "未授权，录入失败！", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isNetworkConnected()) {
                    Toast.makeText(UploadActivity.this, "未打开网络，录入失败！", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(UploadActivity.this)
                            .setTitle("请打开数据网络或Wi-Fi")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // TODO Auto-generated method stub
                                        }
                                    })
                            .show();
                    return;
                }
                if (AC_status == "") {
                    Toast.makeText(UploadActivity.this, "请选择空调档位", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Passengers == "") {
                    Toast.makeText(UploadActivity.this, "请选择乘客人数", Toast.LENGTH_SHORT).show();
                    return;
                }
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "carInfo" + "/";
                fileName = name + "_" + plate + "_" + model + "_" + dateTime + ".txt";
                try {
                    //fileName = String.encode(fileName,"UTF-8");
                    fileName = new String(fileName.getBytes("UTF-8"), "UTF-8");
                    System.out.println("file name:" + fileName);
                } catch (UnsupportedEncodingException e) {

                    e.printStackTrace();
                }
                //GPS initiation
                String latitudeS = String.valueOf(latitude);
                String longitudeS = String.valueOf(longitude);
                String speedS = String.valueOf(speed);
                String UTCtimeS = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));

                if (latitudeS == "0.0" && longitudeS == "0.0") {
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    phoneTime = formatter.format(curDate);
                }

                String ACS = null;
                if (isCoolPressed) {
                    ACS = "1";
                } else if (isHeatPressed) {
                    ACS = "2";
                } else {
                    ACS = "0";
                }

                String dataS = ACS + "," + AC_status + "," + Passengers;

                if (!checkFileString(dataS, filePath, fileName)) {
                    if (writeTxtToFile(phoneTime, UTCtimeS, isCoolPressed, isHeatPressed, AC_status, Passengers,
                            longitudeS, latitudeS, speedS, filePath, fileName)) {
                        Toast.makeText(UploadActivity.this, "录入成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UploadActivity.this, "录入失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UploadActivity.this, "信息未修改", Toast.LENGTH_SHORT).show();
                }

                writeButton();
                //isLog = true;
                //return home
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);

            }

        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNotification) {
                    NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                            getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
                    UploadActivity.getsContext().stopService(intent);

                    String AC = null;
                    if (isCoolPressed) {
                        AC = "制冷";
                    } else if (isHeatPressed) {
                        AC = "制热";
                    } else {
                        AC = "关闭";
                    }

                    String status = null;
                    switch (AC_status) {
                        case "0":
                            status = "0";
                            break;
                        case "1":
                            status = "1";
                            break;
                        case "2":
                            status = "2";
                            break;
                        case "3":
                            status = "3";
                            break;
                        case "A":
                            status = "自动";
                            break;
                        default:
                            status = "";
                            break;
                    }
                    PushService.addNotification(delaytime, "中国工况提醒您！", "中国工况信息采集APP", "请您录入车辆信息！当前状态：" + AC + ", " + status + "档" + ", " + Passengers + "人");
                } else {
                    NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                            getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
                    UploadActivity.getsContext().stopService(intent);
                }
                if (!isGrantExternalRW(UploadActivity.this)) {
                    Toast.makeText(UploadActivity.this, "未授权，上传失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isNetworkConnected()) {
                    Toast.makeText(UploadActivity.this, "网络未打开，上传失败", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(UploadActivity.this)
                            .setTitle("请打开数据网络或Wi-Fi")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // TODO Auto-generated method stub
                                        }
                                    })
                            .show();
                    return;
                }
                if (AC_status == "") {
                    Toast.makeText(UploadActivity.this, "请选择空调档位", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Passengers == "") {
                    Toast.makeText(UploadActivity.this, "请选择乘客人数", Toast.LENGTH_SHORT).show();
                    return;
                }

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "carInfo" + "/";
                fileName = name + "_" + plate + "_" + model + "_" + dateTime + ".txt";
                try {
                    //fileName = String.encode(fileName,"UTF-8");
                    fileName = new String(fileName.getBytes("UTF-8"), "UTF-8");
                    System.out.println("file name:" + fileName);
                } catch (UnsupportedEncodingException e) {

                    e.printStackTrace();
                }
                //GPS initiation
                String latitudeS = String.valueOf(latitude);
                String longitudeS = String.valueOf(longitude);
                String speedS = String.valueOf(speed);
                String UTCtimeS = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                if (latitudeS == "0.0" && longitudeS == "0.0") {
                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    phoneTime = formatter.format(curDate);
                }
                //GPS
                if (writeTxtToFile(phoneTime, UTCtimeS, isCoolPressed, isHeatPressed, AC_status, Passengers,
                        longitudeS, latitudeS, speedS, filePath, fileName)) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            SendEmail.send("/" + "carInfo/" + fileName, dateTime, name);
                        }
                    }.start();

                    Toast.makeText(UploadActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
                    writeButton();
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    home.addCategory(Intent.CATEGORY_HOME);
                    startActivity(home);
                } else {
                    Toast.makeText(UploadActivity.this, "上传失败！", Toast.LENGTH_SHORT).show();
                    return;
                }
                writeButton();
                //Toast.makeText(UploadActivity.this, "录入成功！", Toast.LENGTH_SHORT).show();

            }
        });

        tv_re_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater factory = LayoutInflater.from(UploadActivity.this);
                View wifiEntryView = factory.inflate(R.layout.activity_wifilog,null);
                final EditText re_wifi_ipaddr = wifiEntryView.findViewById(R.id.re_editText_IP);
                final EditText re_wifi_port = wifiEntryView.findViewById(R.id.re_editText_Port);
                final RadioGroup re_wifi_delayRG = wifiEntryView.findViewById(R.id.re_radioWifiDelay);
                final Button re_wifi_enter = wifiEntryView.findViewById(R.id.btn_re_wifiEnter);

                final AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this).setView(wifiEntryView);
                builder.setCancelable(false);
                final AlertDialog dialog = builder.show();
                builder.create();

                re_wifi_ipaddr.setText(sendIPAddress);
                re_wifi_port.setText(String.valueOf(sendPort));

                if(sendDelay == 5*1000){
                    re_wifi_delayRG.check(R.id.re_fiveRB);
                }else if(sendDelay == 10*1000){
                    re_wifi_delayRG.check(R.id.re_tenRB);
                }else if(sendDelay == 30*1000){
                    re_wifi_delayRG.check(R.id.re_thirtyRB);
                }else if(sendDelay == 60*1000){
                    re_wifi_delayRG.check(R.id.re_sixtyRB);
                }

                re_wifi_delayRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                        int selectedId =re_wifi_delayRG.getCheckedRadioButtonId();
                        RadioButton delayRB = radioGroup.findViewById(i);

                        if(delayRB.getText().toString().equals("5秒")){
                            sendDelay = 5*1000;
                        }else if(delayRB.getText().toString().equals("10秒")){
                            sendDelay = 10*1000;
                        }else if(delayRB.getText().toString().equals("30秒")){
                            sendDelay = 30*1000;
                        }else if(delayRB.getText().toString().equals("60秒")){
                            sendDelay = 60*1000;
                        }
                    }
                });

                re_wifi_enter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendIPAddress = re_wifi_ipaddr.getText().toString();
                        sendPort = Integer.parseInt(re_wifi_port.getText().toString());

                        final Pattern IP_ADDRESS
                                = Pattern.compile(
                                "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                                        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                                        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                                        + "|[1-9][0-9]|[0-9]))");
                        Matcher matcher = IP_ADDRESS.matcher(sendIPAddress);

                        if(matcher.matches()&&sendDelay!=0&&sendPort!=0&&sendDelay>1000&&checkWifi().isWifiEnabled()){
                            writeData();// Modification
                            writeButton();
                            builder.setCancelable(false);
                            dialog.dismiss();
                        }else {
                            writeButton();
                            Toast.makeText(UploadActivity.this, R.string.alert, Toast.LENGTH_SHORT).show();
                        }
                    }

                });

            }
        });

        tv_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater factory = LayoutInflater.from(UploadActivity.this);
                View textEntryView = factory.inflate(R.layout.activity_re_registration, null);
                final EditText re_model = (EditText) textEntryView.findViewById(R.id.editText_model);
                final EditText re_name = (EditText) textEntryView.findViewById(R.id.editText_name);
                final EditText re_plate = (EditText) textEntryView.findViewById(R.id.editText_plate);
                final Button re_enter = (Button) textEntryView.findViewById(R.id.btn_enter);
                final CheckBox cb_Notification = (CheckBox) textEntryView.findViewById(R.id.checkbox_notification);

                // create a dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this)
                        .setView(textEntryView);
                builder.setCancelable(false);
                final AlertDialog dialog = builder.show();
                builder.create();

                re_name.setText(name);
                re_model.setText(model);
                re_plate.setText(plate);
                if (isNotification) {
                    cb_Notification.setChecked(true);
                } else {
                    cb_Notification.setChecked(false);
                }

                re_enter.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        name = re_name.getText().toString();
                        model = re_model.getText().toString();
                        plate = re_plate.getText().toString();
                        if ((name.length() >= 2) && (model.length() >= 2) && (plate.length() >= 2)) {
                            writeData();
                            writeButton();
                            builder.setCancelable(false);
                            dialog.dismiss();
                            System.out.println("Info: " + name + " " + model + " " + plate);
                            sendMsg = SendMsgFormator();
                        } else {
                            writeButton();
                            Toast.makeText(UploadActivity.this, R.string.alert, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                cb_Notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            isNotification = true;
                            cb_Notification.setChecked(true);
                            NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                                    getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();
                            Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
                            UploadActivity.getsContext().stopService(intent);

                            String AC = null;
                            if (isCoolPressed) {
                                AC = "制冷";
                            } else if (isHeatPressed) {
                                AC = "制热";
                            } else {
                                AC = "关闭";
                            }

                            String status = null;
                            switch (AC_status) {
                                case "0":
                                    status = "0";
                                    break;
                                case "1":
                                    status = "1";
                                    break;
                                case "2":
                                    status = "2";
                                    break;
                                case "3":
                                    status = "3";
                                    break;
                                case "A":
                                    status = "自动";
                                    break;
                                default:
                                    status = "";
                                    break;
                            }
                            PushService.addNotification(delaytime, "中国工况提醒您！", "中国工况信息采集APP", "请您录入车辆信息！当前状态：" + AC + ", " + status + "档" + ", " + Passengers + "人");
                        } else {
                            isNotification = false;
                            cb_Notification.setChecked(false);
                            NotificationManager notificationManager = (NotificationManager) UploadActivity.getsContext().
                                    getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();
                            Intent intent = new Intent(UploadActivity.getsContext(), PushService.class);
                            UploadActivity.getsContext().stopService(intent);

                        }
                        writeButton();
                    }
                });


            }
        });

        //Use a new thread to refresh the data regularly

        Thread backgroundRefreshThread = new Thread() {
            @Override
            public void run(){
                try{
                    while(sendFlag){
                        sleep(sendDelay);
                        sendMsg = SendMsgFormator();
                        refreshData(sendMsg,sendPort,sendIPAddress);
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };

        backgroundRefreshThread.start();


        //notification service
        /*isTogglePressed = checkToggleStatus();
        if(isTogglePressed){
            if(PushService.timer != null){
                System.out.println("2nd Exam: timer!= null");
                int delaytime = 5*1000;
                PushService.addNotification(delaytime,"中国工况提醒您！","中国工况信息采集APP","请您输入车辆信息！");
            }
        }else{
            NotificationManager notificationManager = (NotificationManager)UploadActivity.getsContext().
                    getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            Intent intent = new Intent(UploadActivity.getsContext(),PushService.class);
            UploadActivity.getsContext().stopService(intent);
        }
        tbtn_notification.setChecked(isTogglePressed);

        tbtn_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                tbtn_notification.setChecked(b);
                if(b){
                    isTogglePressed = true;
                    //init Push Service
                    //Notification
                    int delaytime = 5*1000;
                    PushService.addNotification(delaytime,"中国工况提醒您！","中国工况信息采集APP","请您输入车辆信息！");
                    writeStatus();

                }else{
                    //PushService.cleanAllNotification();
                    isTogglePressed = false;
                    NotificationManager notificationManager = (NotificationManager)UploadActivity.getsContext().
                            getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    Intent intent = new Intent(UploadActivity.getsContext(),PushService.class);
                    UploadActivity.getsContext().stopService(intent);
                    writeStatus();
                }
            }
        });*/


        /*btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start Location Listener
                initLocationManager();
            }
        });
        //Location Data Fusion Test
        btn_lbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start LBS Listener
                initLBSManager();
                //getLocation();

            }
        });*/

    }

    public String SendMsgFormator(){
        String header = "AHAU";
        //phone time
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String sysTime = formatter.format(curDate);
        //GPS info

        String GPStime = "NULL";
        String GPSSpeed = "NULL";
        String GPSLon = "NULL";
        String GPSLat = "NULL";
        String AC = "OFF";
        String ACLevel = "OFF";
        String p = "NULL";

        if(tv_GPS.getText().toString() != "" && !tv_GPS.getText().toString().equals("GPS 信息显示")){
            String GPSInfo = tv_GPS.getText().toString();
            String[] values = GPSInfo.split(";");
            GPStime = values[0];
            GPSSpeed = values[1];
            GPSLon = values[2];
            GPSLat = values[3];
        }

        if(isCoolPressed){
            AC = "COOL";
        }else if(isHeatPressed){
            AC ="HEAT";
        }

        if(isStatus0){
            ACLevel = "0";
        }else if(isStatus1){
            ACLevel = "1";
        }else if(isStatus2){
            ACLevel = "2";
        }else if(isStatus3){
            ACLevel = "3";
        }else if(isStatus_auto){
            ACLevel = "AUTO";
        }

        if(isPassenger0){
            p = "0";
        }else if(isPassenger1){
            p = "1";
        }else if(isPassenger2){
            p = "2";
        }else if(isPassenger3){
            p = "3";
        }else if(isPassenger4){
            p = "4";
        }

        String msg = header+";"+sysTime+";"+GPStime+";"+AC+";"+ACLevel+";"+p+";"+GPSLon+";"+GPSLat+";"+GPSSpeed+";"+"Check"+";"+"\r\n";

        return msg;

    }


    public void checkButton() {
        SharedPreferences sharedPreferences = getSharedPreferences("ButtonInfo", Activity.MODE_PRIVATE);
        AC_status = sharedPreferences.getString("AC_status", "");
        Passengers = sharedPreferences.getString("Passengers", "");
        isCoolPressed = sharedPreferences.getBoolean("isCoolPressed", false);
        isHeatPressed = sharedPreferences.getBoolean("isHeatPressed", false);
        isNotification = sharedPreferences.getBoolean("isNotification", isNotification);

        isStatus0 = sharedPreferences.getBoolean("isStatus0", false);
        isStatus1 = sharedPreferences.getBoolean("isStatus1", false);
        isStatus2 = sharedPreferences.getBoolean("isStatus2", false);
        isStatus3 = sharedPreferences.getBoolean("isStatus3", false);
        isStatus_auto = sharedPreferences.getBoolean("isStatus_auto", false);

        isPassenger0 = sharedPreferences.getBoolean("isPassenger0", false);
        isPassenger1 = sharedPreferences.getBoolean("isPassenger1", false);
        isPassenger2 = sharedPreferences.getBoolean("isPassenger2", false);
        isPassenger3 = sharedPreferences.getBoolean("isPassenger3", false);
        isPassenger4 = sharedPreferences.getBoolean("isPassenger4", false);

        System.out.println("check data method");
    }

    public void writeButton() {
        SharedPreferences mySharedPreferences = getSharedPreferences("ButtonInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_model", model);
        editor.putString("user_plate", plate);
        //Need Test
//        editor.putString("wifi_IpAddress",sendIPAddress);
//        editor.putInt("wifi_Port",sendPort);
//        editor.putInt("wifi_Delay",sendDelay);
        editor.putBoolean("isNotification", isNotification);

        editor.putBoolean("isCoolPressed", isCoolPressed);
        editor.putBoolean("isHeatPressed", isHeatPressed);
        editor.putString("AC_status", AC_status);
        editor.putString("Passengers", Passengers);

        editor.putBoolean("isStatus0", isStatus0);
        editor.putBoolean("isStatus1", isStatus1);
        editor.putBoolean("isStatus2", isStatus2);
        editor.putBoolean("isStatus3", isStatus3);
        editor.putBoolean("isStatus_auto", isStatus_auto);

        editor.putBoolean("isPassenger0", isPassenger0);
        editor.putBoolean("isPassenger1", isPassenger1);
        editor.putBoolean("isPassenger2", isPassenger2);
        editor.putBoolean("isPassenger3", isPassenger3);
        editor.putBoolean("isPassenger4", isPassenger4);

        System.out.println("write data method");
        editor.commit();
    }

    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
        return true;
    }


    // May need some reviews
    private void initLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(UploadActivity.this, "位置信息未授权！", Toast.LENGTH_SHORT).show();

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        //Permission Check
                        if (ActivityCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.
                                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                                checkSelfPermission(UploadActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        time = location.getTime();
                        speed = location.getSpeed();

                        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
                        Date curDate = new Date(System.currentTimeMillis());
                        phoneTime = formatter.format(curDate);

                        String UTCtime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                        if (String.valueOf(latitude) == "0.0" && String.valueOf(longitude) == "0.0") {
                            sendMsg = SendMsgFormator();
                            tv_GPS.setText("");
                        } else {
                            sendMsg = SendMsgFormator();
                            tv_GPS.setText(UTCtime + "; " + speed + "; " + "\r\n" + String.valueOf(latitude) + "; " + String.valueOf(longitude));
                        }
                        System.out.println(String.valueOf(latitude) + "; " +
                                String.valueOf(longitude) + "; " + String.valueOf(time) + "; " + String.valueOf(speed));
                        //locationManager.removeUpdates(this);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        //Toast.makeText(UploadActivity.this, "GPS provide status changed", Toast.LENGTH_SHORT).show();
                        //Location location = locationManager.getLastKnownLocation(provider);
                        switch (status) {

                            //GPS状态为可见时
                            case LocationProvider.AVAILABLE:
                                String UTCtime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                                if (String.valueOf(latitude) == "0.0" && String.valueOf(longitude) == "0.0") {
                                    sendMsg = SendMsgFormator();
                                    tv_GPS.setText("");
                                } else {
                                    sendMsg = SendMsgFormator();
                                    tv_GPS.setText(UTCtime + "; " + speed + "; " + "\r\n" + String.valueOf(latitude) + "; " + String.valueOf(longitude));
                                }
                                break;
                            //GPS状态为服务区外时
                            case LocationProvider.OUT_OF_SERVICE:
                                //String UTCtime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                                if (String.valueOf(latitude) == "0.0" && String.valueOf(longitude) == "0.0") {
                                    sendMsg = SendMsgFormator();
                                    tv_GPS.setText("");
                                }
                                break;
                            //GPS状态为暂停服务时
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                //String UTCtime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                                if (String.valueOf(latitude) == "0.0" && String.valueOf(longitude) == "0.0") {
                                    sendMsg = SendMsgFormator();
                                    tv_GPS.setText("");
                                }
                                break;
                        }
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        //Toast.makeText(UploadActivity.this, "GPS provide is enabled", Toast.LENGTH_SHORT).show();
                        isGPSEnable = true;
                        String UTCtime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
                        if (String.valueOf(latitude) == "0.0" && String.valueOf(longitude) == "0.0") {
                            sendMsg = SendMsgFormator();
                            tv_GPS.setText("");
                        } else {
                            sendMsg = SendMsgFormator();
                            tv_GPS.setText(UTCtime + "; " + speed + "; " + "\r\n" + String.valueOf(latitude) + "; " + String.valueOf(longitude));
                        }

                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        //Toast.makeText(UploadActivity.this, "GPS provide is disabled", Toast.LENGTH_SHORT).show();
                        isGPSEnable = false;
//                        initLBSManager();
                    }
                }
        );
    }

    public Boolean checkFileString(String data, String filePath, String fileName){
        String fileString = readFile(filePath, fileName);
        if (fileString==null){
            return false;
        }
        else if (fileString.contains(data)){
            return true;
        }
        return false;
    }

    public String readFile(String filePath,String fileName) {
        File file = new File(filePath + fileName);
        String content = ""; //文件内容字符串
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            Log.d("TestFile", "The File doesn't not exist.");
            return null;
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                    }
                    instream.close();
                    return content;
                }
                return null;
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return null;
    }
    public Boolean writeTxtToFile(String phoneTime,String time,boolean isCoolPressed, boolean isHeatPressed,
                                  String AC_status,String passengers,String longitude,String latitude,String speed,
                                  String filePath, String fileName){
        makeFilePath(filePath,fileName);
        String strFilePath = filePath+fileName;
        String AC = null;
        if(isCoolPressed){
            AC = "1";
        }else if(isHeatPressed) {
            AC = "2";
        }else{
            AC = "0";
        }

        String strContent = phoneTime+","+time+","+AC+","+AC_status+","+passengers+","+longitude+","+latitude+","+speed+"\r\n";
        if(longitude=="0.0"&&latitude=="0.0"){
            strContent = phoneTime+","+AC+","+AC_status+","+passengers+"\r\n";
        }
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    Log.i("error:", e + "");
                    return false;
                }

            }
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(file.length());
                randomAccessFile.write(strContent.getBytes());
                randomAccessFile.close();
                return true;
            } catch (Exception e) {
                Log.i("error:", e + "");
                return false;
            }
        }catch(Exception e){
            //Toast.makeText(UploadActivity.this, "录入失败！", Toast.LENGTH_SHORT).show();
            Log.e("TestFile", "Error on write File:" + e);
            return false;
        }

    }
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("请点击录入")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub

                                }
                            })
                    .show();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
    public void writeData(){
        SharedPreferences  mySharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString("user_name",name);
        editor.putString("user_model",model);
        editor.putString("user_plate",plate);
        editor.putString("wifi_IpAddress",sendIPAddress);
        editor.putInt("wifi_Port",sendPort);
        editor.putInt("wifi_Delay",sendDelay);
        System.out.println("write data method");
        editor.commit();
    }

    //是否有可用网络
    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network != null) {
            return network.isAvailable();
        }
        return false;
    }

    //Wifi是否可用
    private WifiManager checkWifi() {
        WifiManager wifiManager = (WifiManager) sContext
                .getSystemService(Context.WIFI_SERVICE);
        return wifiManager;
    }

    public void refreshData(String msg, int port, String IPAddr){
        udpHelper.send(msg,port,IPAddr);
    }

    //Gps是否可用
    private boolean isGpsEnable() {
        LocationManager locationManager =
                ((LocationManager) sContext.getSystemService(Context.LOCATION_SERVICE));
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /* @author suncat
     * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * @return
     */
    public static final boolean ping() {

        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }

    @Override
    public void onResume(){
        if(wifiReceiver == null){
            wifiReceiver = new WifiReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiReceiver, filter);

        super.onResume();
    }


    @Override
    public void onPause(){
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }


    @Override
    public void onDestroy(){

        super.onDestroy();
    }

}
