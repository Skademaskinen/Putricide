package skademaskinen.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This method handles the configuration of the bot software, it is one of the most important classes, as all the specific data for the specific guild is defined in the configuration file for this class. in the future it might also have a defconfig file with default values for many of the functions
 */
public class Config {
    private Map<String, String> config;
    private String path = "files/config.conf";

    /**
     * This is the constructor, it initializes the representation of the configfile into a Map.
     */
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
            Shell.exceptionHandler(e);
        }

    }

    /**
     * This is the method to write the local configuration object into the configuration file
     */
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

    /**
     * This is the global method to get any value for a given key from the configuration file.
     * @param key The key used to get a value
     * @return The corresponding value for the inputted key
     */
    public String get(String key){
        return config.get(key);
    }

    /**
     * The method to set a value in the configuration file and writing it to the configuration file
     * @param key The key to write to
     * @param value The value written to the key
     */
    public void set(String key, String value){
        config.put(key, value);
        write();
    }

    /**
     * returns the configuration object, use with caution, this map contains everything in the configuration file
     * @return The configuration object
     */
    public Map<String, String> getConfig() {
        return config;
    }
}
