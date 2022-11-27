package com.irineuantunes.hachinio.util;

import com.irineuantunes.hachinio.HachiNIO;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtil {

    private static LivingThread livingThread;

    public static void attach(HachiNIO hachiNIO) {
        if (ProcessUtil.livingThread == null) {
            ProcessUtil.livingThread = new LivingThread();
        }else if (ProcessUtil.livingThread.getState() == Thread.State.TERMINATED){
            ProcessUtil.livingThread = new LivingThread();
        }

        ProcessUtil.livingThread.attach(hachiNIO);
    }
}

class LivingThread extends Thread{

    private List<HachiNIO> serversAndClients;

    public LivingThread() {
        this.serversAndClients = new ArrayList<>();
    }

    public void attach(HachiNIO hachiNIO){
        this.serversAndClients.add(hachiNIO);

        //if(!this.isAlive()){
        if(this.getState() == Thread.State.NEW){
            this.start();
        }
    }

    @Override
    public void run() {
        while(serversAndClients.size() > 0) {
            try {
                Thread.sleep(1 * 1000);
                for (int i = 0; i < serversAndClients.size(); i++) {
                    if (!serversAndClients.get(i).isActive()) {
                        serversAndClients.remove(i);
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}