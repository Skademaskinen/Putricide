package skademaskinen.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This method handles the configuration of the bot software, it is one of the most important classes, as all the specific data for the specific guild is defined in the configuration file for this class. in the future it might also have a defconfig file with default values for many of the functions
 */
public class Config {
    List<Entry> config;
    private String path = "files/config.conf";

    /**
     * This is the constructor, it initializes the representation of the configfile into a Map.
     */
    public Config() {
        config = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(path)))){
            int lineNum = 0;
            for(String line : reader.lines().toList()){
                String key = line.split("=")[0];
                String value = line.split("=").length == 2 ? line.split("=")[1] : "";
                config.add(new Entry(key, value, lineNum));
                lineNum++;
            }
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }

    /**
     * This is the method to write the local configuration object into the configuration file
     */
    public void write(){
        try(FileWriter writer = new FileWriter(new File(path))) {
            for(int line = 0; line < config.size(); line++){
                for(Entry entry : config){
                    if(entry.getLineNum() == line){
                        writer.write(entry.getKey()+"="+entry.getValue()+"\n");
                        break;
                    }
                }
            }
            writer.flush();
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }

    /**
     * This is the global method to get any value for a given key from the configuration file.
     * @param key The key used to get a value
     * @return The corresponding value for the inputted key
     */
    public String get(String key){
        for(Entry entry : config){
            if(entry.getKey().equals(key)){
                return entry.getValue();
            }
        }
        return "";
    }

    /**
     * The method to set a value in the configuration file and writing it to the configuration file
     * @param key The key to write to
     * @param value The value written to the key
     */
    public void set(String key, String value){
        for(Entry entry : config){
            if(entry.getKey().equals(key)){
                entry.setValue(value);
                return;
            }
        }
        config.add(new Entry(key, value));
    }

    /**
     * returns the configuration object, use with caution, this map contains everything in the configuration file
     * @return The configuration object
     */
    public List<Entry> getConfig() {
        return config;
    }

    /**
     * This class is used to save data about a specific config option
     */
    public class Entry{
        private String key;
        private String value;
        private int lineNum;

        public Entry(String key, String value, int lineNum){
            this.key = key;
            this.value = value;
            this.lineNum = lineNum;
        }
        public Entry(String key, String value){
            lineNum = 0;
            for(Entry entry : config) if(entry.getLineNum() > lineNum) lineNum = entry.getLineNum();
            lineNum++;
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public int getLineNum() {
            return lineNum;
        }
    }
}
