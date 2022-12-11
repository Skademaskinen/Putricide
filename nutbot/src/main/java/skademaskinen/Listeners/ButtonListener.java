package skademaskinen.Listeners;

import java.lang.reflect.Constructor;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;
import skademaskinen.Bot;
import skademaskinen.Features.Feature;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;

/**
 * This is the listener class for button events, it handles whenever a button has been pressed in discord
 */
public class ButtonListener extends ListenerAdapter {

    /**
     * This is the button handler method, it initializes a command object corresponding to the button pressed, executes it and writes the result as a reply
     * @param event This is the event, it contains a lot of information about how this event happened
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Shell.println(Shell.green("Button event: "));
        Shell.println(Shell.yellow("Timestamp:    ")+Utils.timestamp());
        Shell.println(Shell.yellow("Guild:        ")+event.getGuild().getName());
        Shell.println(Shell.yellow("Member:       ")+event.getUser().getAsTag());
        Shell.println(Shell.yellow("Button ID:    ")+event.getComponentId());

        try{

            Class<?> featureClass = Class.forName("skademaskinen.Features."+Utils.capitalize(event.getButton().getId().split("::")[0]));
            Constructor<?> constructor = featureClass.getConstructor(ButtonInteractionEvent.class);
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
                replyContent = feature.execute(event);
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
}
