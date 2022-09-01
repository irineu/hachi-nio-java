package com.irineuantunes.hachinio;

import com.irineuantunes.hachinio.network.HachiNIOConnection;
import com.irineuantunes.hachinio.network.HachiNIOTLSConnection;
import com.irineuantunes.hachinio.network.handlers.*;
import com.irineuantunes.hachinio.util.ProcessUtil;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.*;
import java.security.cert.CertificateException;

public class HachiNIOTLSClient extends HachiNIOClient{

    private SSLContext context;

    public HachiNIOTLSClient(String srvAddr, int srvPort, HachiNIOHandler handler) throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, KeyManagementException {
        super(srvAddr, srvPort, handler);

        context = SSLContext.getInstance("TLSv1.2");

        context.init(createKeyManagers("/Users/t736609/Desktop/projects/CPP/FraudDetector/relay-mtls/crt-03/cert.jks", "123456", "123456"),
                createTrustManagers("/Users/t736609/Desktop/projects/CPP/FraudDetector/relay-mtls/crt-03/trustedCerts.jks", "123456"),
                new SecureRandom()
        );
    }

    public void connect() throws IOException {
        HachiNIOClient instance = this;
        sockChannel = AsynchronousSocketChannel.open();
        sockChannel.connect( new InetSocketAddress(this.srvAddr, this.srvPort), sockChannel, new CompletionHandler<Void, AsynchronousSocketChannel >() {

            @Override
            public void completed(Void unused, AsynchronousSocketChannel clientSocketChannel) {
                SSLSession dummySession = context.createSSLEngine().getSession();
                int packetBufferSize = dummySession.getPacketBufferSize();
                int applicationBufferSize = dummySession.getApplicationBufferSize();
                dummySession.invalidate();

                connection = new HachiNIOTLSConnection(ByteBuffer.allocate(applicationBufferSize),
                        ByteBuffer.allocate(packetBufferSize),
                        ByteBuffer.allocate(packetBufferSize),
                        new TLSClientReadCompletionHandler(instance),
                        new ClientWriteCompletionHandler(instance),
                        clientSocketChannel
                );

                SSLEngine engine = context.createSSLEngine();
                engine.setUseClientMode(true);
                ((HachiNIOTLSConnection)connection).setSSLEngine(engine);

                try {
                    engine.beginHandshake();
                } catch (SSLException e) {
                    e.printStackTrace();
                }

                clientSocketChannel.read(
                        ((HachiNIOTLSConnection) connection).getRawReadSocketByteBuffer(),
                        clientSocketChannel,
                        connection.getReadCompleteHandler()
                );

                //TODO chamar o sslcycle no getReadCompleteHandler do cliente para disparar o hadshake

                //TODO for after handshake
                handler.onConnect(connection);
            }

            @Override
            public void failed(Throwable ex, AsynchronousSocketChannel asynchronousSocketChannel) {
                handler.onClientError(ex, connection);
                isAlive = false;
            }
        });


        this.isAlive = true;
        ProcessUtil.attach(this);
        System.out.println(this.isAlive);
    }

    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
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

    protected TrustManager[] createTrustManagers(String filepath, String keystorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
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
