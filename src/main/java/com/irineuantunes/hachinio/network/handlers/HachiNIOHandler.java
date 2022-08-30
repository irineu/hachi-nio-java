package com.irineuantunes.hachinio.network.handlers;

import com.irineuantunes.hachinio.network.HachiNIOConnection;

import java.util.Map;

public interface HachiNIOHandler {

    void onConnect(HachiNIOConnection connection);
    void onDisconnect(HachiNIOConnection connection);
    void onClientError(Throwable ex, HachiNIOConnection connection);
    void onMessage(HachiNIOConnection connection, Map header, byte[] message);
    void onWritten(HachiNIOConnection connection);

}
