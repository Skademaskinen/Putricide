package skademaskinen.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Utils {
    
    public static String timestamp(){
        return "["+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))+"]";
    }
    
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

	public Role findRole(Member member, String name) {
        List<Role> roles = member.getRoles();
        return roles.stream()
            .filter(role -> role.getName().equals(name)) // filter by role name
            .findFirst() // take first result
            .orElse(null); // else return null
	}

    public static JSONObject readJSON(String filepath) {
        try{
            return new JSONObject(new JSONTokener(new FileInputStream(new File(filepath))));
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
            return null;
        }
    }

    public static void writeJSON(String filepath, JSONObject data) {
        try(FileWriter writer = new FileWriter(filepath)){
            writer.write(data.toString(4));
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }
}
