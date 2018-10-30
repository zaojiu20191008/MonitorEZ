package com.easygo.monitor.common;

import android.text.TextUtils;

import com.easygo.monitor.utils.EZLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    private static final String TAG = "MD5";

    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            EZLog.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }
        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            EZLog.e(TAG, "calculatedDigest null");
            return false;
        }
        EZLog.v(TAG, "Calculated digest: " + calculatedDigest);
        EZLog.v(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            EZLog.e(TAG, "Exception while getting digest", e);
            return null;
        }
        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            EZLog.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                EZLog.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
}