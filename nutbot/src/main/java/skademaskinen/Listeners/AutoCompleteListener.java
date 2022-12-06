package skademaskinen.Listeners;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skademaskinen.Commands.Command;
import skademaskinen.Commands.Pvp;
import skademaskinen.Commands.Raid;
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
        Command command;
        switch(event.getName()){
            case "raid":
                command = new Raid(event);
                break;
            case "pvp":
                command = new Pvp(event);
                break;
            default:
                return;
        }
        List<Choice> choices = command.AutoComplete(event);
        event.replyChoices(choices).queue();
    }
}
