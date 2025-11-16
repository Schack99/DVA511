package com.company;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Paths;
import java.io.*;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Scanner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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
        //create hmacsha256 session
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        //return a Hex of bytes
        byte [] send = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return HexFormat.of().formatHex(send).toLowerCase(Locale.ROOT);
    }

    public static boolean check(String macUser, String macServer) throws Exception {
        return macUser.equals(macServer);
    }

    public static client fileHandling(String pass, Scanner scan1, Scanner scan2) throws Exception{
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
        client client1 = new client();
        client1.initClient(pass,mess.toString(), mac);
        return client1;
    }

    static class MyHandler implements HttpHandler {
        String response = "";
        public MyHandler(String mess) {
            response = mess;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static void main(String [] args) throws Exception {
        //Here we extract the info from args
        if (args.length != 3){
            System.out.println("wrong formating");
            System.out.println("try writing absoulte path for files :)");
            System.exit(0);
        }

        //Create client with pass,mess and mac
        Scanner scan1 = new Scanner(new File(String.valueOf(Paths.get(args[1]))));
        Scanner scan2 = new Scanner(new File(String.valueOf(Paths.get(args[2]))));
        client client = fileHandling(args[0], scan1, scan2);

        //create hmac sesstion and verify hmac
        if (check(encode(client.password, client.message), client.mac)){
        } else {
            System.out.println("Error");
            System.exit(0);
        }
        System.out.println("No sign of data Integrity breach, you may proceed");

        //Get message on web (this part I found online, but I did modify it a bit)
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/main", new MyHandler(client.message));
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("copy paste this to local web browser:");
        System.out.println("http://localhost:8000/main");
    }
}
