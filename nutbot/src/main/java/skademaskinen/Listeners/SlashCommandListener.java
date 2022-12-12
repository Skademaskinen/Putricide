package skademaskinen.Listeners;

import java.lang.reflect.Constructor;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;
import skademaskinen.Bot;
import skademaskinen.Features.Feature;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;

/**
 * This class listens for slash commands and handles them accordingly
 */
public class SlashCommandListener extends ListenerAdapter{
    
    /**
     * This method is the listener for slash commands, it initializes a command object for the given type of command, executes that command and replies the result of that command in discord
     * @param event This is the event object specified in the JDA library
     */
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Shell.println(Shell.green("Slash command event:  "));
        Shell.println(Shell.yellow("Timestamp:     ")+Utils.timestamp());
        Shell.println(Shell.yellow("Guild:         ")+event.getGuild().getName());
        Shell.println(Shell.yellow("Member:        ")+event.getUser().getAsTag());
        Shell.println(Shell.yellow("Command ID:    ")+event.getName());
        if(event.getSubcommandName() != null) Shell.println(Shell.yellow("Subcommand ID: ")+event.getSubcommandName());
        for(OptionMapping option : event.getOptions()) Shell.println(Shell.yellow("option("+option.getName()+"): ")+option.getAsString());

        try{
            Class<?> featureClass = Class.forName("skademaskinen.Features."+Utils.capitalize(event.getName()));
            Constructor<?> constructor = featureClass.getConstructor(SlashCommandInteractionEvent.class);
            Feature feature = (Feature) constructor.newInstance(new Object[]{event});

            if(feature.requiresAdmin() && !event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                event.reply("Error, you are not an administrator!").setEphemeral(true).queue();
                return;
            }
    
            if(feature.shouldDefer()){
                event.deferReply(feature.isEphemeral()).queue();
            }
            Object replyContent;
            try{
                replyContent = feature.run(event);
                if(replyContent == null) if(feature.shouldDefer()) event.getHook().editOriginal("An unhandled error occured, contact Mast3r_waf1z#0420 for more info").queue();
                else event.reply("An unhandled error occured, contact Mast3r_waf1z#0420 for more info").queue();
            }
            catch(Exception e){
                Shell.exceptionHandler(e);
                if(feature.shouldDefer()){
                    event.getHook().editOriginal(e.getMessage()).queue();
                }
                else{
                    event.reply(e.getMessage()).queue();
                }
                return;
            }

            if(replyContent.getClass().equals(ModalImpl.class)){
                event.replyModal((ModalImpl) replyContent).queue();
            }
            else{
                Bot.replyToEvent(event.getHook(), replyContent, feature.getActionRows());
            }
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }

        
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(Bot.getShell().isInChannel(event.getGuild(), event.getChannel())){
            Shell.println(Shell.cyan(event.getMember().getUser().getAsTag())+": \n"+event.getMessage().getContentDisplay());
        }
    }
}
