package skademaskinen;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;
import skademaskinen.Utils.GlobalConfig;
import skademaskinen.Utils.Loggable;
import skademaskinen.Utils.ServerConfig;
import skademaskinen.Utils.Shell;
import skademaskinen.WorldOfWarcraft.BattleNetAPI;
import skademaskinen.WorldOfWarcraft.PvpTeam;
import skademaskinen.WorldOfWarcraft.RaidTeam;
import skademaskinen.Listeners.AutoCompleteListener;
import skademaskinen.Listeners.ButtonListener;
import skademaskinen.Listeners.LoggingListener;
import skademaskinen.Listeners.ModalListener;
import skademaskinen.Listeners.SelectMenuListener;
import skademaskinen.Listeners.SlashCommandListener;

/**
 * The main class of The Nut Bot
 * It handles class abstractions and handles the main api, it also handles initialization.
 */
public class Bot implements Loggable{
    private static JDA jda;
    private static Shell shell;
    private static List<CommandData> commands;
    public static Bot bot;
    public Map<String, Boolean> args;
    /**
     * The main method of the software, this method initializes everything and runs it.
     * @param args command line arguments that are passed after compilation, args[0] is always the access token for blizzard servers
     */
    public static void main(String[] args) {
        
        Bot.bot = new Bot(args);
    }

    private static List<CommandData> generateFeatures() {
        File[] files = new File("ppbot/src/main/java/skademaskinen/Features").listFiles();
        List<CommandData> result = new ArrayList<>();
        for(File file : files){
            if(file.getName().equals("Feature.java")) continue;
            try {
                Class<?> featureClass = Class.forName("skademaskinen.Features."+file.getName().replace(".java", ""));
                //Shell.println("Initializing command: "+ commandClass.getSimpleName());
                Method method = featureClass.getMethod("configure");
                result.add((CommandData) method.invoke(featureClass));
            } 
            catch (Exception e) {
                Shell.exceptionHandler(e, jda.getGuildById("692410386657574952"));
                if(e.getClass().equals(InvocationTargetException.class)){
                    Shell.exceptionHandler(((InvocationTargetException)e).getTargetException(), jda.getGuildById("692410386657574952"));
                }
            }
        }
        return result;
    }

    /**
     * The constructor, it is used to ensure that the main method never just throws exceptions but logs them instead
     * @param token The access token for the blizzard servers
     */

    public Bot(String[] args){
        try{
            for(String arg : args){
                switch(arg){
                    case "--disable-teams":
                        this.args.put("teams", false);
                }
            }
            jda = JDABuilder.createDefault(GlobalConfig.get().getString("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .build();
            jda.addEventListener(new SlashCommandListener());
            jda.addEventListener(new ModalListener());
            jda.addEventListener(new ButtonListener());
            jda.addEventListener(new AutoCompleteListener());
            jda.addEventListener(new LoggingListener());
            jda.addEventListener(new SelectMenuListener());
            shell = new Shell();
            BattleNetAPI.init();
            jda.awaitReady();
            jda.getPresence().setActivity(Activity.competing(GlobalConfig.get().getString("status")));
            commands = generateFeatures();
            jda.updateCommands().addCommands(commands).queue();
            //verify that all servers are configured
            for(Guild guild : jda.getGuilds()) updateServerConfig(guild);

            //exceptionTester();
            //jda.getGuildById("692410386657574952").getTextChannelById("1046840206562709514").sendMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
            new Thread(shell).start();
            log(true, new String[]{});
        }
        catch(Exception e){
            log(false, new String[]{e.getMessage()});
            Shell.exceptionHandler(e);

        }
    }

    /**
     * Getter for the api object of the Discord Java API
     * @return the initialized jda object of type JDA
     */
    public static JDA getJda() {
        return jda;
    }

    /**
     * The getter for the shell object, this is used to ensure initialization
     * @return The shell object of type Shell
     */
    public static Shell getShell(){
        return shell;
    }

    /**
     * This method is used to ensure consistency in replies to events across the interaction events for the bot, they always call this method at the end
     * @param hook The InteractionHook object, this is a callback that we can use to edit already sent acknowledgements to interactions
     * @param replyContent The content to add to this reply, it can have type MessageEmbed or String
     * @param actionRows If there is any actionRows this variable can be used, or null if there are no actionrows.
     */
    public static void replyToEvent(InteractionHook hook, Object replyContent, List<ActionRow> actionRows) {
        Class<?> ContentClass = replyContent.getClass();
        WebhookMessageEditAction<Message> action;
        if(ContentClass.equals(String.class)){
            action = hook.editOriginal((String) replyContent);
        }
        else if(ContentClass.equals(MessageEmbed.class)){
            action = hook.editOriginalEmbeds((MessageEmbed) replyContent);
        }
        else if(ContentClass.equals(FileUpload.class)){
            action = hook.editOriginalAttachments((FileUpload)replyContent);
        }
        else{
            action = hook.editOriginal("Error invalid reply class identified by: "+replyContent.getClass().getName());
        }
        if(actionRows != null){
            action.setComponents(actionRows);
        }
        action.queue();
    }

    public static void updateServerConfig(Guild guild) throws Exception{
        File configPath = new File("files/config/"+guild.getId());
        File config = new File(configPath.getPath()+"/config.json");
        File rolepicker = new File(configPath.getPath()+"/rolepicker.json");
        File pvp = new File(configPath.getPath()+"/pvp.json");
        File raid = new File(configPath.getPath()+"/raid.json");
        if(!configPath.exists()) configPath.mkdir();
        if(!config.exists()) if(config.createNewFile()){
            try(FileWriter writer = new FileWriter(config)){
                TextChannel firstChannel = guild.getTextChannels().get(0);
                writer.write(new JSONObject()
                    .put("image", "https://www.interprint-services.co.uk/wp-content/uploads/2019/04/placeholder-banner.png")
                    .put("name", "Placeholder")
                    .put("realm", "Placeholder")
                    .put("region", "Placeholder")
                    .put("channels", new JSONObject()
                        .put("log", firstChannel.getId())
                        .put("announcements", firstChannel.getId())
                        .put("issues", firstChannel.getId()))
                    .put("raid", new JSONObject()
                        .put("filled", new JSONArray())
                        .put("preferred", new JSONArray())
                        .put("ilvl", 370)
                        .put("message", "placeholder")
                        .put("channel", firstChannel.getId()))
                    .put("pvp", new JSONObject()
                        .put("filled", new JSONArray())
                        .put("preferred", new JSONArray())
                        .put("ilvl", 370)
                        .put("message", "placeholder")
                        .put("channel", firstChannel.getId()))
                    .toString(4));
            }
        }
        if(!rolepicker.exists()) if(rolepicker.createNewFile()){
            try(FileWriter writer = new FileWriter(rolepicker)){
                writer.write(new JSONObject()
                    .put("configuration", new JSONObject()
                        .put("image", "https://www.interprint-services.co.uk/wp-content/uploads/2019/04/placeholder-banner.png")
                        .put("description", "Placeholder")
                        .put("title", "Rolepicker"))
                    .put("categories", new JSONObject())    
                    .toString(4));
            }
        }
        if(!pvp.exists()) if(pvp.createNewFile()){
            try(FileWriter writer = new FileWriter(pvp)){
                writer.write(new JSONObject()
                    .put("Ranged Damage", new JSONObject())
                    .put("Melee Damage", new JSONObject())
                    .put("Tank", new JSONObject())
                    .put("Healer", new JSONObject())
                    .put("bench", new JSONObject())
                    .toString(4));
            }
        }
        if(!raid.exists()) if(raid.createNewFile()){
            try(FileWriter writer = new FileWriter(raid)){
                writer.write(new JSONObject()
                    .put("Ranged Damage", new JSONObject())
                    .put("Melee Damage", new JSONObject())
                    .put("Tank", new JSONObject())
                    .put("Healer", new JSONObject())
                    .put("bench", new JSONObject())
                    .toString(4));
            }
        }
        if(!ServerConfig.get(guild).getJSONObject("pvp").getString("message").equals("placeholder")) PvpTeam.update(guild, false);
        if(!ServerConfig.get(guild).getJSONObject("raid").getString("message").equals("placeholder")) RaidTeam.update(guild, false);

    }

    public static void exceptionTester(){
        try {
            throw new Exception();
        } catch (Exception e) {
            Shell.exceptionHandler(e, jda.getGuildById("692410386657574952"));
        }
    }
}
