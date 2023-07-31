import java.io.IOException;
import java.util.ArrayList;

//class for configuration settings

public class ConfigurationSetting
{

    //variables declarations for configuration settings
    //all are static variables

    static int preferredNeighbours;
    static int intervalToUnCh;
    static int optiUnChoke;

    //string declaration for file name

    static String nameOfFile;
    static int sizeOfFile;
    static int sizeOfPiece;
    static int piecesTot;

//certain methods for parsing integer to other forms

    public static void read(String filePath) throws IOException
    {

        ArrayList<String> lines = UtilityClass.retrieveFL(filePath);

        //splitting the lines for further operations
        preferredNeighbours = Integer.parseInt(lines.get(0).split(" ")[1]);
        intervalToUnCh = Integer.parseInt(lines.get(1).split(" ")[1]);
        optiUnChoke = Integer.parseInt(lines.get(2).split(" ")[1]);

        //splitting the name with three characters
        nameOfFile = lines.get(3).split(" ")[1];

        //declaration for file size
        sizeOfFile = Integer.parseInt(lines.get(4).split(" ")[1]);
        sizeOfPiece = Integer.parseInt(lines.get(5).split(" ")[1]);
        piecesTot = (int) Math.ceil((double) sizeOfFile / sizeOfPiece);
    }


}


