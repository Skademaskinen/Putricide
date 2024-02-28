package skademaskinen.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;
import org.json.JSONTokener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import skademaskinen.Bot;

/**
 * The purpose of this class is to compile every single type of utility that have multiple purposes into one single static class
 */
public class Utils {
    
    /**
     * This method is used to have a standardized time format all around this software.
     * @return a timestamp formatted like this: "[06/12/2022 - 13:11]"
     */
    public static String timestamp(){
        return "["+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))+"]";
    }
    
    /**
     * This will convert a miliseconds time to minutes and seconds
     * @param duration This is the milliseconds as a long value
     * @return a string formatted like this: for duration=81000, the return will be 01:21
     */
    public static String getTime(long duration) {
        String minutes = String.valueOf((duration/1000)/60);
        if(Integer.parseInt(minutes) < 10){
            minutes = "0" + minutes;
        }
        String seconds = String.valueOf((duration/1000)%60);
        if(Integer.parseInt(seconds) < 10){
            seconds = "0"+seconds;
        }
        return minutes + ":" + seconds;
    }

    /**
     * This method returns a boolean describing whether a given member has a role
     * @param member The member being checked for
     * @param role The role being checked
     * @return A boolean symbolizing the result
     */
    public static boolean hasRole(Member member, Role role){
        return member.getRoles().contains(role);
    }

    /**
     * Reads a json file and returns a JSONObject for the given filepath
     * @param filepath The path to the file that is wanted to be loaded.
     * @return A JSONObject with the data in the given filepath, or null if an exception occurs
     */
    public static JSONObject readJSON(String filepath) {
        try{
            return new JSONObject(new JSONTokener(new FileInputStream(new File(filepath))));
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
            return null;
        }
    }

    /**
     * Writes a JSONObject to the given filepath
     * @param filepath The filepath to be written to
     * @param data The JSONObject to be written on that path
     */
    public static void writeJSON(String filepath, JSONObject data) {
        try(FileWriter writer = new FileWriter(filepath)){
            writer.write(data.toString(4));
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }

    /**
     * This method takes a string as input and capitalizes every first letter, it is useful for names of things in Discord
     * @param input The string to be formatted
     * @return The formatted string
     */
    public static String capitalize(String input){
        String[] temp = input.split(" ");
        String output = "";
        for(String word : temp){
            output+=word.substring(0, 1).toUpperCase()+word.substring(1, word.length())+" ";
        }
        output = output.substring(0, output.length()-1);
        return output;
    }

    public static boolean isURLValid(String searchTerm) {
        try{
            new URL(searchTerm);
            return true;
        }
        catch(MalformedURLException e){
            return false;
        }
    }

    public static String getRoot(){
        if(Bot.stringArgs.containsKey("config")){
            return Bot.stringArgs.get("config");
        }
        return ".";
    }
}
