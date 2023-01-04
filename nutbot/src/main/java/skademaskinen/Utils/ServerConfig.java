package skademaskinen.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.JSONObject;
import org.json.JSONTokener;

import net.dv8tion.jda.api.entities.Guild;

public class ServerConfig {
    
    public static JSONObject get(Guild guild){
        String path = "files/config/"+guild.getId()+"/config.json";
        try(FileReader reader = new FileReader(new File(path))){
            return new JSONObject(new JSONTokener(reader));
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
            return null;
        }
    }

    public static void write(Guild guild, JSONObject object){
        String path = "files/config/"+guild.getId()+"/config.json";
        try(FileWriter writer = new FileWriter(new File(path))){
            writer.write(object.toString(4));
            writer.flush();
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
        }
    }

    public static JSONObject raidGet(Guild guild){
        String path = "files/config/"+guild.getId()+"/raid.json";
        try(FileReader reader = new FileReader(new File(path))){
            return new JSONObject(new JSONTokener(reader));
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
            return null;
        }
    }

    public static void raidWrite(Guild guild, JSONObject object){
        String path = "files/config/"+guild.getId()+"/raid.json";
        try(FileWriter writer = new FileWriter(new File(path))){
            writer.write(object.toString(4));
            writer.flush();
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
        }
    }

    public static JSONObject pvpGet(Guild guild){
        String path = "files/config/"+guild.getId()+"/pvp.json";
        try(FileReader reader = new FileReader(new File(path))){
            return new JSONObject(new JSONTokener(reader));
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
            return null;
        }
    }
    public static void pvpWrite(Guild guild, JSONObject object){
        String path = "files/config/"+guild.getId()+"/pvp.json";
        try(FileWriter writer = new FileWriter(new File(path))){
            writer.write(object.toString(4));
            writer.flush();
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
        }
    }

    public static JSONObject rolepickerGet(Guild guild){
        String path = "files/config/"+guild.getId()+"/rolepicker.json";
        try(FileReader reader = new FileReader(new File(path))){
            return new JSONObject(new JSONTokener(reader));
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
            return null;
        }
    }
    public static void rolepickerWrite(Guild guild, JSONObject object){
        String path = "files/config/"+guild.getId()+"/rolepicker.json";
        try(FileWriter writer = new FileWriter(new File(path))){
            writer.write(object.toString(4));
            writer.flush();
        }
        catch(Exception e){
            Shell.exceptionHandler(e, guild);
        }
    }
}
