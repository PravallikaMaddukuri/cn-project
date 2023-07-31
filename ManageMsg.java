import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

//class file for managing messages class
public class ManageMsg{


    public static byte[] makeMessage(int cls, byte[] payLVar, int IndPiece)
    {

        //can retrive integer and probably giving the actual message that they have created
        //as we are assuming "actual message" which is defined by the project

        return MsgClass.mkMeth(cls, payLVar, IndPiece);
    }

    // returning the message type

    public static MsgClass getMsg(byte[] messageArray){

        //assuming for the mmessage class not thehandshake

        //the finilization of the return type is not yet done

        MsgClass gma = new MsgClass();
        gma.read(messageArray);

        //int returned is TYPE, then byte array as ints.
        // int types are returend which as the ints are of byte type

        return gma;
    }
    public int checkBitFieldValues(int siz, int coVal)
    {
        int ne = siz;
        do
        {
                System.out.print("");
        }
        while (ne>=0);
        return coVal;
    }
    public static byte[] makeHS(int peerID)
    {
        //declaration of variables which are used for handshakes

        byte[] packHS = new byte[32];
        byte[] headHS = "P2PFILESHARINGPROJ".getBytes();
        byte[] nullPad = "0000000000".getBytes();

        // for filling th length in btw the header and peerID
        //allocation of four values for conversion of array to strings

        byte[] bytPID = ByteBuffer.allocate(4).put(String.valueOf(peerID).getBytes()).array();

        int index1=0;

        // iterative loop for length of the head hankshake

        for(int x =0; x<headHS.length;x+=1)
        {
            packHS[index1] = headHS[x];
            index1+=1;
        }

        // iterative loop for iterating null pad
        for (int y =0; y<nullPad.length;y+=1)
        {
            packHS[index1] = nullPad[y];
            index1+=1;
        }

        // iterative loop for byteint
        for(int z =0; z< bytPID.length ; z+=1)
        {
            packHS[index1] = bytPID[z];
            index1+=1;
        }

        // print statement for handshaking
        System.out.println("The generated Hand Shake packet is "+new String(packHS, StandardCharsets.UTF_8));
        return packHS;

    }

    public void checkNewB(){
        int di = 0;
        while(di<10){
            di = checkBitFieldValues(9,1);
            di = di+1;
            int bytSt = checkBitFieldValues(7,1);
        }
    }

}