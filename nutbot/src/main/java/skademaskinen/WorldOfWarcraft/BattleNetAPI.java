package skademaskinen.WorldOfWarcraft;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

/**
 * This class abstracts ALL get requests to the Battle.net API such that whenever any request hits an exception, its always this class that needs fixing, its useful to contain errors
 */
public class BattleNetAPI {
    private static HttpClient httpClient = HttpClients.createDefault();
    private static String token;
    private static JSONObject realmData = null;
    private static JSONObject guildData = null;

    /**
     * This method is used to create a character from a completely new set of data from the battle.net servers or use a cached object
     * @param name The name of the character
     * @param realm The server of this character
     * @return A character object representing the character
     */
    public static Character getCharacter(String name, String realm){
        return new Character(name, realm);
    }

    /**
     * Initialize the class such that it contains an OAuth token to access the battle.net API
     * @param OauthToken A string containing a token generated from the battle.net API
     */
    public static void init(String OauthToken){
        token = OauthToken;
    }

    /**
     * Verifies the given character with the battle.net API such that the user can be notified if their privacy settings are conflicting with the bot's functionality
     * @param name The name of the character
     * @param server The server of this character
     * @return A boolean representing whether this character is valid or not
     */
    public static boolean verifyCharacter(String name, String server) {
        //if the json key 'code' = 404, then this method returns false
        //if its null then return true and handle the loss of a valid member
        String region = Bot.getConfig().get("region");
        String url = "https://"+region+".api.blizzard.com/profile/wow/character/"+server.toLowerCase().replace(" ", "-")+"/"+name+"?namespace=profile-"+region+"&locale=en_GB&access_token="+token;
        //Shell.println(url);
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(request);
            request.releaseConnection();
            if(response.getStatusLine().getStatusCode() == 404){
                return false;
            }
            else{
                return true;
            }
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
        
        return false;
    }

    /**
     * gets a json object containing a list of every single realm on the given region of wow in the config file
     * @return A json object containing names of all realms in World of Warcraft
     */
    public static JSONObject getRealmData(){
        if(realmData != null) return realmData;
        realmData = executeSubRequest("https://eu.api.blizzard.com/data/wow/realm/index?namespace=dynamic-eu&locale=en_GB");
        return realmData;
    }

    /**
     * Executes a custom request, this is to abstract the get request from the url, and to build the token and region on top of the request
     * @param url The request url to be executed
     * @return The response json object as specified in the HTTP standard
     */
    public static JSONObject executeSubRequest(String url){
        HttpGet request = new HttpGet(url+"&access_token="+token);
        try {
            HttpResponse response = httpClient.execute(request);
            String responseData = EntityUtils.toString(response.getEntity());
            request.releaseConnection();
            return new JSONObject(responseData);
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }

        return null;

    }

    /**
     * Returns a json object containing all data for a World of Warcraft character
     * @param name The name of the character
     * @param server The realm of the character
     * @return A json object response from the battle.net API
     */
    public static JSONObject getCharacterData(String name, String server){
        String region = Bot.getConfig().get("region");
        String url = "https://"+region+".api.blizzard.com/profile/wow/character/"+server.toLowerCase().replace(" ", "-")+"/"+name+"?namespace=profile-"+region+"&locale=en_GB&access_token="+token;

        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = httpClient.execute(request);
            String responseData = EntityUtils.toString(response.getEntity());
            request.releaseConnection();
            return new JSONObject(responseData);
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }

        return null;

    }

    /**
     * Returns a list of all guild members, it checks whether its already been cached or generates a new list
     * @return a list of all members in the guild specified in the configuration file
     */
    public static JSONObject getGuildMemberList() {
        if(guildData != null) return guildData;
        guildData = executeSubRequest("https://eu.api.blizzard.com/data/wow/guild/"+Bot.getConfig().get("guild:realm").toLowerCase().replace(" ", "-")+"/"+Bot.getConfig().get("guild:name").toLowerCase().replace(" ", "-")+"/roster?namespace=profile-eu&locale=en_GB");
        return guildData;
    }
}
