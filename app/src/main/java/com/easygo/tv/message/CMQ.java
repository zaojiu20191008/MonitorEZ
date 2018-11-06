package com.easygo.tv.message;

import android.util.Log;

import com.google.gson.Gson;
import com.qcloud.cmq.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CMQ {

    public static final String TAG = "CMQ";

    //从腾讯云官网查询的云API密钥信息
    private final String secretId = "AKIDSkoeFcXeJjtqY1RExQdrpXy93h4AsZsu";
    private final String secretKey = "uVrxnNhlZR5TcWBbomVyB8pHTP5VfZDe";
    private final String endpoint = "http://cmq-queue-gz.api.qcloud.com";

    private final Queue queue;
    private final String queueName = "android-test";

    public final String MSG_OPEN_DOOR = "open_door";
    public final String MSG_RECORD = "recorde";

    public static final String RECEIVER_OPEN_DOOR = "RECEIVER_OPEN_DOOR";
    public static final String RECEIVER_RECORD = "RECEIVER_RECORD";

    private boolean repeatAccept = true;


    private CMQ() {
        Account account = new Account(endpoint, secretId, secretKey);

        //获得队列实例
        queue = account.getQueue(queueName);

        QueueMeta meta = new QueueMeta();
        meta.pollingWaitSeconds = 20;
        try {
            queue.setQueueAttributes(meta);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "CMQ: 设置队列属性失败！");
        }

    }

    public static CMQ getInstance() {
        return Holder.sInstance;
    }

    private static class Holder {
        static CMQ sInstance = new CMQ();
    }

    public void stop() {
        this.repeatAccept = false;
    }

    public void reset() {
        this.repeatAccept = true;
    }

    public interface OnMessageListener {
        void onAccept(String msg);
    }


    public void accept(OnMessageListener listener) {
        //批量接收消息
        ArrayList<String> vtReceiptHandle = new ArrayList<String>(); //保存服务器返回的消息句柄，用于删除消息

        Log.i(TAG, "accept: 开始接收消息");
        List<Message> msgList = null;
        try {
            msgList = queue.batchReceiveMessage(10, 10);
            Log.i(TAG, "accept: 接收消息数量： " + msgList.size());
            for (int i = 0; i < msgList.size(); i++) {
                Message msg = msgList.get(i);
//            System.out.println("msgId:" + msg.msgId);
            System.out.println("msgBody:" + msg.msgBody);
//            System.out.println("receiptHandle:" + msg.receiptHandle);
//            System.out.println("enqueueTime:" + msg.enqueueTime);
//            System.out.println("nextVisibleTime:" + msg.nextVisibleTime);
//            System.out.println("firstDequeueTime:" + msg.firstDequeueTime);
//            System.out.println("dequeueCount:" + msg.dequeueCount);

                if(listener != null) {
                    listener.onAccept(msg.msgBody);
                }

                vtReceiptHandle.add(msg.receiptHandle);
            }
            //批量删除消息
            Log.i(TAG, "accept: 删除消息中");
            queue.batchDeleteMessage(vtReceiptHandle);

            if(repeatAccept)
                accept(listener);
        }catch (CMQServerException e1) {
            Log.i(TAG, "Server Exception, " + e1.toString());
            if(repeatAccept)
                accept(listener);
        } catch (CMQClientException e2) {
            Log.i(TAG, "Client Exception, " + e2.toString());
            if(repeatAccept)
                accept(listener);
        } catch (Exception e) {
            Log.i(TAG, "error..." + e.toString());
            if(repeatAccept)
                accept(listener);
        }

    }

    public void send(ArrayList<String> vtMsgBody) {
        try {
            queue.batchSendMessage(vtMsgBody);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "sendMessage: 发送消息失败");
        }
    }


    public static void main(String[] args) {

        //从腾讯云官网查询的云API密钥信息
        String secretId = "AKIDSkoeFcXeJjtqY1RExQdrpXy93h4AsZsu";
        String secretKey = "uVrxnNhlZR5TcWBbomVyB8pHTP5VfZDe";
        String endpoint = "http://cmq-queue-gz.api.qcloud.com";

        try {
            Account account = new Account(endpoint, secretId, secretKey);

            //获得队列实例
            System.out.println("--------------- queue[android-test] ---------------");
            Queue queue = account.getQueue("android-test");

            //设置队列属性
            System.out.println("---------------set queue attributes ...---------------");
            QueueMeta meta1 = new QueueMeta();
            meta1.pollingWaitSeconds = 20;
            queue.setQueueAttributes(meta1);
            System.out.println("pollingWaitSeconds=20 set");

            //批量操作
            //批量发送消息
            System.out.println("---------------batch send message ...---------------");
            ArrayList<String> vtMsgBody = new ArrayList<String>();
            String msgBody = "hello world,this is cmq sdk for java 1";
//            vtMsgBody.add(msgBody);

            Test test = new Test();
//            test.action = Msg.ACTION_USER_START_PLAY;//开始直播
//            test.action = Msg.ACTION_USER_STOP_PLAY;//停止直播
//            test.action = Msg.ACTION_BP_START_RECORD;//盘点开始录制
            test.action = Msg.ACTION_BP_STOP_RECORD;//盘点结束录制
            test.user_id = 111;
            test.shop_id = 319;
            test.shop_name = "力迅上筑";

            msgBody = new Gson().toJson(test);
            vtMsgBody.add(msgBody);

            //播放16个
//            for (Map.Entry<Integer, String> entry : ShopMap.sShop.entrySet()) {
//
//                Test t = new Test();
//                t.action = Msg.ACTION_USER_START_PLAY;
//                t.shop_id = entry.getKey();
//
//                msgBody = new Gson().toJson(t);
//                vtMsgBody.add(msgBody);
//            }

//            for (String name : ShopMap.sName) {
////            for (int i=4; i < 8; i++) {
//
////                String name = ShopMap.sName[i];
////                String name = "东山雅筑商务中心";
//                Test t = new Test();
//                t.action = Msg.ACTION_USER_START_PLAY;
//                t.shop_name = name;
//
//                msgBody = new Gson().toJson(t);
//                vtMsgBody.add(msgBody);
//            }


            List<String> vtMsgId = queue.batchSendMessage(vtMsgBody);
            for (int i = 0; i < vtMsgBody.size(); i++)
                System.out.println("[" + vtMsgBody.get(i) + "] sent");
//            for (int i = 0; i < vtMsgId.size(); i++)
//                System.out.println("msgId:" + vtMsgId.get(i));


//            batchReceive(queue, false);
//            batchReceive(queue, true);

            //批量接收消息
//            ArrayList<String> vtReceiptHandle = new ArrayList<String>(); //保存服务器返回的消息句柄，用于删除消息
//            System.out.println("---------------batch recv message ...---------------");
//            List<Message> msgList = queue.batchReceiveMessage(10, 10);
//            System.out.println("recv msg count:" + msgList.size());
//            for (int i = 0; i < msgList.size(); i++) {
//                Message msg1 = msgList.get(i);
//                System.out.println("msgBody:" + msg1.msgBody);
//
//                vtReceiptHandle.add(msg1.receiptHandle);
//            }
            //批量删除消息
//            System.out.println("---------------batch delete message ...---------------");
//            queue.batchDeleteMessage(vtReceiptHandle);
//            for (int i = 0; i < vtReceiptHandle.size(); i++)
//                System.out.println("receiptHandle:" + vtReceiptHandle.get(i) + " deleted");

        } catch (CMQServerException e1) {
            System.out.println("Server Exception, " + e1.toString());
//            main(null);
        } catch (CMQClientException e2) {
            System.out.println("Client Exception, " + e2.toString());
        } catch (Exception e) {
            System.out.println("error..." + e.toString());
        }
    }


    public static void batchReceive(Queue queue, boolean needDelete) throws Exception {

            ArrayList<String> vtReceiptHandle = new ArrayList<>(); //保存服务器返回的消息句柄，用于删除消息
            System.out.println("---------------batch recv message ...---------------");
            List<Message> msgList = queue.batchReceiveMessage(10, 10);
            System.out.println("recv msg count:" + msgList.size());
            for (int i = 0; i < msgList.size(); i++) {
                Message msg1 = msgList.get(i);
                System.out.println("msgBody:" + msg1.msgBody);

                vtReceiptHandle.add(msg1.receiptHandle);
            }

            if(needDelete) {
                //批量删除消息
            System.out.println("---------------batch delete message ...---------------");
            queue.batchDeleteMessage(vtReceiptHandle);
            for (int i = 0; i < vtReceiptHandle.size(); i++)
                System.out.println("receiptHandle:" + vtReceiptHandle.get(i) + " deleted");
            }

    }
}
