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

public class PvpTeam implements Loggable {
    private static String filepath = "files/pvp.json";
    private static Map<String, Character> characters = new HashMap<>();

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

    public static String update() {
        if(Bot.getConfig().get("pvp:message") == null || Bot.getConfig().get("pvp:channel") == null){
            return "Failed to update pvp team, the configuration id might be wrong";
        }
        Guild guild = Bot.getJda().getGuildById(Bot.getConfig().get("guildId"));
        TextChannel channel = guild.getTextChannelById(Bot.getConfig().get("pvp:channel"));
        Message message = channel.getHistoryAround(Bot.getConfig().get("pvp:message"), 2).complete().getMessageById(Bot.getConfig().get("pvp:message"));
        JSONObject team = Utils.readJSON(filepath);
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("Pvp Team!")
            .setDescription("This is the pvp team, this message will get updated with pvp team members!")
            .setImage(Bot.getConfig().get("guildImage"));

		builder.appendDescription("\n**Pvp team composition:** "+team.getJSONObject("Tank").length()+"/"+team.getJSONObject("Healer").length()+"/"+(team.getJSONObject("Ranged Damage").length()+team.getJSONObject("Melee Damage").length()));

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

        return "Successfully updated pvp team!";
    }

}
