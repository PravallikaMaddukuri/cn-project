import java.util.Arrays;

//class for message retrieving class

public class MsgClass
{
// declarations of variables for message class
    public int IDType;
    public int IndPiece;
    public byte[] payLVar;

    public static final int ChokVar= 0;
    public static final int UNChokVar= 1;
    public static final int VarIntrest= 2;
    public static final int NOT_VarIntrest= 3;
    public static final int HaVar= 4;
    public static final int BFVar = 5;
    public static final int ReqVar= 6;
    public static final int PieVariable= 7;

    public static byte[] mkMeth(int type, byte[] payLVar, int IndPiece){
        byte[] meby;

// switch case to select require function

        switch(type){
            case ChokVar:
            case UNChokVar:
            case VarIntrest:
            case NOT_VarIntrest:
                meby = new byte[5];
                UtilityClass.addInBA(meby, 1, 0);
                meby[4] = (byte) type;
                return meby;
            case HaVar:
            case ReqVar:
                meby = new byte[9];
                UtilityClass.addInBA(meby, 5, 0);
                meby[4] = (byte) type;
                UtilityClass.addInBA(meby, IndPiece, 5);
    
                return meby;
            case BFVar:

                //arranging the bitfields in such a way where we use from large to small
                //this kind of depends on what data struct we use
                // this arranging totally depends on the data structure which we use

                meby = new byte[(5 + payLVar.length)];
                UtilityClass.addInBA(meby, 1 + payLVar.length, 0);
                meby[4] = (byte) type;
                for(int a = 0; a< payLVar.length; a++){
                    meby[5+a] = payLVar[a];
                }
                return meby;
           
            case PieVariable:

                //same case as 6
                //all small values will be edited from back to froth

                meby = new byte[(9 + payLVar.length)];

                // add message length

                UtilityClass.addInBA(meby, 1 + payLVar.length, 0);

                // adding message to print

                meby[4] = (byte) type;

                // adding print text

                UtilityClass.addInBA(meby, IndPiece, 5);

                // adding payval

                for(int a = 0; a< payLVar.length; a++){
                    meby[9+a] = payLVar[a];
                }
                return meby;
            default: 
                return (new byte[0]);
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
    // changing void
    public void read(byte[] muse){
        int sile =  muse.length;
        if(muse.length  == 0) {
            this.IDType = -1;
            return;
        }
        int typeVar = muse[0];
        if (typeVar>=0&&typeVar<=7){
            this.IDType = typeVar;

            //varyfying sile to typeVar. if anything doesnot match? again going to get new error
            //nested loops for executing the logics

            if (sile == 5&&(typeVar==HaVar||typeVar==ReqVar)){
                IndPiece = UtilityClass.byAI(Arrays.copyOfRange(muse, 1, 5));
            } else if (typeVar==PieVariable){
                IndPiece = UtilityClass.byAI(Arrays.copyOfRange(muse, 1, 5));
                this.payLVar = new byte[sile - 5];
                for(int i = 5; i< sile ;i++){
                    this.payLVar[i - 5] =  muse[i];
                    int j = checkBitFieldValues(9,1);
                }
            } else if (typeVar==BFVar){
                this.payLVar = new byte[sile - 1];
                for(int i=1; i<sile; i++){
                    this.payLVar[i - 1] = muse[i];
                }
            }
        } 
    }

}