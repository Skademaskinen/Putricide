package skademaskinen.WorldOfWarcraft;

import org.json.JSONObject;


public class Character {
    private String name;
    private String realm;
    private String Class;
    private String specialization;
    private int ilvl;
    private int averageIlvl;
    private String avatarUrl;

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
    public String getAvatarURL(){
        return BattleNetAPI.executeSubRequest(avatarUrl).getJSONArray("assets").getJSONObject(0).getString("value");
    }
}
