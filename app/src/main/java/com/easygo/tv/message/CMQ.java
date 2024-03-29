package com.easygo.tv.message;

import android.util.Log;

import com.easygo.monitor.BuildConfig;
import com.google.gson.Gson;
import com.qcloud.cmq.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CMQ {

    public static final String TAG = "CMQ";

    //从腾讯云官网查询的云API密钥信息
    private final String secretId = "AKIDSkoeFcXeJjtqY1RExQdrpXy93h4AsZsu";
    private final String secretKey = "uVrxnNhlZR5TcWBbomVyB8pHTP5VfZDe";
    private final String endpoint = "http://cmq-queue-gz.api.qcloud.com";

    private final Queue queue;
//    private final String queueName = "android-test";
    private final String queueName = BuildConfig.QUEUE_NAME;

    public final String MSG_OPEN_DOOR = "open_door";
    public final String MSG_RECORD = "recorde";

    public static final String RECEIVER_OPEN_DOOR = "RECEIVER_OPEN_DOOR";
    public static final String RECEIVER_RECORD = "RECEIVER_RECORD";

    private boolean repeatAccept = true;

    private final long TOLERATE = 5 * 60 * 1000;//毫秒


    private CMQ() {
        Account account = new Account(endpoint, secretId, secretKey);

        //获得队列实例
        queue = account.getQueue(queueName);

        QueueMeta meta = new QueueMeta();
        meta.pollingWaitSeconds = 20;
        meta.msgRetentionSeconds = 5 * 60;
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

    public boolean isRepeatAccept() {
        return this.repeatAccept;
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

            long currentTimeMillis = System.currentTimeMillis();
            for (int i = 0; i < msgList.size(); i++) {
                Message msg = msgList.get(i);
//            System.out.println("msgId:" + msg.msgId);
            System.out.println("msgBody:" + msg.msgBody);
//            System.out.println("receiptHandle:" + msg.receiptHandle);
//            System.out.println("enqueueTime:" + msg.enqueueTime);
//            System.out.println("nextVisibleTime:" + msg.nextVisibleTime);
//            System.out.println("firstDequeueTime:" + msg.firstDequeueTime);
//            System.out.println("dequeueCount:" + msg.dequeueCount);

//                long time = currentTimeMillis - msg.enqueueTime*1000;
//                Log.i(TAG, "accept: 消息接收与入队时间差值 time:" + time);

//                if(currentTimeMillis - msg.enqueueTime*1000 <= TOLERATE) {
                    if (listener != null) {
                        listener.onAccept(msg.msgBody);
                    }
//                }
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
//            System.out.println("--------------- queue[android-test] ---------------");
//            Queue queue = account.getQueue("android-test");
            System.out.println("--------------- queue[android-monitor] ---------------");
            Queue queue = account.getQueue("android-monitor");

            //设置队列属性
            System.out.println("---------------set queue attributes ...---------------");
            QueueMeta meta1 = new QueueMeta();
            meta1.pollingWaitSeconds = 20;
            meta1.msgRetentionSeconds = 300;
            queue.setQueueAttributes(meta1);
            System.out.println("pollingWaitSeconds=20 set");

            //批量操作
            //批量发送消息
            System.out.println("---------------batch send message ...---------------");
            ArrayList<String> vtMsgBody = new ArrayList<String>();
            String msgBody = "hello world,this is cmq sdk for java 1";
//            vtMsgBody.add(msgBody);

            Test test = new Test();
            test.action = Msg.ACTION_USER_START_PLAY;//开始直播
//            test.action = Msg.ACTION_USER_STOP_PLAY;//停止直播
//            test.action = Msg.ACTION_BP_START_RECORD;//盘点开始录制
//            test.action = Msg.ACTION_BP_STOP_RECORD;//盘点结束录制
//            test.action = Msg.ACTION_PAY_SUCCESS;//支付成功
//            test.action = Msg.ACTION_DUBIOUS;//可疑人员进店
//            test.action = Msg.ACTION_TEST;//测试
//            test.width = 640;//测试 宽度
//            test.height = 360;//测试 高度
            test.width = 960;//测试 宽度
            test.height = 540;//测试 高度
//            test.width = 1920;//测试 宽度
//            test.height = 1080;//测试 高度
            test.video_level = 1;
            test.user_id = 111;
            test.shop_id = 343;
            test.shop_name = "力迅上筑";
            test.nick_name = "xx小偷";
            test.pay_success_count = 5;
            test.count_s = 10;

            msgBody = new Gson().toJson(test);
            vtMsgBody.add(msgBody);

            //播放16个
            int x = 0;
//            int x = 4;
            for (Map.Entry<Integer, String> entry : ShopMap.sShop.entrySet()) {

                if(x <= 0)
                    break;
                x--;

                Test t = new Test();
                t.action = Msg.ACTION_USER_START_PLAY;
//                t.action = Msg.ACTION_USER_STOP_PLAY;
                t.shop_id = entry.getKey();

                msgBody = new Gson().toJson(t);
                vtMsgBody.add(msgBody);
            }

            //停止播放
            int j = 0;
            for (Map.Entry<Integer, String> entry : ShopMap.sShop.entrySet()) {

                if(j <= 0)
                    break;

                j--;
                Test t = new Test();
                t.action = Msg.ACTION_USER_STOP_PLAY;
                t.shop_id = entry.getKey();

                msgBody = new Gson().toJson(t);
                vtMsgBody.add(msgBody);
            }



            List<String> vtMsgId = queue.batchSendMessage(vtMsgBody);
            for (int i = 0; i < vtMsgBody.size(); i++)
                System.out.println("[" + vtMsgBody.get(i) + "] sent");


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
//            main(null);
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
