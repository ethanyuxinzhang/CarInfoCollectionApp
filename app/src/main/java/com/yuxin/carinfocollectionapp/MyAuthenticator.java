package com.yuxin.carinfocollectionapp;

import javax.mail.*;

/**
 * Created by Yuxin Zhang on 21/4/19
 * Monash University,
 * yuxin.zhang@monash.edu.
 */
public class MyAuthenticator extends Authenticator {

    String userNamer = null;
    String password = null;
    public MyAuthenticator(String userNamer, String password){
        this.userNamer = userNamer;
        this.password = password;
    }
    protected PasswordAuthentication getPasswordAuthentication(){
        //Not 1111
        return new PasswordAuthentication(userNamer,password);
    }

}
