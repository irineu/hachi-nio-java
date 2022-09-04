package com.irineuantunes.hachinio.test.samples;

import com.google.gson.Gson;
import com.irineuantunes.hachinio.HachiNIOTLSClient;
import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.network.handlers.HachiNIOHandler;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class MainTLSClient {
    private static HachiNIOTLSClient client;

    private static TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };

    public static void main(String[] args) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, trustAllCerts, new SecureRandom());

        client = new HachiNIOTLSClient("127.0.0.1", 3575, context, new HachiNIOHandler(){

            @Override
            public void onConnect(HachiNIOConnection connection) {
                System.out.println("on connect");
                Map m = new HashMap();
                m.put("transaction", "123");

                try {
                    connection.send(m, "hello".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnect(HachiNIOConnection connection) {
                System.out.println("on disconnect");
                System.out.println(connection.isActive());
            }

            @Override
            public void onClientError(Throwable ex, HachiNIOConnection connection) {
                ex.printStackTrace();
            }

            @Override
            public void onMessage(HachiNIOConnection connection, Map header, byte[] message) {
                System.out.println("on message");
                System.out.println(new Gson().toJson(header));
                System.out.println(new String(message));
            }

            @Override
            public void onWritten(HachiNIOConnection connection) {
                System.out.println("on written");
            }
        });

        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
