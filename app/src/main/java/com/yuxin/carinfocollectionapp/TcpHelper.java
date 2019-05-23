package com.yuxin.carinfocollectionapp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Yuxin Zhang on 23/5/19
 * Monash University,
 * yuxin.zhang@monash.edu.
 */
public class TcpHelper {
    private Socket socket = null;

    public  TcpHelper (String host, int port) throws IOException {
        socket = new Socket(host, port);
    }

    public void sendMsg(String msg, String hostS, int portS) throws IOException {

        if(socket.getInetAddress().equals(hostS) && socket.getPort()==portS){
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(msg.replace("\n", "") + "\n");
            writer.flush();
        }else{
            socket.close();
            socket = new Socket(hostS, portS);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            writer.write(msg.replace("\n", "") + "\n");
            System.out.println("TCP Data:" + msg);
            writer.flush();
        }


    }

}
