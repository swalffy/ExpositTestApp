import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;


public class FileDownloader {
    private String name;
    private URL url;

    FileDownloader(){
    }

    FileDownloader(String name, String url)throws MalformedURLException{
        this.name = name;
        this.url = new URL(url);
    }
    public String getName(){
        return name;
    }
    public URL getUrl(){
        return url;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setUrl(String url) throws  MalformedURLException{
        this.url = new URL(url);
    }

    public boolean Download(String location)throws IOException{
        String target = location + "/" + name;
        InputStream in;
        FileOutputStream out;
        try {
            out = new FileOutputStream(target);
        } catch (FileNotFoundException e){
            System.out.println(">You don't have the permission to open this folder. Skip this file");
            return false;
        }
        try {
            in = new BufferedInputStream(url.openStream());
        } catch (FileNotFoundException e){
            System.out.println(">Corrupted url. Skip " + name + "  file");
            return false;
        } catch (IOException e){
            System.out.println(">Corrupted url. Skip " + name + "  file");
            return false;
        } catch (HTTPException e){
            System.out.println(">Corrupted url. Skip " + name + "  file");
            return false;
        }
        System.out.print(">Downloading " + name + " file: ");
        byte buf[] = new byte[2048];
        int count = -1;
        while ((count = in.read(buf)) != -1) {
            out.write(buf, 0, count);
        }
        return true;
    }
}
