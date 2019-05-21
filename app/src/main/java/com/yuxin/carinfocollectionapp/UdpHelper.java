package com.yuxin.carinfocollectionapp;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Yuxin Zhang on 21/4/19
 * Monash University,
 * yuxin.zhang@monash.edu.
 */
public class UdpHelper {
    public boolean IsThreadDisable = false;
    private static WifiManager.MulticastLock lock;
    InetAddress mInetAddr;

    public UdpHelper(WifiManager manager) {
        this.lock = manager.createMulticastLock("UDPwifi");
    }

    public static void send(String msg, int port, String ipName){
        msg = (msg == null ? "No Data Change !":msg);
        int server_port = port;
        Log.d("UDP","UDP Data: "+ msg);
        DatagramSocket s = null;

        try{
            s = new DatagramSocket();
        }catch (SocketException e){
            e.printStackTrace();
        }

        InetAddress local = null;

        try{
            local = InetAddress.getByName(ipName); // need review
        }catch (UnknownHostException e){
            e.printStackTrace();
        }

        int msg_length = msg.length();

        byte[] msgByte = msg.getBytes();

        DatagramPacket p = new DatagramPacket(msgByte, msg_length, local, server_port);

        try{
            s.send(p);
            s.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
