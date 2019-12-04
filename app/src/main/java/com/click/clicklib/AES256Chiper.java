package com.click.clicklib;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Chiper {
    public static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    //AES256 암호화
    public static String AES_Encode(String str, String secretKey)	throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        byte[] textBytes = str.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

        return Base64.encodeToString(cipher.doFinal(textBytes), 0);
    }

    //AES256 복호화
    public static String AES_Decode(String str , String secretKey)	throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {


        byte[] textBytes =Base64.decode(str.getBytes(),0);
//        byte[] textBytes = str.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return new String(cipher.doFinal(textBytes), "UTF-8");
    }

    public static String getServer1(Context context) {
        String server = null;

        try {
            String key = AES_Decode(loadTxt(context).get(2).toString(),"durltjrmaksenj82");
           server  = AES_Decode(loadTxt(context).get(0).toString(),key+key);

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        } catch (NoSuchPaddingException e) {


            e.printStackTrace();
        } catch (InvalidKeyException e) {

            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {

            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {

            e.printStackTrace();
        } catch (BadPaddingException e) {

            e.printStackTrace();
        }
        return server;
    }

    public static String getServer2(Context context) {
        String server = null;
        try {
            String key = AES_Decode(loadTxt(context).get(2).toString(),"durltjrmaksenj82");
            server  = AES_Decode(loadTxt(context).get(0).toString(),key+key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return server;
    }
    public static ArrayList loadTxt(Context context){
        InputStream inputData = context.getResources().openRawResource(R.raw.server1);

        ArrayList<String> arraylist = new ArrayList<String>();

        try {
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputData,"EUC_KR"));
            while(true){
                String string= bufferedReader.readLine();

                if(string != null){
                    arraylist.add(string);
                }else{

                    break;
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return arraylist;
    }
}
