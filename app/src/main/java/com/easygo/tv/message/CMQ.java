package com.easygo.tv.message;

import android.util.Log;

import com.qcloud.cmq.*;

import java.util.ArrayList;
import java.util.List;


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

//                        account.deleteQueue("queue-test10");
            //创建队列
//                        System.out.println("---------------create queue ...---------------");
//                        QueueMeta meta = new QueueMeta();
//                        meta.pollingWaitSeconds = 10;
//                        meta.visibilityTimeout = 10;
//                        meta.maxMsgSize = 65536;
//                        meta.msgRetentionSeconds = 345600;
//                        account.createQueue("queue-test10",meta);
//                        System.out.println("queue-test10 created");
//                        account.createQueue("queue-test11",meta);
//                        System.out.println("queue-test11 created");
//                        account.createQueue("queue-test12",meta);
//                        System.out.println("queue-test12 created");

            //列出当前帐号下所有队列名字
//                        System.out.println("---------------list queue ...---------------");
//                        ArrayList<String> vtQueue = new ArrayList<String>();
//                        int totalCount = account.listQueue("",-1,-1,vtQueue);
//                        System.out.println("totalCount:" + totalCount);
//                        for(int i=0;i<vtQueue.size();i++)
//                        {
//                                System.out.println("queueName:" + vtQueue.get(i));
//                        }

            //删除队列
//                        System.out.println("---------------delete queue ...---------------");
//                        account.deleteQueue("queue-test11");
//                        System.out.println("queue-test11 deleted");
//                        account.deleteQueue("queue-test12");
//                        System.out.println("queue-test12 deleted");

            //获得队列实例
            System.out.println("--------------- queue[queue-test10] ---------------");
//                        Queue queue = account.getQueue("queue-test10");
            Queue queue = account.getQueue("android-test");

            //设置队列属性
            System.out.println("---------------set queue attributes ...---------------");
            QueueMeta meta1 = new QueueMeta();
            meta1.pollingWaitSeconds = 20;
            queue.setQueueAttributes(meta1);
            System.out.println("pollingWaitSeconds=20 set");

            //获取队列属性
//            System.out.println("---------------get queue attributes ...---------------");
//            QueueMeta meta2 = queue.getQueueAttributes();
//            System.out.println("maxMsgHeapNum:" + meta2.maxMsgHeapNum);
//            System.out.println("pollingWaitSeconds:" + meta2.pollingWaitSeconds);
//            System.out.println("visibilityTimeout:" + meta2.visibilityTimeout);
//            System.out.println("maxMsgSize:" + meta2.maxMsgSize);
//            System.out.println("createTime:" + meta2.createTime);
//            System.out.println("lastModifyTime:" + meta2.lastModifyTime);
//            System.out.println("activeMsgNum:" + meta2.activeMsgNum);
//            System.out.println("inactiveMsgNum:" + meta2.inactiveMsgNum);

            //发送消息
//                        System.out.println("---------------send message ...---------------");
//                        String msgId = queue.sendMessage("hello world,this is cmq sdk for java");
//                        System.out.println("[hello world,this is cmq sdk for java] sent");

//                        //接收消息
//                        System.out.println("---------------recv message ...---------------");
//                        Message msg = queue.receiveMessage(10);
//
//                        System.out.println("msgId:" + msg.msgId);
//                        System.out.println("msgBody:" + msg.msgBody);
//                        System.out.println("receiptHandle:" + msg.receiptHandle);
//                        System.out.println("enqueueTime:" + msg.enqueueTime);
//                        System.out.println("nextVisibleTime:" + msg.nextVisibleTime);
//                        System.out.println("firstDequeueTime:" + msg.firstDequeueTime);
//                        System.out.println("dequeueCount:" + msg.dequeueCount);
//
//                        //删除消息
//                        System.out.println("---------------delete message ...---------------");
//                        queue.deleteMessage(msg.receiptHandle);
//                        System.out.println("receiptHandle:" + msg.receiptHandle +" deleted");

            //批量操作
            //批量发送消息
            System.out.println("---------------batch send message ...---------------");
            ArrayList<String> vtMsgBody = new ArrayList<String>();
            String msgBody = "hello world,this is cmq sdk for java 1";
//            vtMsgBody.add(msgBody);
//            msgBody = "hello world,this is cmq sdk for java 2";
//            vtMsgBody.add(msgBody);
//            msgBody = "hello world,this is cmq sdk for java 3";
//            vtMsgBody.add(msgBody);
//            msgBody = "stop";

//            msgBody = "add_182365469";
//            msgBody = "remove_182365469";
//            vtMsgBody.add(msgBody);

//            msgBody = "add_182364991";
//            msgBody = "remove_182364991";
//            vtMsgBody.add(msgBody);

//            msgBody = "add_182365528";
            msgBody = "remove_182365528";
            vtMsgBody.add(msgBody);


            //182365469
            //182364991
            //182365528
            List<String> vtMsgId = queue.batchSendMessage(vtMsgBody);
//            for (int i = 0; i < vtMsgBody.size(); i++)
//                System.out.println("[" + vtMsgBody.get(i) + "] sent");
//            for (int i = 0; i < vtMsgId.size(); i++)
//                System.out.println("msgId:" + vtMsgId.get(i));

            //批量接收消息
//            ArrayList<String> vtReceiptHandle = new ArrayList<String>(); //保存服务器返回的消息句柄，用于删除消息
//            System.out.println("---------------batch recv message ...---------------");
//            List<Message> msgList = queue.batchReceiveMessage(10, 10);
//            System.out.println("recv msg count:" + msgList.size());
//            for (int i = 0; i < msgList.size(); i++) {
//                Message msg1 = msgList.get(i);
//                System.out.println("msgId:" + msg1.msgId);
//                System.out.println("msgBody:" + msg1.msgBody);
//                System.out.println("receiptHandle:" + msg1.receiptHandle);
//                System.out.println("enqueueTime:" + msg1.enqueueTime);
//                System.out.println("nextVisibleTime:" + msg1.nextVisibleTime);
//                System.out.println("firstDequeueTime:" + msg1.firstDequeueTime);
//                System.out.println("dequeueCount:" + msg1.dequeueCount);
//
//                vtReceiptHandle.add(msg1.receiptHandle);
//            }
//            //批量删除消息
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
}
