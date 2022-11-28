package skademaskinen.WorldOfWarcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class RaidTeam implements Loggable {
    private static String filepath = "files/team.json";
    private static Map<String, Character> characters = new HashMap<>();

    public static boolean add(User user, String name, String role, String server){
        if(!verifyCharacter(name, server)){
            return false;
        }
        JSONObject team = Utils.readJSON(filepath);
        Map<String, Object> raider = new HashMap<>();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        raider.put("name", name);
        raider.put("server", server);
        raider.put("role", role);
        team.put(user.getId(), raider);
        Utils.writeJSON(filepath, team);
        characters.put(user.getId(), new Character(name, server));
        update();
        return true;
    }

    public static void remove(User user){
        JSONObject team = Utils.readJSON(filepath);
        team.remove(user.getId());
        Utils.writeJSON(filepath, team);
    }

    @SuppressWarnings("null")
    public static String update() {
        if(Bot.getConfig().get("teamMessageId") == null || Bot.getConfig().get("teamMessageChannelId") == null){
            return "Failed to update raid team, the configuration id might be wrong";
        }
        Guild guild = Bot.getJda().getGuildById(Bot.getConfig().get("guildId"));
        TextChannel channel = guild.getTextChannelById(Bot.getConfig().get("teamMessageChannelId"));
        Message message = channel.getHistoryAround(Bot.getConfig().get("teamMessageId"), 2).complete().getMessageById(Bot.getConfig().get("teamMessageId"));
        JSONObject team = Utils.readJSON(filepath);
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("Raid Team!")
            .setDescription("This is the raid team, this message will get updated with raid team members!")
            .setImage(Bot.getConfig().get("guildImage"));

        List<String> tanks = new ArrayList<>();
        List<String> healers = new ArrayList<>();
        List<String> ranged = new ArrayList<>();
        List<String> melee = new ArrayList<>();
        for(String key : team.keySet()){
            JSONObject raider = team.getJSONObject(key);
            switch(raider.getString("role").toLowerCase()){
                case "melee damage":
                    melee.add(key);
                    break;
                case "ranged damage":
                    ranged.add(key);
                    break;
                case "tank":
                    tanks.add(key);
                    break;
                case "healer":
                    healers.add(key);
                    break;
            }
        }

		builder.appendDescription("\n**Raid team composition:** "+tanks.size()+"/"+healers.size()+"/"+(ranged.size()+melee.size()));

        String tanksMessage = "";
        for(String key : tanks){
            JSONObject raider = team.getJSONObject(key);
			tanksMessage+= "\n\n" + guild.retrieveMemberById(key).complete().getAsMention();
			tanksMessage+= "\n" + raider.get("name");
			if(!raider.get("server").toString().equalsIgnoreCase("argent-dawn")){
				tanksMessage+= " (" + raider.get("server") + ")";
			}
        }
        builder.addField("Tanks:", tanksMessage, true);

        String healersMessage = "";
        for(String key : healers){
            JSONObject raider = team.getJSONObject(key);
			healersMessage+= "\n\n" + guild.retrieveMemberById(key).complete().getAsMention();
			healersMessage+= "\n" + raider.get("name");
			if(!raider.get("server").toString().equalsIgnoreCase("argent-dawn")){
				healersMessage+= " (" + raider.get("server") + ")";
			}
        }
        builder.addField("Healers:", healersMessage, true);
		builder.addBlankField(false); 

        String rangedMessage = "";
        for(String key : ranged){
            JSONObject raider = team.getJSONObject(key);
			rangedMessage+= "\n\n" + guild.retrieveMemberById(key).complete().getAsMention();
			rangedMessage+= "\n" + raider.get("name");
			if(!raider.get("server").toString().equalsIgnoreCase("argent-dawn")){
				rangedMessage+= " (" + raider.get("server") + ")";
			}
        }
        builder.addField("Ranged Damage:", rangedMessage, true);

        String meleeMessage = "";
        for(String key : melee){
            JSONObject raider = team.getJSONObject(key);
			meleeMessage+= "\n\n" + guild.retrieveMemberById(key).complete().getAsMention();
			meleeMessage+= "\n" + raider.get("name");
			if(!raider.get("server").toString().equalsIgnoreCase("argent-dawn")){
				meleeMessage+= " (" + raider.get("server") + ")";
			}
        }
        builder.addField("Melee Damage:", meleeMessage, true);

        message.editMessageEmbeds(builder.build()).queue();


        return "Successfully updated raid team!";
    }

    private static boolean verifyCharacter(String name, String server) {
        return false;
    }
}
