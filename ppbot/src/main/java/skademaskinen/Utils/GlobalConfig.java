package skademaskinen.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.JSONObject;
import org.json.JSONTokener;

public class GlobalConfig {

    public static JSONObject get(){
        String path = Utils.getRoot()+"/files/config/main.json";
        try(FileReader reader = new FileReader(new File(path))){
            return new JSONObject(new JSONTokener(reader));
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
            return null;
        }
    }
    public static void raidWrite(JSONObject object){
        String path = Utils.getRoot()+"/files/config/main.json";
        try(FileWriter writer = new FileWriter(new File(path))){
            writer.write(object.toString(4));
            writer.flush();
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }
}
