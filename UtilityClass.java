import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.TreeMap;

public class UtilityClass {
    
    public static int byAI(byte[] bArr) {
        return ByteBuffer.wrap(bArr).getInt();
    }

    public static byte[] inBA(int intToConvert) {
        
        return ByteBuffer.allocate(4).putInt(intToConvert).array();
    }
// method for adding integer to byte address

    public static void addInBA(byte[] bArr, int intToConvert, int startIndex) {

        // message length to a 4-byte array

        byte[] lenArr = UtilityClass.inBA(intToConvert);

        // the first four bytes of the message array

        for(int z = 0; z < 4; z++) {
            bArr[startIndex + z] = lenArr[z];
        }
    }

//method for gettinh shakehands and chocking method
    public static String getSFB(byte[] bArr, int start, int end) {
        int sa = end - start + 1;
        //tbd - exception
        if (sa <= 0 || end >= bArr.length)
        {return "";}

        //You must return a portion of the byte array as a string.
        byte[] opSt = new byte[sa];
        System.arraycopy(bArr, start, opSt, 0, sa);
        return new String(opSt, StandardCharsets.UTF_8);

    }

    // public class for array list for getting name of files
    public static ArrayList<String> retrieveFL(String nameofFile) throws IOException {
        ArrayList<String> words = new ArrayList<>();
        BufferedReader bReader = new BufferedReader(new FileReader(nameofFile));
        String word = bReader.readLine();
        while (word != null) {
            words.add(word);
            word = bReader.readLine();
        }
        bReader.close();
        return words;
    }

}
