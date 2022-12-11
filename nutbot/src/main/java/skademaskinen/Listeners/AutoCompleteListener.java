package skademaskinen.Listeners;

import java.lang.reflect.Constructor;
import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skademaskinen.Features.Feature;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

/**
 * This is the autocomplete listener, it handles autocompletion of commands
 */
public class AutoCompleteListener extends ListenerAdapter {
    
    /**
     * This is the autocomplete handler method of this class, it initializes a corresponding command object for the given command, executes its autocomplete method and replies with the result
     * @param event This is the event, it contains a lot of information about how this event happened
     */
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        try{

            Class<?> featureClass = Class.forName("skademaskinen.Features."+Utils.capitalize(event.getName()));
            Constructor<?> constructor = featureClass.getConstructor(CommandAutoCompleteInteractionEvent.class);
            Feature feature = (Feature) constructor.newInstance(new Object[]{event});
            
            List<Choice> choices = feature.execute(event);
            if(choices == null) return;
            event.replyChoices(choices).queue();
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }
}
