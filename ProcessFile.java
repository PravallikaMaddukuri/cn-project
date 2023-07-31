import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


// class for processing file
public class ProcessFile{
    public static boolean verify(String id_peer) throws IOException {
        File file = new File("./" + id_peer + "/thefile");
        return file.exists();
    }

    //method for getting bits
    public static byte[] getbits(byte[] real, int small, int large) {
        byte[] outcome = new byte[large - small];
        System.arraycopy(real, small, outcome, 0, Math.min(real.length - small, large - small));
        return outcome;
    }

    // method for retrieving data

    public static HashMap<Integer, byte[]> retrieveDataInC(int sizeoffile, int sizeochunk, String nameoffile) throws Exception {
      
        HashMap<Integer, byte[]> dataInFile= new HashMap<Integer, byte[]>();
        BufferedInputStream file = new BufferedInputStream(new FileInputStream("./" + MainThrd.ID_OfPeer + "/" + nameoffile));
        byte[] arrayofbyte = new byte[sizeoffile];

        file.read(arrayofbyte);
        file.close();
        int indexc = 0, count = 0;

        while (indexc < sizeoffile) {

            if (indexc + sizeochunk <= sizeoffile) {
                dataInFile.put(count, getbits(arrayofbyte, indexc, indexc + sizeochunk));
                count++;
            } else {
                dataInFile.put(count ,getbits(arrayofbyte, indexc, sizeoffile));
                count++;
            }
            indexc += sizeochunk;

        }

        return dataInFile;

    }

// method for making directories
    public static void makedir(int id_peer, String file) throws IOException {

        Path r1 = Paths.get("./" + String.valueOf(id_peer));
        System.out.println(r1.toString());
        if (Files.exists(r1)) {
            clear(r1, file);
        } else {
            Files.createDirectory(r1);

        }
        System.out.println("At this location");
        new File("./" + String.valueOf(id_peer) + "/logs_" + String.valueOf(id_peer) + ".log");
    }

    //method for clearing
    public static void clear(Path path, String file) throws IOException {

        Stream<Path> listOfFile = Files.list(path);

        for (Object obj : listOfFile.toArray()) {

            Path fileCurr = (Path) obj;
            if (!fileCurr.getFileName().toString().equals(file)) {
                Files.delete(fileCurr);
            }

        }
        listOfFile.close();


    }


// method for arrainging data
    public static HashMap<Integer, byte[]> arrangeData(HashMap<Integer, byte[]> map) throws Exception
    {
        List<Map.Entry<Integer, byte[]> > set =
                new LinkedList<Map.Entry<Integer, byte[]> >(map.entrySet());

        // Sorting the lists
        Collections.sort(set, new Comparator<Map.Entry<Integer, byte[]> >() {
            public int compare(Map.Entry<Integer, byte[]> obj1,
                               Map.Entry<Integer, byte[]> obj2)
            {
                return (obj1.getKey()).compareTo(obj2.getKey());
            }
        });

        // add information from a sorted list to a hashmap
        HashMap<Integer, byte[]> temp = new LinkedHashMap<Integer, byte[]>();
        for (Map.Entry<Integer, byte[]> sorted : set) {
            temp.put(sorted.getKey(), sorted.getValue());
        }
        return temp;

    }



}

