import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Node {

    //resources shared between nodes
    // total peers for having the file here
    // number of nodes used

    static int noOfDown = 0;

    // list of id's used for geeting the interestedpeers

    static HashSet<Integer> goodPeer = new HashSet<>();
    static HashSet<Integer> keenPeer = new HashSet<>();

    // id for currently unchocked peers

    static int unchokeOP;

    // download rates for the specified neighbors during the most recent unchoking period

    static HashMap<Integer, Double> lastIntDS = new HashMap<>();
    static HashMap<Integer, BitSet> mapBF = new HashMap<>();
    static HashMap<Integer, Boolean> mapCh = new HashMap<>();
    static HashMap<Integer, Boolean> writtenFile = new HashMap<>();
    static BitSet bitsNeed;

    //static boolean writtenFile;

    // ---------- members locally ------------//

    int id_Peer;
    String nameOfHost;
    int conn;
    int fileHas;
    Socket loop;
    boolean wasItChoked;
    static HashMap<Integer, byte[]> dataInFile;

    public Node(int id_Peer, String nameOfHost, int conn, int fileHas) throws Exception {
        this.id_Peer = id_Peer;
        this.nameOfHost = nameOfHost;
        this.conn = conn;
        this.fileHas = fileHas;
        mapBF.put(id_Peer, new BitSet(ConfigurationSetting.piecesTot));
        if (id_Peer != MainThrd.ID_OfPeer) {
            mapCh.put(id_Peer, true);
            lastIntDS.put(id_Peer, 0.0);
        }

        if (this.fileHas == 1 && (id_Peer == MainThrd.ID_OfPeer)) {
            mapBF.get(id_Peer).set(0, ConfigurationSetting.piecesTot);
            noOfDown = 1;
            dataInFile = ProcessFile.retrieveDataInC(ConfigurationSetting.sizeOfFile, ConfigurationSetting.sizeOfPiece, ConfigurationSetting.nameOfFile);
            writtenFile.put(MainThrd.ID_OfPeer, true);
        } else if (this.fileHas != 1 && (id_Peer == MainThrd.ID_OfPeer)) {
            dataInFile = new HashMap<Integer, byte[]>();
            writtenFile.put(MainThrd.ID_OfPeer, false);

        }

    }

    public void doMsgExg(Socket loop) {
        this.loop = loop;
        new Thread(new ExMsg(loop)).start();
    }

    public void bitFsSet(int id_Peer, byte[] bytes) {
        mapBF.put(id_Peer, BitSet.valueOf(bytes));
    }

    public void bitFSet(int id_Peer, int pieceIn) {
        mapBF.get(id_Peer).set(pieceIn);
    }

    class ExMsg implements Runnable {
        Socket loop;

        public ExMsg(Socket loop) {
            this.loop = loop;
        }

        public int checkBitFieldValues(int siz, int coVal){
            int ne = siz;
            do {
                checkNewFund(ne);
            } while (ne>=0);
            return coVal;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {

                    DataInputStream ipSt = new DataInputStream(loop.getInputStream());
                    DataOutputStream pSt = new DataOutputStream(loop.getOutputStream());

                    // When the current peer has any pieces, send a bitfield message.

                    if (!mapBF.get(MainThrd.ID_OfPeer).isEmpty()) {
                        pSt.write(ManageMsg.makeMessage(MsgClass.BFVar,
                                mapBF.get(MainThrd.ID_OfPeer).toByteArray(), -1));
                        pSt.flush();
                    }

                    while (noOfDown < MainThrd.allPeerVars.size()) {

                           int sizeOfMsg = 0;
                           try
                           {
                                sizeOfMsg = ipSt.readInt();

                           }
                           catch (EOFException c1)
                           {
                              
                                continue;
                           }

                        byte[] arrayOfM = new byte[sizeOfMsg];
                        double begin = System.currentTimeMillis();
                        ipSt.read(arrayOfM);
                        double clock = System.currentTimeMillis() - begin;

                        MsgClass message = ManageMsg.getMsg(arrayOfM);
                        lastIntDS.put(id_Peer, sizeOfMsg / clock);
                        lastIntDS = downloadSS(lastIntDS);
                        switch (message.IDType) {
                        case MsgClass.BFVar:
                            mapBF.put(id_Peer, BitSet.valueOf(message.payLVar));


                            // pieces of neighbor possesses, but the opposite is true

                            BitSet bitsNeeded = (BitSet) mapBF.get(MainThrd.ID_OfPeer).clone();
                            bitsNeeded.xor(mapBF.get(id_Peer));
                            bitsNeeded.andNot(mapBF.get(MainThrd.ID_OfPeer));

                            if (!(bitsNeeded.length() == 0)) {
                                pSt.write(ManageMsg.makeMessage(MsgClass.VarIntrest, null, -1));
                                pSt.flush();
                            } else {
                                pSt.write(ManageMsg.makeMessage(MsgClass.NOT_VarIntrest, null, -1));
                                pSt.flush();

                            }

                            if (message.payLVar.length * 8 >= ConfigurationSetting.piecesTot) {
                                noOfDown += 1;
                                writtenFile.put(id_Peer, true);
                            }

                            break;

                        case MsgClass.VarIntrest:
                            MainThrd.helper_log.informationLog("Peer "+ id_Peer  + " sent interest message to Peer "+ MainThrd.ID_OfPeer);
                            goodPeer.add(id_Peer);
                            break;

                        case MsgClass.NOT_VarIntrest:

                            MainThrd.helper_log.informationLog("Peer "+ id_Peer + " sent uninterested message to Peer "+ MainThrd.ID_OfPeer);
                            goodPeer.remove(id_Peer);
                            break;

                        case MsgClass.ChokVar:
                            MainThrd.helper_log.informationLog("Peer "+ id_Peer + " choked Peer "+ MainThrd.ID_OfPeer);

                            // contemporary peer was choked by this neighbor

                            wasItChoked = true;
                            break;

                        case MsgClass.UNChokVar:
                            MainThrd.helper_log.informationLog("Peer "+ id_Peer + " unchoked Peer "+ MainThrd.ID_OfPeer);

                            // This neighbor has freed up the other person.
                            wasItChoked = false;
                            // request piece required

                            bitsNeeded = (BitSet) mapBF.get(MainThrd.ID_OfPeer).clone();
                            bitsNeeded.xor(mapBF.get(id_Peer));
                            bitsNeeded.andNot(mapBF.get(MainThrd.ID_OfPeer));

                            if (bitsNeed == null) {
                                bitsNeed = new BitSet(ConfigurationSetting.piecesTot);
                            }


                            if (!(bitsNeeded.size() == 0)) {

                                int pieceIn = bitsNeeded.nextSetBit(new Random().nextInt(bitsNeeded.size()));
                                if (pieceIn < 0) {
                                    pieceIn = bitsNeeded.nextSetBit(0);
                                }
                                if (pieceIn >= 0) {
                                    bitsNeed.set(pieceIn);
                                    pSt.write(ManageMsg.makeMessage(MsgClass.ReqVar, null, pieceIn));
                                    pSt.flush();
                                }
                            }

                            break;
                        case MsgClass.ReqVar:
                            if (id_Peer == unchokeOP || keenPeer.contains(id_Peer)) {
                                int pieceIn = message.IndPiece;
                                byte[] piece = new byte[ConfigurationSetting.sizeOfPiece];

                                piece = retrieveChunkD(pieceIn);

                                //sending pieces
                                if(piece != null) {
                                    pSt.write(ManageMsg.makeMessage(MsgClass.PieVariable, piece, pieceIn));
                                    pSt.flush();
                                } else {
                                    System.out.println("The index of the null piece is" + pieceIn);
                                }
                              

                            }
                            break;
                        case MsgClass.PieVariable:
                            dataInFile.put(message.IndPiece, message.payLVar);
                            // setting fields of bit

                            bitFSet(MainThrd.ID_OfPeer, message.IndPiece);

                            // sending the messages

                            for (Node node : MainThrd.allPeerVars.values()) {
                                if (node.loop != null) {
                                    DataOutputStream oStream = new DataOutputStream(node.loop.getOutputStream());
                                    if(dataInFile.size() == ConfigurationSetting.piecesTot) {
                                        oStream.write(ManageMsg.makeMessage(MsgClass.HaVar, null, ConfigurationSetting.piecesTot));
                                        oStream.write(ManageMsg.makeMessage(MsgClass.HaVar, null, message.IndPiece));
                                    } else {
                                        oStream.write(ManageMsg.makeMessage(MsgClass.HaVar, null, message.IndPiece));
                                    }
                                    oStream.flush();
                                }

                            }

                            MainThrd.helper_log.informationLog("Peer "+ MainThrd.ID_OfPeer + " transferred the piece from Peer. "+ id_Peer+" The total number of pieces with the Peer is " + dataInFile.size());


                            // asking for more
                            bitsNeeded = (BitSet) mapBF.get(MainThrd.ID_OfPeer).clone();
                            bitsNeeded.xor(mapBF.get(id_Peer));
                            bitsNeeded.andNot(mapBF.get(MainThrd.ID_OfPeer));

                            bitsNeed = new BitSet(ConfigurationSetting.piecesTot);

                            if (!(bitsNeeded.length() == 0) && !wasItChoked) {
                                int pieceIn = bitsNeeded.nextSetBit(0);
                                bitsNeed.set(pieceIn);
                                pSt.write(ManageMsg.makeMessage(MsgClass.ReqVar, null, pieceIn));
                                pSt.flush();
                                bitsNeeded.andNot(bitsNeed);

                            }

                            if (dataInFile.size() == ConfigurationSetting.piecesTot) {

                                MainThrd.helper_log.informationLog("Peer " + MainThrd.ID_OfPeer + "  has received the entire file.");
                                if(!writtenFile.get(MainThrd.ID_OfPeer)) {
                                    noOfDown += 1;
                                    typeFile();
                                    writtenFile.put(MainThrd.ID_OfPeer, true);
                                    }
                            }

                            break;
                        case MsgClass.HaVar:


                            if(message.IndPiece == ConfigurationSetting.piecesTot) {
                                noOfDown += 1;
                                writtenFile.put(id_Peer, true);
                                break;
                            }
                            MainThrd.helper_log.informationLog("Peer "+ MainThrd.ID_OfPeer + " sent the message to Pee "+ id_Peer);


                            bitFSet(id_Peer, message.IndPiece);
                            bitsNeeded = (BitSet) mapBF.get(MainThrd.ID_OfPeer).clone();
                            bitsNeeded.xor(mapBF.get(id_Peer));
                            bitsNeeded.andNot(mapBF.get(MainThrd.ID_OfPeer));

                            if (!(bitsNeeded.length() == 0) && !(dataInFile.size() == ConfigurationSetting.piecesTot)) {
                                pSt.write(ManageMsg.makeMessage(MsgClass.VarIntrest, null, -1));
                                pSt.flush();
                            } else {
                                pSt.write(ManageMsg.makeMessage(MsgClass.NOT_VarIntrest, null, -1));
                                pSt.flush();
                                }

                            break;

                        default:
                            break;

                        }



                    }

                    Thread.sleep(5000);
                    System.exit(0);

                }
                catch (SocketException s)
                {
                    System.out.println("The Socket lost its connection with " + id_Peer);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

    }

    public static byte[] retrieveChunkD(int indexc) {

        return dataInFile.get(indexc);
    }

    public int checkBitFieldValues(int siz, int coVal){
        int ne = siz;
        do {
            checkNewFund(ne);
        } while (ne>=0);
        return coVal;
    }

    public static void typeChunkD(int indexc, byte[] data) {

        dataInFile.put(indexc, data);
    }

    public void typeFile() throws Exception {
       
            dataInFile = ProcessFile.arrangeData(dataInFile);
            File file = new File("./" + MainThrd.ID_OfPeer + "/thefile");
            if (file.createNewFile()) {
                FileWriter writeFile = new FileWriter("./" + MainThrd.ID_OfPeer + "/" + ConfigurationSetting.nameOfFile, true);
                BufferedWriter writeBuff = new BufferedWriter(writeFile);

                for (HashMap.Entry<Integer, byte[]> entry : dataInFile.entrySet()) {
                    writeBuff.write(new String(entry.getValue(), StandardCharsets.UTF_8));
                }
                writeBuff.close();
                writeFile.close();
            }

        
    }

    public int checkNewFund(int zi){
        int ne = zi;
        int bitSet = 0;
        for(int m = 0; m > zi ; m--){
            bitSet = ne;
        }
        return bitSet;
    }

    public static HashMap<Integer, Double> downloadSS(HashMap<Integer, Double> hsh) throws Exception {
        List<Map.Entry<Integer, Double>> lst = new LinkedList<Map.Entry<Integer, Double>>(hsh.entrySet());

        // Sort the list

        Collections.sort(lst, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> obj1, Map.Entry<Integer, Double> obj2) {
                return -1*(obj1.getValue()).compareTo(obj2.getValue());
            }
        });

        // put data from sorted list to hashmap

        HashMap<Integer, Double> nw = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> sorted : lst) {
            nw.put(sorted.getKey(), sorted.getValue());
        }
        return nw;

    }

}

