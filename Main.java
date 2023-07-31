package com.company;

// "static void main" must be defined inreslt a public class.
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.io.*;

public class Main {
     static FileWriter fw;
     static BufferedWriter bw;
    public static void write(String message)
    {
        try
        {
            // System.out.println(message);

            synchronized (fw) {
                // BufferedWriter bw = new BufferedWriter(fw);

                System.out.print(message);
                bw.write(message);
                bw.newLine();
                // bw.close();
            }
        }
        catch(IOException e) {
     
            System.out.println("Exception occurred");
        }

    }

    public static class MyThread extends Thread {
        public void run(){
            while(true) {
                try{
                    write("thread1 t1t1t11t1t1t1");
                }
                catch(Exception e) {
               
                    System.out.println("Exception occurred");
                }

            }

        }
    }
    public static class MyThread1 extends Thread {
        public void run(){
            while(true) {
                try{
                    write("thread2 t2t2t2t2t2t2t22");
                }
                catch(Exception e) {
                    System.out.println("Exception occurred");
                }
            }
        }
    }
    public static void main(String[] args) {
        try {
            fw = new FileWriter("Peer.txt", true);
            bw = new BufferedWriter(fw);
            bw.write("hiii");
             Thread t1 = new MyThread();
             Thread t2 = new MyThread1();
            Thread t11 = new MyThread();
            Thread t22 = new MyThread1();

//             bw.write("hi");

             t1.start();
             t2.start();
            t11.start();
            t22.start();
             Thread.sleep(100);

//stop all threads
             t1.stop(); 

             t2.stop();
            t11.stop();
            // stopping thread t1
            t22.stop();
            bw.close();
        }
        catch (Exception e) {
            System.out.println("Exception occurred");
        }

    }
}




