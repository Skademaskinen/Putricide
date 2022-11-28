package skademaskinen.WorldOfWarcraft;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class BattleNetAPI {
    private static HttpClient httpClient = HttpClients.createDefault();
    private static String token;

    public Character getCharacter(String name, String realm){
        return new Character(name, realm);
    }

    public static void init(String OauthToken){
        token = OauthToken;
    }

    public static String getToken() {
        return token;
    }

    public static HttpClient getHttpClient(){
        return httpClient;
    }
}
