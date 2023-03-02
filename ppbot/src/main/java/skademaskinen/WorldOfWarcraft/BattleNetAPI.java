package skademaskinen.WorldOfWarcraft;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import skademaskinen.Utils.GlobalConfig;
import skademaskinen.Utils.ServerConfig;
import skademaskinen.Utils.Shell;

/**
 * This class abstracts ALL get requests to the Battle.net API such that whenever any request hits an exception, its always this class that needs fixing, its useful to contain errors
 */
public class BattleNetAPI {
    private static HttpClient httpClient = HttpClient.newBuilder().build();
    private static String token;
    private static JSONObject realmData = null;
    private static JSONObject guildData = null;
    private static long expiry; //expiry of token in millis from epoch

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
     */
    public static void init(){
        token = generateToken();
    }

    private static String generateToken() {
        String id = GlobalConfig.get().getString("clientId");
        String secret = GlobalConfig.get().getString("clientSecret");
        String url =  "https://oauth.battle.net/token?client_id="+id+"&client_secret="+secret+"&grant_type=client_credentials";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            JSONObject body = new JSONObject(response.body());
            expiry = (body.getLong("expires_in")*1000) + System.currentTimeMillis();
            return body.getString("access_token");
        } catch (IOException | InterruptedException e) {
            Shell.exceptionHandler(e);
        }
        
        return null;

    }

    private static boolean verifyToken() {
        if(expiry > System.currentTimeMillis()) return true;
        //the following is for redundancy in case the first check fails
        String url = "https://oauth.battle.net/oauth/check_token?region=eu&token="+token;
        HttpRequest request = HttpRequest.newBuilder()
            .POST(BodyPublishers.noBody())
            .uri(URI.create(url))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            expiry = new JSONObject(response.body()).getLong("exp")*1000;
            if(new JSONObject(response.body()).getLong("exp")*1000 > System.currentTimeMillis()) return true;
            else return false;

            
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
        return false;
    }

    /**
     * Verifies the given character with the battle.net API such that the user can be notified if their privacy settings are conflicting with the bot's functionality
     * @param name The name of the character
     * @param server The server of this character
     * @return A boolean representing whether this character is valid or not
     */
    public static boolean verifyCharacter(String name, String server) {
        if(!verifyToken()) init();
        //if the json key 'code' = 404, then this method returns false
        //if its null then return true and handle the loss of a valid member
        String url = "https://eu.api.blizzard.com/profile/wow/character/"+server.toLowerCase().replace(" ", "-")+"/"+name+"?namespace=profile-eu";
        JSONObject response = query(url);
        if(response.has("code") && response.getInt("code") == 404) return false;
        else return true;
    }


    /**
     * gets a json object containing a list of every single realm on the given region of wow in the config file
     * @return A json object containing names of all realms in World of Warcraft
     */
    public static JSONObject getRealmData(){
        if(!verifyToken()) init();
        if(realmData != null) return realmData;
        realmData = query("https://eu.api.blizzard.com/data/wow/realm/index?namespace=dynamic-eu");
        return realmData;
    }

    /**
     * Executes a custom request, this is to abstract the get request from the url, and to build the token and region on top of the request
     * @param url The request url to be executed
     * @return The response json object as specified in the HTTP standard
     */
    public static JSONObject query(String url){
        if(!verifyToken()) init();
        url+="&locale=en_GB&access_token="+token;
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            return new JSONObject(response.body());
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
        String url = "https://eu.api.blizzard.com/profile/wow/character/"+server.toLowerCase().replace(" ", "-")+"/"+name+"?namespace=profile-eu";
        return query(url);
    }

    /**
     * Returns a list of all guild members, it checks whether its already been cached or generates a new list
     * @return a list of all members in the guild specified in the configuration file
     */
    public static JSONObject getGuildMemberList(Guild guild) {
        String realmSlug = ServerConfig.get(guild).getString("realm").toLowerCase().replace(" ", "-");
        String guildSlug = ServerConfig.get(guild).getString("name").toLowerCase().replace(" ", "-");
        if(guildData != null) return guildData;
        guildData = query("https://eu.api.blizzard.com/data/wow/guild/"+realmSlug+"/"+guildSlug+"/roster?namespace=profile-eu");
        return guildData;
    }
}
