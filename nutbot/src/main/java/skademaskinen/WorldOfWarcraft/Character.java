package skademaskinen.WorldOfWarcraft;

import org.json.JSONException;
import org.json.JSONObject;

import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

/**
 * This class is an abstraction of a World of Warcraft character, it contains all the relevant information for using this character for the raid team, it is also convenient to avoid pinging blizzard servers constantly
 */
public class Character {
    private String name;
    private String realm;
    private String Class;
    private String specialization;
    private int ilvl;
    private int averageIlvl;
    private String avatarUrl;

    /**
     * The constructor of this class, it is the most important method as this object is essentially just a struct for saving data.
     * @param name Name of the character
     * @param realm The in-game server this character is from
     */
    public Character(String name, String realm) {
        JSONObject data = BattleNetAPI.getCharacterData(name.toLowerCase(), realm);
        this.name = name;
        this.realm = realm;
        this.Class = data.getJSONObject("character_class").getString("name");
        this.specialization = data.getJSONObject("active_spec").getString("name");
        this.ilvl = data.getInt("equipped_item_level");
        this.averageIlvl = data.getInt("average_item_level");
        avatarUrl = data.getJSONObject("media").getString("href");
    }
    /**
     * Getter for the name String
     * @return the name attribute
     */
    public String getName(){
        return name;
    }
    /**
     * Getter for the realm String
     * @return The realm attribute
     */
    public String getRealm(){
        return realm;
    }
    /**
     * Getter for the class String
     * @return The Class attribute
     */
    public String _getClass(){
        return Class;
    }
    /**
     * Getter for the specialization String
     * @return The specialization attribute
     */
    public String getSpecialization(){
        return specialization;
    }
    /**
     * Getter for the ilvl integer
     * @return The ilvl attribute
     */
    public int getIlvl(){
        return ilvl;
    }
    /**
     * Getter for the average ilvl integer
     * @return The average ilvl attribute
     */
    public int getAverageIlvl(){
        return averageIlvl;
    }
    /**
     * This method calls the BattleNetAPI to get the avatar of a user, it sends a get request to fetch the URL for their character
     * @return a String containing the url to the avatar of a character
     */
    public String getAvatarURL(){
        try{
            return BattleNetAPI.executeSubRequest(avatarUrl).getJSONArray("assets").getJSONObject(0).getString("value");
        }
        catch(JSONException e){
            Shell.exceptionHandler(e);
            return Bot.getJda().getGuildById(Bot.getConfig().get("guildId")).getIconUrl();
        }
    }
}
