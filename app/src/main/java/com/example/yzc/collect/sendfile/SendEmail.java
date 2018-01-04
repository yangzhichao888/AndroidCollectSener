package com.example.yzc.collect.sendfile;

import android.os.AsyncTask;

import javax.mail.MessagingException;

/**
 * Created by yzc on 2017/11/4.
 */

public class SendEmail extends AsyncTask<String,Integer,String> {
            @Override
            protected void onPreExecute() {

                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... strings) {

                EmailSender sender = new EmailSender();
                //设置服务器地址和端口，网上搜的到
                sender.setProperties("smtp.163.com", "25");
                try {
                    //分别设置发件人，邮件标题和文本内容
                    sender.setMessage("yunfweikxn@163.com", strings[0], strings[1]);
                    //设置收件人
                    sender.setReceiver(new String[]{"yunfweikxn@163.com"});
                    //添加附件
                    //这个附件的路径是我手机里的啊，要发你得换成你手机里正确的路径
//                          sender.addAttachment("/sdcard/DCIM/Camera/asd.jpg");
                    //发送邮件
                    sender.sendEmail("smtp.163.com", "yunfweikxn", "qwer1234");
                    //<span style="font-family: Arial, Helvetica, sans-serif;">sender.setMessage("你的163邮箱账号", "EmailSender", "Java Mail ！");这里面两个邮箱账号要一致</span>

                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return "";
            }

}

