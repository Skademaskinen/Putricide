package skademaskinen.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private Map<String, String> config;
    private String path = "files/config.conf";

    public Config() {
        config = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(path)))){
            for(String line : reader.lines().toList()){
                String key = line.split("=")[0];
                String value = line.split("=")[1];
                System.out.println("Initialized config key: "+key);
                config.put(key, value);
            }
        }
        catch(Exception e){
            
        }

    }

    public String get(String key){
        return config.get(key);
    }
}
