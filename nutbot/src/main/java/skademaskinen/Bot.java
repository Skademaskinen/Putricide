package skademaskinen;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import skademaskinen.Utils.Config;
import skademaskinen.Utils.Loggable;
import skademaskinen.Commands.*;
import skademaskinen.Listeners.ModalListener;
import skademaskinen.Listeners.SlashCommandListener;

public class Bot implements Loggable{
    private static JDA jda;
    private static Config config;
    private static CommandData[] commands = {Version.configure(), Roll.configure()};
    public static void main(String[] args) {
        new Bot();
    }

    public Bot(){
        try{
            config = new Config();
            jda = JDABuilder.createDefault(config.get("token")).build();
            jda.addEventListener(new SlashCommandListener());
            jda.addEventListener(new ModalListener());
            jda.updateCommands().addCommands(commands).queue();
            log(true, new String[]{});
        }
        catch(Exception e){
            log(false, new String[]{e.getMessage()});

        }
    }

    public static JDA getJda() {
        return jda;
    }
    public static Config getConfig(){
        return config;
    }

    public static void replyToEvent(InteractionHook hook, Object replyContent) {
        Class<?> ContentClass = replyContent.getClass();
        if(ContentClass.equals(String.class)){
            hook.editOriginal((String) replyContent).queue();
        }
        else if(ContentClass.equals(MessageEmbed.class)){
            hook.editOriginalEmbeds((MessageEmbed) replyContent).queue();
        }
        else{
            hook.editOriginal("Error invalid reply class identified by: "+replyContent.getClass().getName()).queue();
        }
    }
}
