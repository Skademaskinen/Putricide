package skademaskinen.Listeners;

import java.lang.reflect.Constructor;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import skademaskinen.Bot;
import skademaskinen.Features.Feature;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;

/**
 * This is the modal listener class, it handles modals sent to the bot
 */
public class ModalListener extends ListenerAdapter{
    
    /**
     * This is the listener method, it handles modal interaction events by initializing a corresponding command object and executing its modal execution method and prints its result
     * @param event This is the event, it contains a lot of information about how this event happened
     */
    public void onModalInteraction(ModalInteractionEvent event) {
        Shell.println(Shell.green("Modal event: "));
        Shell.println(Shell.yellow("Timestamp:    ")+Utils.timestamp());
        if(event.isFromGuild()) Shell.println(Shell.yellow("Guild:        ")+event.getGuild().getName());
        Shell.println(Shell.yellow("Member:       ")+event.getUser().getAsTag());
        Shell.println(Shell.yellow("Modal ID:     ")+event.getModalId());
        for(ModalMapping mapping : event.getValues()) Shell.println(Shell.yellow("value("+mapping.getId()+"): ")+mapping.getAsString());

        try{

            Class<?> featureClass = Class.forName("skademaskinen.Features."+Utils.capitalize(event.getModalId().split("::")[0]));
            Constructor<?> constructor = featureClass.getConstructor(ModalInteractionEvent.class);
            Feature feature = (Feature) constructor.newInstance(new Object[]{event});
            
            if(!event.isFromGuild());
            else if(feature.requiresAdmin() && !event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                event.reply("Error, you are not an administrator!").setEphemeral(true).queue();
                return;
            }
            
            if(feature.shouldDefer()){
                event.deferReply(feature.isEphemeral()).queue();
                
            }
            if(feature.shouldDeferEdit()){
                event.deferEdit().queue();
                feature.execute(event);
                return;
            }
            Object response;
            try{
                response = feature.execute(event);
                if(response == null) if(feature.shouldDefer()) event.getHook().editOriginal("An unhandled error occured, contact Mast3r_waf1z#0420 for more info").queue();
                else event.reply("An unhandled error occured, contact Mast3r_waf1z#0420 for more info").queue();
            }
            catch(Exception e){
                Shell.exceptionHandler(e, event.getGuild());
                if(feature.shouldDefer()){
                    event.getHook().editOriginal(e.getMessage()).queue();
                }
                else{
                    event.reply(e.getMessage()).queue();
                }
                return;
            }
            Bot.replyToEvent(event.getHook(), response, feature.getActionRows());
        }
        catch(Exception e){
            Shell.exceptionHandler(e, event.getGuild());
        }
    }
}
