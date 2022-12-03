package skademaskinen.Listeners;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skademaskinen.Bot;
import skademaskinen.Commands.Command;
import skademaskinen.Commands.Configure;
import skademaskinen.Utils.Shell;

public class ModalListener extends ListenerAdapter{
    
    public void onModalInteraction(ModalInteractionEvent event) {
        Shell.println("""

            Slash Command Event!
            Member: """+event.getUser().getAsTag()+"""

            Guild: """+event.getGuild().getName()+"""

            Modal ID name: """+event.getModalId()+"""
            """);
        Command command;
        switch(event.getModalId().split("::")[0].toLowerCase()){
            case "configure":
                command = new Configure();
                break;
            default:
                event.reply("Error, invalid command").queue();
                return;
        }

        event.deferReply(command.isEphemeral()).queue();
        Object replyContent = command.ModalExecute(event);
        Bot.replyToEvent(event.getHook(), replyContent);
    }
}
