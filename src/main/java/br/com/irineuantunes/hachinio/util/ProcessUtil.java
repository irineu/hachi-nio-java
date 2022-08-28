package br.com.irineuantunes.hachinio.util;

import br.com.irineuantunes.hachinio.HachiNIO;

import java.util.ArrayList;
import java.util.List;

public class ProcessUtil {

    private static LivingThread livingThread;

    public static void attach(HachiNIO hachiNIO) {
        if (ProcessUtil.livingThread == null) {
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

        if(!this.isAlive()){
            this.start();
        }
    }

    @Override
    public void run() {
        while(serversAndClients.size() > 0) {
            try {
                Thread.sleep(1 * 1000);
                //System.out.println("tick " + serversAndClients.size());
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

        System.out.println("no more active stuff");
    }
}