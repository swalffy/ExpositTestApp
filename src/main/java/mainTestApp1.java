
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class mainTestApp1 {

    public static Map<String,String> argsParser(String[] args){
        Map<String,String> result = new HashMap<String, String>();
        for (int i = 0; i < args.length; i+=2){
            result.put(args[i], args[i+1]);
        }
        return result;
    }
    public static void jsonFileDownloader(String fileName, String location) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String temp;
        List<String> errors = new ArrayList<String>();
        int count = 0;
        while ((temp = reader.readLine()) != null){
            FileDownloader f;
            try{
                f = mapper.readValue(temp, FileDownloader.class);
            } catch (JsonParseException e){
                continue;
            }
            if (f.Download(location)) {
                System.out.println("completed");
                count++;
            }
            else
                errors.add(f.getName());
        }
        int totalCount = errors.size() + count;
        System.out.println(">Downloading was ended. " + count + " of " + totalCount + ";");
        if (!errors.isEmpty()){
            System.out.println(">Failed files: ");
            for (String item : errors)
                System.out.println(" " + item);
        }
    }
    public static void csvFileDownloader(String fileName,String location) throws IOException {
        File csvData = new File(fileName);
        CSVParser parser = CSVParser.parse(csvData, Charset.defaultCharset() , CSVFormat.EXCEL);
        int count = 0;
        List<String> errors = new ArrayList<String>();
        for(CSVRecord csvRecord : parser){
            FileDownloader f;
            try{
                f = new FileDownloader(csvRecord.get(0),csvRecord.get(1));
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
            if (f.Download(location)) {
                System.out.println("completed");
                count++;
            }
            else
                errors.add(f.getName());
        }
        int totalCount = errors.size() + count;
        System.out.println(">Downloading was ended. " + count + " of " + totalCount + ";");
        if (!errors.isEmpty()){
            System.out.println(">Failed files: ");
            for (String item : errors)
                System.out.println(" " + item);
        }
    }
    private static String getFileExtension(String fileName) {
        int index = fileName.indexOf('.');
        return index == -1 ? null : fileName.substring(index);
    }
    public static void downloadsFromFile(String location, String file) throws IOException {
        String fileExtension = getFileExtension(file);
        if (fileExtension.equals(".json"))
            jsonFileDownloader(file, location);
        else if (fileExtension.equals(".csv"))
            csvFileDownloader(file, location);
        else
            System.out.println("Wrong file extension. Please, input json or csv file");
    }

    public static void main(String[] args) throws IOException{
        String[] a = {"-loc", "D:/Trash/downloads", "-fil", "D:/Trash/2.csv"};
        if (args.length % 2 != 0 ) {
            System.out.println("Wrong arguments line. Execution");
            return;
        }
        Map<String,String> arguments = argsParser(a);

        String location = arguments.get("-loc");
        String name = arguments.get("-nam");
        String url = arguments.get("-url");
        String file = arguments.get("-fil");

        int countOfStreams;
        try{
            countOfStreams = Integer.parseInt(arguments.get("-sem"));
        } catch (NumberFormatException e){
            countOfStreams = 1;
        }
        if (location != null && (file == null && (name != null && url != null))){
            new File(location).mkdirs();
            new FileDownloader(name,url).Download(location);
        }else if (location != null && (file != null && (name == null && url == null))){
            new File(location).mkdirs();
            downloadsFromFile(location, file);
        }
        else {
            System.out.println("Wrong arguments line. Execution");
            return;
        }
    }
}
