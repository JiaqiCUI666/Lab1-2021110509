package util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Document {
    private static int m = 0;
    public static List<String> ReadFromFile(String pathname) throws IOException {
        List<String> all_words = new ArrayList<>();
        File file = new File(pathname);
        FileReader fr = new FileReader(file);
        BufferedReader rea = new BufferedReader(fr);
        String line = null;
        while((line=rea.readLine())!=null) {
            String[] words = line.split("\\P{Alpha}+");
            int n = 0;
            while(words[n]!=null&&!words[n].isEmpty()) {
                all_words.add(words[n].toLowerCase());
                n++;
                if(n >= words.length) break;
            }
            m++;
        }
        return all_words;
    }
    public static void WriteToFile(String path, int head, int tail) throws IOException {
        FileWriter writer = new FileWriter("./src/main/file/random.txt");
        String[] temp =  path.split(" ");
        temp = Arrays.copyOfRange(temp, head, tail);
        for(String value: temp){
            writer.write(value + " ");
        }
        writer.close();
    }
}