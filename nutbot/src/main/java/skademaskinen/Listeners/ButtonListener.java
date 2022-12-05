package skademaskinen.Listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;
import skademaskinen.Bot;
import skademaskinen.Commands.Command;
import skademaskinen.Commands.Team;
import skademaskinen.Utils.Shell;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Shell.println("""

            Button Event!
            Member: """+event.getUser().getAsTag()+"""

            Guild: """+event.getGuild().getName()+"""

            Button ID name: """+event.getButton().getId()+"""
            """);

        Command command;
        switch(event.getButton().getId().split("::")[0].toLowerCase()){
            case "team":
                command = new Team(event);
                break;
            default:
                event.reply("Error, invalid button").setEphemeral(true).queue();
                return;
        }

        if(command.shouldDefer()){
            event.deferReply(command.isEphemeral()).queue();
        }
        Object replyContent = command.ButtonExecute(event);
        if(replyContent.getClass().equals(ModalImpl.class)){
            event.replyModal((ModalImpl) replyContent).queue();
        }
        else{
            Bot.replyToEvent(event.getHook(), replyContent, command.getActionRows());
        }

    }
}
