package skademaskinen.WorldOfWarcraft;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import skademaskinen.Bot;
import skademaskinen.Utils.Loggable;
import skademaskinen.Utils.Utils;

/**
 * The RaidTeam manager object, despite the Raid command having a similar purpose, this class provides abstractions such that the Raid class can focus on handling the response to the user
 */
public class RaidTeam implements Loggable {
    private static String filepath = "files/raid.json";
    private static Map<String, Character> characters = new HashMap<>();

    /**
     * This method adds a user and their character to the raid team
     * @param user The user object representing a discord user
     * @param name The name of the World of Warcraft character they are joining with
     * @param role The role they are planning to be on the raid team
     * @param server The server this character is from
     * @return
     */
    public static boolean add(User user, String name, String role, String server){
        if(!BattleNetAPI.verifyCharacter(name.toLowerCase(), server)){
            return false;
        }
        remove(user);

        JSONObject team = Utils.readJSON(filepath);
        JSONObject raider = new JSONObject();
        raider.put("name", name);
        raider.put("server", server);
        JSONObject roleobj = team.getJSONObject(Utils.capitalize(role));
        roleobj.put(user.getId(), raider);
        Utils.writeJSON(filepath, team);
        characters.put(user.getId(), new Character(name, server));
        update();
        return true;
    }

    /**
     * This method removes a discord user from the raid team
     * @param user The user object representing a Discord user
     */
    public static void remove(User user){
        JSONObject team = Utils.readJSON(filepath);
        for(String key : team.keySet()){
            if(team.getJSONObject(key).has(user.getId())){
                team.getJSONObject(key).remove(user.getId());
            }
        }
        Utils.writeJSON(filepath, team);
        update();
    }

    /**
     * This method updates the raid team message, it requires the config file to contain fields:
     * These are the raid:channel and raid:message, they are channel and message ids.
     * @return The response for the user, it can be that it is updated successfully or an error message
     */
    public static String update() {
        if(Bot.getConfig().get("raid:message") == null || Bot.getConfig().get("raid:channel") == null){
            return "Failed to update raid team, the configuration id might be wrong";
        }
        Guild guild = Bot.getJda().getGuildById(Bot.getConfig().get("guild:id"));
        TextChannel channel = guild.getTextChannelById(Bot.getConfig().get("raid:channel"));
        Message message = channel.getHistoryAround(Bot.getConfig().get("raid:message"), 2).complete().getMessageById(Bot.getConfig().get("raid:message"));
        JSONObject team = Utils.readJSON(filepath);
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("Raid Team!")
            .setDescription("This is the raid team, this message will get updated with raid team members!")
            .setImage(Bot.getConfig().get("guild:image"));

		builder.appendDescription("\n**Raid team composition:** "+team.getJSONObject("Tank").length()+"/"+team.getJSONObject("Healer").length()+"/"+(team.getJSONObject("Ranged Damage").length()+team.getJSONObject("Melee Damage").length()));

        String[] roles = {"Tank", "Healer", "Ranged Damage", "Melee Damage"};

        for(String role : roles){
            String temp = "";
            for(String raiderId : team.getJSONObject(role).keySet()){
                JSONObject raider = team.getJSONObject(role).getJSONObject(raiderId);
                Character character = new Character(raider.getString("name"), raider.getString("server"));

                temp+= "\n\n"+ guild.retrieveMemberById(raiderId).complete().getAsMention();
                temp+= "\n"+ Utils.capitalize(character.getName());
                if(!character.getRealm().toLowerCase().replace(" ", "-").equals("argent-dawn")){
                    temp+= " (" + Utils.capitalize(raider.getString("server").replace("-", " ")) + ")";
                }
                temp+= "\n"+character._getClass();
                temp+= "\n"+character.getSpecialization();
                temp+= "\n"+character.getIlvl()+"/"+character.getAverageIlvl()+" ilvl";
            }
            builder.addField(role, temp, true);
            if(role.equals("Healer")) builder.addBlankField(false);
        }

        message.editMessageEmbeds(builder.build()).queue();

        return "Successfully updated raid team!";
    }

}
