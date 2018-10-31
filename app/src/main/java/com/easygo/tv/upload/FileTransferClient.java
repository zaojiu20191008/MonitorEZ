/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.easygo.tv.upload;

import android.util.Log;

import com.easygo.monitor.utils.DataManager;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Administrator
 */
public class FileTransferClient implements Runnable {

    public static final String TAG = "FileTransferClient";

//    private static final String SERVER_IP = "127.0.0.1"; // 服务端IP
//    private static final String SERVER_IP = "192.168.96.31"; // 服务端IP 
//    private static final String SERVER_IP = "132.232.103.240"; // 服务端IP
//    private static final int SERVER_PORT = 8899; // 服务端端口  
//    private static final String SERVER_IP = "111.230.31.211"; // 服务端IP
    private static final String SERVER_IP = "192.168.31.75"; // 服务端IP
    private static final int SERVER_PORT = 6657; // 服务端端口

    private Socket client;

    private FileInputStream fis;

    private DataOutputStream dos;

    private boolean hasAlreadyUpload = false;



    /**
     * 构造函数<br/>
     * 与服务器建立连接
     */
    public FileTransferClient() {
    }

    public FileTransferClient(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 向服务端传输文件
     *
     * @param filePath
     * @throws Exception
     */
    public void sendFile(String filePath) throws Exception {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                dos = new DataOutputStream(client.getOutputStream());

                // 文件名和长度  
                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();

                // 开始传输文件  
                Log.i(TAG, "======== Start transferring files ========");
                byte[] bytes = new byte[1024];
                int length = 0;
                //long progress = 0;
                while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);
                    dos.flush();
                    //progress += length;
                    //System.out.print("| " + (100 * progress / file.length()) + "% |");
                }
                Log.i(TAG, "======== File transfer successfully ========");
                hasAlreadyUpload = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            hasAlreadyUpload = false;
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (dos != null) {
                dos.close();
            }
            client.close();
        }
    }


    private  String filePath = DataManager.getInstance().getRecodeFilePath();//todo 待定文件路径

    @Override
    public void run() {

        this.upload();

    }

    public void upload() {
        try {
            this.client = new Socket(SERVER_IP, SERVER_PORT);
            Log.i(TAG, "Cliect[port:" + client.getLocalPort() + "] Successful connection to server");
            sendFile(filePath); // 传输文件

            if(hasAlreadyUpload) {
                //上传完成 删除文件
                File deleteFile = new File(filePath);
                if(deleteFile.exists()) {
                    deleteFile.delete();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.i(TAG, "upload: " + ex.getMessage());
        }
    }

    public void start() {
        new Thread(this).start();
    }

}
