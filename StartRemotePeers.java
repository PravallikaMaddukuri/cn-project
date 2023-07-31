package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



public class StartRemotePeers {

    private static final String scriptPrefix = "cd CNProject/src/com/company;javac -cp jsch-0.1.54.jar *.java; java -cp \\cise\\homes\\dpakanati\\CNProject\\src\\com\\company peerProcess ";

    public static class peerData {

        private String peerID;
        private String hostName;

        public peerData(String peerID, String hostName) {
            super();
            this.peerID = peerID;
            this.hostName = hostName;
        }

        public String getPid() {
            return peerID;
        }

        public void setPid(String peerID) {
            this.peerID = peerID;
        }

        public String retrieve_HN() {
            return hostName;
        }

        public void HN_Setter(String hostName) {
            this.hostName = hostName;
        }

    }

    public static void main(String[] args) {

        ArrayList<peerData> peerList = new ArrayList<>();

        String ciseUser = "dpakanati";



        peerList.add(new peerData("1001", "lin114-01.cise.ufl.edu"));
        peerList.add(new peerData("1002", "lin114-02.cise.ufl.edu"));
        peerList.add(new peerData("1003", "lin114-03.cise.ufl.edu"));
        peerList.add(new peerData("1004", "lin114-04.cise.ufl.edu"));
        peerList.add(new peerData("1005", "lin114-05.cise.ufl.edu"));
        peerList.add(new peerData("1006", "lin114-06.cise.ufl.edu"));

        

        for (peerData remotePeer : peerList) {
            try {
                JSch jsch = new JSch();
                 JSch which accepts a password.
                 
                System.out.println("Comming till private key call");
                jsch.addIdentity("C:\\Users\\dhanu\\.ssh\\private", "");
                System.out.println("Private key loaded");
                Session session = jsch.getSession(ciseUser, remotePeer.retrieve_HN(), 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.connect();
                System.out.println("Session connected");

                System.out.println("Session to peer# " + remotePeer.getPid() + " at " + remotePeer.retrieve_HN());

                Channel channel = session.openChannel("exec");
                System.out.println("remotePeerID"+remotePeer.getPid());
                ((ChannelExec) channel).setCommand(scriptPrefix + remotePeer.getPid());

                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream input = channel.getInputStream();
                channel.connect();

                System.out.println("Channel Connected to peer# " + remotePeer.getPid() + " at "
                        + remotePeer.retrieve_HN() + " svr with commands");

                (new Thread() {
                    @Override
                    public void run() {

                        InputStreamReader inputReader = new InputStreamReader(input);
                        BufferedReader b_R = new BufferedReader(inputReader);
                        String line = null;

                        try {

                            while ((line = b_R.readLine()) != null) {
                                System.out.println(remotePeer.getPid() + ">:" + line);
                            }
                            b_R.close();
                            inputReader.close();
                        } catch (Exception ex) {
                            System.out.println(remotePeer.getPid() + " Exception >:");
                            ex.printStackTrace();
                        }

                        channel.disconnect();
                        session.disconnect();
                    }
                }).start();

            } catch (JSchException e) {

// TODO Auto-generated catch block
                System.out.println(remotePeer.getPid() + " JSchException >:");
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println(remotePeer.getPid() + " Exception >:");
                ex.printStackTrace();
            }

        }
    }

}
