package com.yuxin.carinfocollectionapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    //Define Variables For User Infos
    static String nameS = null;
    static String modelS = null;
    static String plateS = null;
    static String IpAddrS = null;
    static int portS = 0;
    static int delayS = 0;
    //Request All permission at once
    private static final int PERMISSION_CODE = 999;
    static boolean isPermissionGranted = false;
    static boolean isNotificationS = true;

    private static Context sContext = null;

    public static Context getsContext() {
        return sContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ExitApplication.getInstance().addActivity(this);
        final EditText IpAddr = findViewById(R.id.editText_IP);
        final EditText port = findViewById(R.id.editText_Port);
        final RadioGroup delayRG = findViewById(R.id.radioWifiDelay);
        final EditText name = (EditText)findViewById(R.id.editText_name);
        final EditText model = (EditText)findViewById(R.id.editText_model);
        final EditText plate = (EditText)findViewById(R.id.editText_plate);
        final Button enter = (Button)findViewById(R.id.btn_enter);
        final CheckBox cb_notification = (CheckBox)findViewById(R.id.checkbox_notification);
        //invoke check data method to see whether app has previous log msgs
        checkData();
        //check whether app granted all needed permissions already
        checkPermissions();

        //EditText Info
        if(nameS!=""&&modelS!=""&&plateS!=""&&IpAddrS!=""&&portS!=0&&delayS!=0&&portS>1000){
            name.setText(nameS);
            model.setText(modelS);
            plate.setText(plateS);
            IpAddr.setText(IpAddrS);
            port.setText(String.valueOf(portS));

            if(delayS == 5*1000){
                delayRG.check(R.id.fiveRB);
            }else if(delayS == 10*1000){
                delayRG.check(R.id.tenRB);
            }else if(delayS == 30*1000){
                delayRG.check(R.id.thirtyRB);
            }else if(delayS == 60*1000){
                delayRG.check(R.id.sixtyRB);
            }

            System.out.println("123: "+nameS);
            //volume.setText(volumeS);
            Intent intent = new Intent();
            intent.setClass(RegistrationActivity.this,com.yuxin.carinfocollectionapp.UploadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name",nameS);
            bundle.putString("model",modelS);
            bundle.putString("plate",plateS);
            bundle.putString("IpAddr",IpAddrS);
            bundle.putInt("port", portS);
            bundle.putInt("delay",delayS);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        cb_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //isModify=true;
                if(isChecked){
                    isNotificationS = true;
                }
                else{
                    isNotificationS = false;
                }

            }
        });

        //Send all parameters to next activity And Start Notification Service
        enter.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                nameS = name.getText().toString();
                modelS = model.getText().toString();
                plateS = plate.getText().toString();
                IpAddrS = IpAddr.getText().toString();
                portS = Integer.parseInt(port.getText().toString());
                int selectedId =delayRG.getCheckedRadioButtonId();
                RadioButton delayRB = findViewById(selectedId);

                if(delayRB.getText().toString().equals("5秒")){
                      delayS = 5*1000;
                }else if(delayRB.getText().toString().equals("10秒")){
                    delayS = 10*1000;
                }else if(delayRB.getText().toString().equals("30秒")){
                    delayS = 30*1000;
                }else if(delayRB.getText().toString().equals("60秒")){
                    delayS = 60*1000;
                }

                final Pattern IP_ADDRESS
                        = Pattern.compile(
                        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                                + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                                + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                                + "|[1-9][0-9]|[0-9]))");
                Matcher matcher = IP_ADDRESS.matcher(IpAddrS);

                boolean ipValidation = matcher.matches();

                //volumeS = volume.getText().toString();
                System.out.println("Info: "+nameS+" "+modelS+" "+plateS);
                if((nameS.length()>=2)&&(modelS.length()>=2)&&(plateS.length()>=2)&&ipValidation&&delayS!=0&&portS!=0&&portS>1000){
                    //Store Data in SharedPreference
                    writeData();
                    System.out.println("Data Written !");
                    Intent intent = new Intent();
                    intent.setClass(RegistrationActivity.this,com.yuxin.
                            carinfocollectionapp.UploadActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("name",nameS);
                    bundle.putString("model",modelS);
                    bundle.putString("plate",plateS);
                    bundle.putString("IpAddr",IpAddrS);
                    bundle.putInt("port", portS);
                    bundle.putInt("delay",delayS);
                    bundle.putBoolean("notification",isNotificationS);
                    // bundle.putString("volume",volumeS);
                    intent.putExtras(bundle);
                    System.out.println("Before Jump !");
                    if(isPermissionGranted){
                        startActivity(intent);
                    }else{
                        Toast.makeText(RegistrationActivity.this,"Grant Permissions First !",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegistrationActivity.this,R.string.alert,Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Permissions Grant Procedures
    public void checkPermissions(){
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if(!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            permissionsNeeded.add("Write Storage");
        }
        if(!addPermission(permissionsList,Manifest.permission.READ_EXTERNAL_STORAGE)){
            permissionsNeeded.add("Read Storage");
        }
        if(!addPermission(permissionsList,Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionsNeeded.add("GPS");
        }
        if(!addPermission(permissionsList,Manifest.permission.ACCESS_COARSE_LOCATION)){
            permissionsNeeded.add("LBS");
        }
        if(!addPermission(permissionsList,Manifest.permission.INTERNET)){
            permissionsNeeded.add("Internet");
        }
        if(permissionsList.size()>0 && Build.VERSION.SDK_INT>=23){
            if(permissionsNeeded.size()>0){
                //Need Rationale
                String msg = "You need to grant access to "+permissionsNeeded.get(0);
                for(int i=1; i<permissionsNeeded.size();i++){
                    msg = msg+","+permissionsNeeded.get(i);
                }
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),PERMISSION_CODE);
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),PERMISSION_CODE);
            return;
        }
        isPermissionGranted = true;
    }
    private boolean addPermission(List<String> permissionsList, String permission){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(permission)!= PackageManager.PERMISSION_GRANTED){
            permissionsList.add(permission);
            //Check for Rationale Option
            if(!shouldShowRequestPermissionRationale(permission)){
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case PERMISSION_CODE:
            {
                Map<String,Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION,PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION,PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE,PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE,PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.INTERNET,PackageManager.PERMISSION_GRANTED);
                //Fill with Results
                for(int i=0;i<permissions.length;i++){
                    perms.put(permissions[i],grantResults[i]);
                }
                //check access
                if(perms.get(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.INTERNET)==PackageManager.PERMISSION_GRANTED){
                    System.out.println("All Permissions Granted !");
                    isPermissionGranted = true;
                }else{
                    //Permission Denied
                    isPermissionGranted = false;
                    Toast.makeText(RegistrationActivity.this,"Some Permission is Denied",Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    public void writeData(){
        SharedPreferences mySharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString("user_name",nameS);
        editor.putString("user_model",modelS);
        editor.putString("user_plate",plateS);
        editor.putBoolean("user_notification",isNotificationS);
        editor.putString("wifi_IpAddress",IpAddrS);
        editor.putInt("wifi_Port",portS);
        editor.putInt("wifi_Delay",delayS);
        System.out.println("write data method");
        editor.commit();
    }

    public void checkData(){
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo",Activity.MODE_PRIVATE);
        nameS = sharedPreferences.getString("user_name","");
        modelS = sharedPreferences.getString("user_model","");
        plateS = sharedPreferences.getString("user_plate","");
        IpAddrS = sharedPreferences.getString("wifi_IpAddress", "");
        portS = sharedPreferences.getInt("wifi_Port", 0);
        delayS = sharedPreferences.getInt("wifi_Delay",0);
        isNotificationS = sharedPreferences.getBoolean("user_notification",true);

        System.out.println("check data method");

    }


}
