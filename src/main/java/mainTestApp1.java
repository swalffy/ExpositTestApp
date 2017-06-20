
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class mainTestApp1 {

    private static String getFileExtension(String fileName) {
        int index = fileName.indexOf('.');
        return index == -1 ? null : fileName.substring(index);
    }

    public static Map<String,String> argsParser(String[] args){
        Map<String,String> result = new HashMap<String, String>();
        for (int i = 0; i < args.length; i+=2){
            result.put(args[i], args[i+1]);
        }
        return result;
    }

    public static List<FileDownloader> getListFromJson(String fileName) throws IOException {
        List<FileDownloader> result = new ArrayList<FileDownloader>() ;
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e){
            System.out.println("File not found");
            return result;
        }
        ObjectMapper mapper = new ObjectMapper();
        String temp;
        while ((temp = reader.readLine()) != null){
            FileDownloader f;
            try{
                f = mapper.readValue(temp, FileDownloader.class);
            } catch (JsonParseException e){
                continue;
            }
            result.add(f);
        }
        return result;
    }

    public static List<FileDownloader> getListFromCSV(String fileName, String location) throws IOException {
        List<FileDownloader> result= new ArrayList<FileDownloader>();
        CSVParser parser;
        try {
            File csvData = new File(fileName);
            parser = CSVParser.parse(csvData, Charset.defaultCharset() , CSVFormat.EXCEL);
        } catch (FileNotFoundException e){
            System.out.println("File not found");
            return result;
        }
        for(CSVRecord csvRecord : parser) {
            FileDownloader f;
            try {
                f = new FileDownloader(csvRecord.get(0), csvRecord.get(1));
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
            result.add(f);
        }
        return result;
    }

    public static void downloadsFromFile(final String location, String file, int streams) throws IOException {
        String fileExtension = getFileExtension(file);
        List <FileDownloader> list = new ArrayList<FileDownloader>();
        if (fileExtension.equals(".json"))
            list = getListFromJson(file);
        else if (fileExtension.equals(".csv"))
            list = getListFromCSV(file, location);
        else {
            System.out.println("Wrong file extension. Please, input json or csv file");
        }
        if (!list.isEmpty()){
            final List <String> errors = new ArrayList<String>();
            ExecutorService executorService = Executors.newFixedThreadPool(streams);

            for (final FileDownloader item : list)
                executorService.submit(new Thread(){
                    public void run(){
                        try{
                            if (!item.Download(location))
                                errors.add(item.getName());
                        } catch (IOException e){}
                    }
                });
            executorService.shutdown();
            while (!executorService.isTerminated() && !Thread.currentThread().isInterrupted()) {}

            System.out.println(">Downloading was ended. " + (list.size()-errors.size()) + " of " + list.size() + ";");
            if (!errors.isEmpty()){
                System.out.println(">Failed files: ");
                for (String item : errors)
                    System.out.println(" " + item);
            }
        }
    }

    public static void main(String[] args) throws IOException{
        if (args.length % 2 != 0 ) {
            System.out.println("Wrong arguments line. Execution");
            return;
        }
        Map<String,String> arguments = argsParser(args);

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
            downloadsFromFile(location, file, countOfStreams);
        }
        else {
            System.out.println("Wrong arguments line. Execution");
            return;
        }
    }
}
