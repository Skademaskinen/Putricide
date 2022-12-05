package skademaskinen;

import java.util.List;

import org.json.JSONObject;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import skademaskinen.Utils.Config;
import skademaskinen.Utils.Loggable;
import skademaskinen.Utils.Shell;
import skademaskinen.WorldOfWarcraft.BattleNetAPI;
import skademaskinen.WorldOfWarcraft.PvpTeam;
import skademaskinen.WorldOfWarcraft.RaidTeam;
import skademaskinen.Commands.*;
import skademaskinen.Listeners.ButtonListener;
import skademaskinen.Listeners.ModalListener;
import skademaskinen.Listeners.SlashCommandListener;

public class Bot implements Loggable{
    private static Config config;
    private static JDA jda;
    private static Shell shell;
    private static CommandData[] commands = {Version.configure(), Roll.configure(), Configure.configure(), Raid.configure(), Pvp.configure(), skademaskinen.Commands.Message.configure()};
    public static void main(String[] args) {
        String accessToken = new JSONObject(args[0]).getString("access_token");
        new Bot(accessToken);
    }

    public Bot(String token){
        try{
            config = new Config();
            jda = JDABuilder.createDefault(config.get("token")).build();
            jda.addEventListener(new SlashCommandListener());
            jda.addEventListener(new ModalListener());
            jda.addEventListener(new ButtonListener());
            jda.updateCommands().addCommands(commands).queue();
            shell = new Shell();
            BattleNetAPI.init(token);
            jda.awaitReady();
            RaidTeam.update();
            PvpTeam.update();
            //jda.getGuildById("692410386657574952").getTextChannelById("1046840206562709514").sendMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
            new Thread(shell).start();
            log(true, new String[]{});
        }
        catch(Exception e){
            log(false, new String[]{e.getMessage()});
            Shell.exceptionHandler(e);

        }
    }

    public static JDA getJda() {
        return jda;
    }
    public static Config getConfig(){
        return config;
    }
    public static Shell getShell(){
        return shell;
    }

    public static void replyToEvent(InteractionHook hook, Object replyContent, List<ActionRow> actionRows) {
        Class<?> ContentClass = replyContent.getClass();
        WebhookMessageEditAction<Message> action;
        if(ContentClass.equals(String.class)){
            action = hook.editOriginal((String) replyContent);
        }
        else if(ContentClass.equals(MessageEmbed.class)){
            action = hook.editOriginalEmbeds((MessageEmbed) replyContent);
        }
        else{
            action = hook.editOriginal("Error invalid reply class identified by: "+replyContent.getClass().getName());
        }
        if(actionRows != null){
            action.setComponents(actionRows);
        }
        action.queue();
    }
}
