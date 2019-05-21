package com.yuxin.carinfocollectionapp;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import static javax.mail.internet.MimeUtility.encodeText;

/**
 * Created by Yuxin Zhang on 21/4/19
 * Monash University,
 * yuxin.zhang@monash.edu.
 */
public class SendEmail {

    public static void send(String filenames,String dateTimeS,String nameS){
        MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost("smtp.163.com");
        mailInfo.setMailServerPort("25");
        mailInfo.setValidate(true);
        mailInfo.setUserName("du_kyseu@163.com");//发件人邮箱账号（必须是163邮箱）
        mailInfo.setPassword("dky2011106501");//发件人邮箱密码
        //设置发件人
        String nick = "";
        try {
            nick = javax.mail.internet.MimeUtility.encodeText(nameS);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mailInfo.setFromAddress(nick+"<du_kyseu@163.com>");//发件人昵称加邮箱

        /*
        设置主题
         */
        //开发者测试邮箱
        /*mailInfo.setToAddress("1053562781@qq.com");//收件人邮箱
        mailInfo.setSubject(dateTimeS.substring(0,4)+"年"+dateTimeS.substring(5,7)+"月"
                +dateTimeS.substring(8,10)+"日"+"-中国工况南京");//邮件主题*/

        /*mailInfo.setToAddress("hefei@ahau.edu.cn");//收件人邮箱
        mailInfo.setSubject(dateTimeS.substring(0,4)+"年"+dateTimeS.substring(5,7)+"月"
                +dateTimeS.substring(8,10)+"日"+"-中国工况合肥");//邮件主题*/

        /*mailInfo.setToAddress("changchun@ahau.edu.cn");//收件人邮箱
        mailInfo.setSubject(dateTimeS.substring(0,4)+"年"+dateTimeS.substring(5,7)+"月"
                +dateTimeS.substring(8,10)+"日"+"-中国工况长春");//邮件主题*/

        mailInfo.setToAddress("wwuhan@ahau.edu.cn");//收件人邮箱
        mailInfo.setSubject(dateTimeS.substring(0,4)+"年"+dateTimeS.substring(5,7)+"月"
                +dateTimeS.substring(8,10)+"日"+"-中国工况南昌");//邮件主题

        /*//mailInfo.setToAddress("1053562781@qq.com");//收件人邮箱
        //mailInfo.setToAddress("hefei@ahau.edu.cn");//收件人邮箱
        //mailInfo.setToAddress("changchun@ahau.edu.cn");//收件人邮箱
        mailInfo.setToAddress("wwuhan@ahau.edu.cn");//收件人邮箱
        mailInfo.setSubject("设置邮件主题");//邮件主题*/

        mailInfo.setContent("发送邮件测试");//邮件正文内容
        String attachFileNames= filenames;//附件文件名
        mailInfo.setAttachFileNames(filenames);

        //这个类主要来发送邮件
        SimpleMailSender sms = new SimpleMailSender();
        //sms.sendTextMail(mailInfo);//发送文体格式
        sms.sendHtmlMail(mailInfo);//发送html格式
        System.out.println("Email Sent !");
    }
}
