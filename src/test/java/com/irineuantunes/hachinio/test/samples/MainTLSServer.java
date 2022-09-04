package com.irineuantunes.hachinio.test.samples;

import com.irineuantunes.hachinio.HachiNIOTLSServer;
import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.network.handlers.HachiNIOServerHandler;
import com.irineuantunes.hachinio.util.ByteUtil;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Map;

public class MainTLSServer {
    public static void main(String[] args) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException, InterruptedException {

        SSLContext context;

        context = SSLContext.getInstance("TLSv1.2");

        context.init(createKeyManagers("/Users/irineuantunes/Downloads/crt-03/cert.jks", "123456", "123456"),
                createTrustManagers("/Users/irineuantunes/Downloads/crt-03/trustedCerts.jks", "123456"),
                new SecureRandom()
        );
        HachiNIOTLSServer server = new HachiNIOTLSServer("127.0.0.1", 3575,context, new HachiNIOServerHandler() {
            @Override
            public void onConnect(HachiNIOConnection connection) {
                System.out.println("on connect");
            }

            @Override
            public void onDisconnect(HachiNIOConnection connection) {
                System.out.println("on disconnect");
                System.out.println(connection.isActive());
            }

            @Override
            public void onClientError(Throwable ex, HachiNIOConnection connection) {
                if (ex.getMessage() != null) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServerError(Throwable ex, AsynchronousServerSocketChannel serverSockChannel) {
                if (ex.getMessage() != null) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onMessage(HachiNIOConnection connection, Map header, byte[] message) {
                System.out.println(header.get("transaction"));
                System.out.println(ByteUtil.bytesToHex(message));
                System.out.println(new String(message));

                try {
                    connection.send(header, "ok".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onWritten(HachiNIOConnection connection) {
                System.out.println("written");
            }
        });

        server.listen();
    }

    protected static KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream keyStoreIS = new FileInputStream(filepath);
        try {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        } finally {
            if (keyStoreIS != null) {
                keyStoreIS.close();
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    protected static TrustManager[] createTrustManagers(String filepath, String keystorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream trustStoreIS = new FileInputStream(filepath);
        try {
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
        } finally {
            if (trustStoreIS != null) {
                trustStoreIS.close();
            }
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }
}
