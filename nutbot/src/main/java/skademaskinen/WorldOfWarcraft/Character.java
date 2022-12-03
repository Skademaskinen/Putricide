package skademaskinen.WorldOfWarcraft;

import org.json.JSONObject;


import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

public class Character {
    private String name;
    private String realm;
    private String Class;
    private String specialization;
    private int ilvl;
    private int averageIlvl;

    public Character(String name, String realm) {
        String region = Bot.getConfig().get("region");
        try{
            
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }
    public String getName(){
        return name;
    }
    public String getRealm(){
        return realm;
    }
    public String _getClass(){
        return Class;
    }
    public String getSpecialization(){
        return specialization;
    }
    public int getIlvl(){
        return ilvl;
    }
    public int getAverageIlvl(){
        return averageIlvl;
    }
}
