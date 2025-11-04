package com.company;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Paths;
import java.util.Base64;
import java.io.*;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    public static class client {
        String password;
        String message;
        String mac;

        public void initClient(String pass, String message, String mac){
            this.password = pass;
            this.message = message;
            this.mac = mac;
        }
    }

    public static String encode(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte [] send = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        String hex = "";

        for (byte i : send) {
            hex += String.format("%02X", i);
        }

        return hex.toLowerCase(Locale.ROOT);
    }

    public static boolean check(String macUser, String macServer) throws Exception {
        if (macUser.equals(macServer)){
            return true;
        }
        else return false;
    }

    public static void main(String [] args) throws Exception {
        //args = new String[]{"key", "mess", "hmac"};
        //Here we extract the info from args

        if (args.length != 3){
            System.out.println("wrong formating");
            System.out.println("try writing absoulte path for files :)");
            System.exit(0);
        }
        String pass = args[0];
        Scanner scan1 = new Scanner(new File(String.valueOf(Paths.get(args[1]))));
        Scanner scan2 = new Scanner(new File(String.valueOf(Paths.get(args[2]))));

        StringBuilder mess = new StringBuilder();
        while (scan1.hasNext()){
            mess.append(scan1.nextLine());
            if (scan1.hasNextLine()) {
                mess.append("\n");
            }
        }
        String mac = scan2.nextLine();
        String[] holder = mac.split(" ");
        mac = holder[0];


        //say that we have client1
        client client1 = new client();
        client1.initClient(pass,mess.toString(), mac);

        //create hmac sesstion and verify hmac
        if (check(encode(client1.password, client1.message), mac)){
        } else {
            System.out.println("Error");
            System.exit(0);
        }
        System.out.println("No sign of data Integrity breach, you may proceed");
    }
}
