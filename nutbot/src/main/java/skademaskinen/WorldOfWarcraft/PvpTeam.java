package skademaskinen.WorldOfWarcraft;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import skademaskinen.Bot;
import skademaskinen.Utils.Loggable;
import skademaskinen.Utils.ServerConfig;
import skademaskinen.Utils.Utils;

/**
 * The PvpTeam manager object, despite the Pvp command having a similar purpose, this class provides abstractions such that the Pvp class can focus on handling the response to the user
 */
public class PvpTeam implements Loggable {
    private static Map<String, Character> characters = new HashMap<>();

    /**
     * This method adds a user and their character to the pvp team
     * @param user The user object representing a discord user
     * @param name The name of the World of Warcraft character they are joining with
     * @param role The role they are planning to be on the pvp team
     * @param server The server this character is from
     * @return
     */
    public static boolean add(Member member, String name, String role, String server, String notes, boolean shouldBench){
        if(!BattleNetAPI.verifyCharacter(name.toLowerCase(), server)){
            return false;
        }
        remove(member);

        JSONObject team = ServerConfig.pvpGet(member.getGuild());
        JSONObject raider = new JSONObject();
        raider.put("name", name);
        raider.put("server", server);
        if(notes != null) raider.put("notes", notes);
        if(shouldBench){
            team.getJSONObject("bench").put(member.getId(), raider.put("role", Utils.capitalize(role)));
        }
        else{
            JSONObject roleobj = team.getJSONObject(Utils.capitalize(role));
            roleobj.put(member.getId(), raider);
        }
        ServerConfig.pvpWrite(member.getGuild(), team);
        characters.put(member.getId(), new Character(name, server));
        update(member.getGuild());
        return true;
    }

    /**
     * This method removes a discord user from the pvp team
     * @param user The user object representing a Discord user
     */
    public static void remove(Member member){
        JSONObject team = ServerConfig.pvpGet(member.getGuild());
        for(String key : team.keySet()){
            if(team.getJSONObject(key).has(member.getId())){
                team.getJSONObject(key).remove(member.getId());
            }
        }
        ServerConfig.pvpWrite(member.getGuild(), team);
        update(member.getGuild());
    }

    /**
     * This method updates the pvp team message, it requires the config file to contain fields:
     * These are the pvp:channel and pvp:message, they are channel and message ids.
     * @return The response for the user, it can be that it is updated successfully or an error message
     */
    public static String update(Guild guild) {
        JSONObject config = ServerConfig.get(guild);
        if(!config.getJSONObject("pvp").has("message") || !config.getJSONObject("pvp").has("channel")){
            return "Failed to update pvp team, the configuration id might be wrong";
        }
        TextChannel channel = guild.getTextChannelById(config.getJSONObject("pvp").getString("channel"));
        Message message = channel.getHistoryAround(config.getJSONObject("pvp").getString("message"), 2).complete().getMessageById(config.getJSONObject("pvp").getString("message"));
        JSONObject team = ServerConfig.pvpGet(guild);
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("Pvp Team!")
            .setDescription("This is the pvp team, this message will get updated with pvp team members!")
            .setImage(config.getString("image"));

		builder.appendDescription("\n**Pvp team composition:** "+team.getJSONObject("Tank").length()+"/"+team.getJSONObject("Healer").length()+"/"+(team.getJSONObject("Ranged Damage").length()+team.getJSONObject("Melee Damage").length()));

        String[] roles = {"Tank", "Healer", "Ranged Damage", "Melee Damage"};

        for(String role : roles){
            String temp = "";
            for(String raiderId : team.getJSONObject(role).keySet()){
                JSONObject raider = team.getJSONObject(role).getJSONObject(raiderId);
                Character character = new Character(raider.getString("name"), raider.getString("server"));
                if(character.failure) return teamError(Bot.getJda().getUserById(raiderId), guild, raider.getString("name"));

                temp+= "\n\n"+ guild.retrieveMemberById(raiderId).complete().getAsMention();
                temp+= "\n"+ Utils.capitalize(character.getName());
                if(!character.getRealm().toLowerCase().replace(" ", "-").equals("argent-dawn")){
                    temp+= " (" + Utils.capitalize(raider.getString("server").replace("-", " ")) + ")";
                }
                temp+= "\n"+character._getClass();
                temp+= "\n"+character.getSpecialization();
                temp+= "\n"+character.getIlvl()+"/"+character.getAverageIlvl()+" ilvl";
                if(raider.has("notes")) temp+="\n**Notes: **"+raider.getString("notes");
            }
            builder.addField(role, temp, true);
            if(role.equals("Healer")) builder.addBlankField(false);
        }
        
        //add benched raiders to the message
        String temp = "";
        for(String raiderId : team.getJSONObject("bench").keySet()){
            JSONObject raider = team.getJSONObject("bench").getJSONObject(raiderId);
            Character character = new Character(raider.getString("name"), raider.getString("server"));

            temp+= "\n\n"+ guild.retrieveMemberById(raiderId).complete().getAsMention();
            if(raider.has("notes")) temp+=" ("+raider.getString("notes")+")";
            temp+= "\n"+ Utils.capitalize(character.getName());
            if(!character.getRealm().toLowerCase().replace(" ", "-").equals(config.getString("realm").toLowerCase().replace(" ", "-"))){
                temp+= " (" + Utils.capitalize(raider.getString("server").replace("-", " ")) + ")";
            }
            temp+= "\n"+character.getSpecialization() + " " + character._getClass();
            temp+= "\n*"+raider.getString("role")+"*";
        }
        if(temp.length() > 0) builder.addField("Bench", temp, false);


        message.editMessageEmbeds(builder.build()).queue();

        return "Successfully updated pvp team!";
    }
    
    private static String teamError(User user, Guild guild, String character) {
        user.openPrivateChannel().complete().sendMessageEmbeds(new EmbedBuilder()
        .setTitle("raid team issue with your character!")
        .setDescription("Your character is causing issues for the pvp team of "+guild.getName()+"\nCharacter name: "+character+"\nGuild server membership: "+(guild.getMemberByTag(user.getAsTag()) != null))
        .build()
        )
        .addActionRow(Button.secondary("Pvp::teamErrorName::"+guild.getId(), "Edit name"), Button.danger("Pvp::teamErrorRemove::"+guild.getId(), "Remove me"))
        .queue();
        return "The following member is invalid: \n"+user.getAsMention();
    }

    public static void editName(Member member, String name) {
        JSONObject team = ServerConfig.pvpGet(member.getGuild());
        for(String key : team.keySet()){
            if(team.getJSONObject(key).has(member.getId())){
                team.getJSONObject(key).getJSONObject(member.getId()).put("name", name);
                break;
            }
        }
        ServerConfig.pvpWrite(member.getGuild(), team);
    }

    public static void editServer(Member member, String server) {
        JSONObject team = ServerConfig.pvpGet(member.getGuild());
        for(String key : team.keySet()){
            if(team.getJSONObject(key).has(member.getId())){
                team.getJSONObject(key).getJSONObject(member.getId()).put("server", server);
                break;
            }
        }
        ServerConfig.pvpWrite(member.getGuild(), team);
    }

    public static void editNote(Member member, String note) {
        JSONObject team = ServerConfig.pvpGet(member.getGuild());
        for(String key : team.keySet()){
            if(team.getJSONObject(key).has(member.getId())){
                if(note.equals("%")){
                    team.getJSONObject(key).getJSONObject(member.getId()).remove("notes");
                    break;
                }
                team.getJSONObject(key).getJSONObject(member.getId()).put("notes", note);
                break;
            }
        }
        ServerConfig.pvpWrite(member.getGuild(), team);
    }

    public static void editRole(Member member, String role) {
        JSONObject team = ServerConfig.pvpGet(member.getGuild());
        for(String key : team.keySet()){
            if(team.getJSONObject(key).has(member.getId())){
                JSONObject userData = team.getJSONObject(key).getJSONObject(member.getId());
                team.getJSONObject(role).put(member.getId(), userData);
                team.getJSONObject(key).remove(member.getId());
                break;
            }
        }
        ServerConfig.pvpWrite(member.getGuild(), team);
    }

    public static void editBench(Member member, boolean shouldBench) {
        JSONObject team = ServerConfig.pvpGet(member.getGuild());
        if(shouldBench){
            for(String key : team.keySet()){
                if(team.getJSONObject(key).has(member.getId())){
                    JSONObject userData = team.getJSONObject(key).getJSONObject(member.getId());
                    team.getJSONObject("bench").put(member.getId(), userData);
                    team.getJSONObject("bench").getJSONObject(member.getId()).put("role", key);
                    team.getJSONObject(key).remove(member.getId());
                }
            }
        }
        else{
            JSONObject userData = team.getJSONObject("bench").getJSONObject(member.getId());
            team.getJSONObject(userData.getString("role")).put(member.getId(), userData);
            team.getJSONObject("bench").remove(member.getId());
        }
        ServerConfig.pvpWrite(member.getGuild(), team);
    }

}
