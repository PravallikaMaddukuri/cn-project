import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Random;

class MainThrd implements Runnable {

    static final String ADDR_PEER = "PeerInfo.cfg";

    static int ID_OfPeer;
    static TreeMap<Integer, Node> allPeerVars;
    static RecordLogging helper_log;
    static HashMap<Integer, byte[]> dataInFile;


// method for main thread

    public MainThrd(int ID_OfPeer) throws Exception {
        MainThrd.ID_OfPeer = ID_OfPeer;
        allPeerVars = infoReading();

        // Peer directories are made

        try {

            ProcessFile.makedir(ID_OfPeer, ConfigurationSetting.nameOfFile);
            helper_log = new RecordLogging(String.valueOf(MainThrd.ID_OfPeer));

        } catch (Exception c3) {
            c3.printStackTrace();
        }


    }


    // method for running

    @Override
    public void run() {
        // Auto-generated method stub is a

        new Thread(new CliStartClass()).start();
        new Thread(new SvrStartClass()).start();
        new Thread(new ObtainIntrestedNeigh()).start();
        new Thread(new UnChokeSome()).start();



    }

    // class for starting
    private class SvrStartClass implements Runnable {

        byte[] packHS = new byte[32];
        @Override
        public void run() {
            try {
                // at the given port, watch for new connections.
                int val = allPeerVars.get(ID_OfPeer).conn;
                ServerSocket socSrvr = new ServerSocket(val);
                helper_log.informationLog("The Current Server: " + MainThrd.ID_OfPeer + " is running on Port:" + val);
                boolean newPeers = false;
                for (Map.Entry<Integer, Node> neighbor : allPeerVars.entrySet()) {
                    if (newPeers) {
                        Socket socVar = socSrvr.accept();
                        ObjectInputStream sIN = new ObjectInputStream(socVar.getInputStream());
                        ObjectOutputStream sON = new ObjectOutputStream(socVar.getOutputStream());

                        sIN.read(packHS);


                        sON.write(ManageMsg.makeHS(ID_OfPeer));
                        sON.flush();

                        helper_log.informationLog("The Current Peer :" + ID_OfPeer +" establishes a connection to"+ neighbor.getKey());
                        neighbor.getValue().doMsgExg(socVar);
                    }
                    if (ID_OfPeer == neighbor.getKey())
                        newPeers = true;
                }
                socSrvr.close();

            } catch (Exception e12) {
               e12.printStackTrace();
            }
        }
    }


    // class for clistartclass

    private class CliStartClass implements Runnable {

        @Override
        public void run() {
            try {
                for (Map.Entry<Integer, Node> PID : allPeerVars.entrySet()) {

                    if (PID.getKey() == ID_OfPeer)
                        break;
                    Node neiVar = PID.getValue();
                    Socket socVar = new Socket(neiVar.nameOfHost, neiVar.conn);
                    // Input and output Streams which are to be generated
                    ObjectOutputStream cliOS = new ObjectOutputStream(socVar.getOutputStream());
                    ObjectInputStream cliIS = new ObjectInputStream(socVar.getInputStream());

                    //putting the handshake header into the output stream
                    byte[] packHS = ManageMsg.makeHS(ID_OfPeer);
                    cliOS.write(packHS);
                    cliOS.flush();


                    //Authenticating a handshake packet by reading it from the
                    cliIS.readFully(packHS);
                    String messageHeader = UtilityClass.getSFB(packHS, 0 ,17);
                    String messageID_OfPeer = UtilityClass.getSFB(packHS, 28, 31);


                    if(messageHeader.equals("P2PFILESHARINGPROJ") && Integer.parseInt(messageID_OfPeer) == PID.getKey())
                    {

                        neiVar.doMsgExg(socVar);
                    }
                    else
                    {
                        socVar.close();
                    }

                }

            } catch (IOException exception) {

                exception.printStackTrace();

            }
        }
    }


    // method for assigning treemap

    public static TreeMap<Integer, Node> infoReading() throws Exception {
        ArrayList<String> lines = UtilityClass.retrieveFL(ADDR_PEER);
        TreeMap<Integer, Node> peerInfo = new TreeMap<>();
        for (String line : lines) {
            String[] words = line.split(" ");
            peerInfo.put(Integer.valueOf(words[0]), new Node(Integer.parseInt(words[0]), words[1],
                    Integer.valueOf(words[2]), Integer.parseInt(words[3])));
        }
        return peerInfo;
    }

}

// class for setting up the peer

public class PeerSetup {

    static final String CONFIG_ADDR = "ConfigDetails.cfg";

    public static void main(String[] args) throws Exception {
        int IDOfPeer = Integer.parseInt(args[0]);
        ConfigurationSetting.read(CONFIG_ADDR);
        new Thread(new MainThrd(IDOfPeer)).start();
    }

}



// class for getting the unchocking methods
class UnChokeSome implements Runnable {

    public UnChokeSome() {
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                while (Node.noOfDown < MainThrd.allPeerVars.size()) {

                    HashSet<Integer> candiPref = new HashSet<Integer>(Node.goodPeer);
                    HashSet<Integer> prefClone = new HashSet<Integer>(Node.keenPeer);
                    candiPref.removeAll(prefClone);

                    Random rVar = new Random();
                    if (candiPref.size() > 0) {
                        int selectedIN = rVar.nextInt(candiPref.size());
                        Node.unchokeOP = (int) candiPref.toArray()[selectedIN];


                        Node.mapCh.put(Node.unchokeOP, false);
                        Socket socVar = MainThrd.allPeerVars.get(Node.unchokeOP).loop;
                        if (socVar == null) {
                            break;
                        }
                        DataOutputStream ouDSt = new DataOutputStream(socVar.getOutputStream());
                        ouDSt.write(ManageMsg.makeMessage(MsgClass.UNChokVar, null, -1));
                        ouDSt.flush();
                    }
                    MainThrd.helper_log.informationLog("The optimally unchoked neighbor of the Peer " + MainThrd.ID_OfPeer + "is " + Node.unchokeOP);

                    Thread.sleep(ConfigurationSetting.optiUnChoke * 1000);
                
                }


            }
            catch (SocketException e)
            {}
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}

// class for getting the interested peers
class ObtainIntrestedNeigh implements Runnable {

    public ObtainIntrestedNeigh() {
    }


    // method for running the threads
    @Override
    public void run() {
        synchronized (this) {

            try {
                while (Node.noOfDown < MainThrd.allPeerVars.size()) {

                    int ZNeigKVar = ConfigurationSetting.preferredNeighbours;
                    Node.keenPeer.clear();


                    if (Node.goodPeer.size() > ZNeigKVar) {
                        int s = 0;
                        for (HashMap.Entry<Integer, Double> t : Node.lastIntDS.entrySet()) {
                            Node.keenPeer.add(t.getKey());
                            s++;
                            int j = checkBitFieldValues(10,15);
                            if (s >= ZNeigKVar) {
                                break;
                            }
                        }
                    } else {
                        int j = checkBitFieldValues(7, 12);
                        for (Integer IDOfPeer : Node.goodPeer) {
                            Node.keenPeer.add(IDOfPeer);
                        }
                    }

                    Node.lastIntDS.replaceAll((key, value) -> 0.0);

                    for (HashMap.Entry<Integer, Boolean> couVar : Node.mapCh.entrySet()) {
                        Socket socket = MainThrd.allPeerVars.get(couVar.getKey()).loop;
                        if (socket == null) {
                            continue;
                        }
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        if (Node.keenPeer.contains(couVar.getKey())) {
                            dataOutputStream.write(ManageMsg.makeMessage(MsgClass.UNChokVar, null, -1));
                            dataOutputStream.flush();
                            Node.mapCh.put(couVar.getKey(), false);
                            int j = checkBitFieldValues(7,16);

                        } else {


                            dataOutputStream.write(ManageMsg.makeMessage(MsgClass.ChokVar, null, -1));
                            dataOutputStream.flush();
                            int j = checkBitFieldValues(4, 6);
                            Node.mapCh.put(couVar.getKey(), true);

                        }
                    }
                    MainThrd.helper_log.informationLog("The neighbors preferred by Peer " + MainThrd.ID_OfPeer +" are " + Node.keenPeer.toString());

                    Thread.sleep(ConfigurationSetting.intervalToUnCh * 1000);

                }

            }
            catch (SocketException c1)
            {}
            catch (Exception c2) {

                c2.printStackTrace();
            }
            System.exit(0);
        }
    }

    public int checkBitFieldValues(int siz, int coVal){
        int ne = siz;
        do {
            checkNewFund(ne);
        } while (ne>=0);
        return coVal;
    }
    public int checkNewFund(int zi){
        int ne = zi;
        int bitSet = 0;
        for(int m = 0; m > zi ; m--){
            bitSet = ne;
        }
        return bitSet;
    }


}
