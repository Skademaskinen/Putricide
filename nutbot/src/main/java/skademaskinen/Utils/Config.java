package skademaskinen.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Config {
    private Map<String, String> config;
    private String path = "files/config.conf";

    public Config() {
        config = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(path)))){
            for(String line : reader.lines().toList()){
                String key = line.split("=")[0];
                String value = line.split("=").length == 2 ? line.split("=")[1] : "";
                config.put(key, value);
            }
        }
        catch(Exception e){
            
        }

    }

    private void write(){
        String content = "";
        for(Entry<String, String> entry : config.entrySet()){
            content+= entry.getKey()+"="+entry.getValue()+"\n";
        }
        try(FileWriter writer = new FileWriter(new File(path))){
            writer.write(content);
        }
        catch(Exception e){
        }
    }

    public String get(String key){
        return config.get(key);
    }

    public void set(String key, String value){
        config.put(key, value);
        write();
    }

    public Map<String, String> getConfig() {
        return config;
    }
}
