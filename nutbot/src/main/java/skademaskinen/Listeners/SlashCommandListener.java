package skademaskinen.Listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;
import skademaskinen.Bot;
import skademaskinen.Commands.Command;
import skademaskinen.Commands.Configure;
import skademaskinen.Commands.Roll;
import skademaskinen.Commands.Team;
import skademaskinen.Commands.Version;

public class SlashCommandListener extends ListenerAdapter{
    
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Bot.getShell().print("""
            Slash Command Event!
            Member: """+event.getUser().getAsTag()+"""

            Guild: """+event.getGuild().getName()+"""

            Command name: """+event.getName()+"""
            """);

        Command command;
        switch(event.getName().toLowerCase()){
            case "version":
                command = new Version();
                break;
            case "roll":
                command = new Roll();
                break;
            case "configure":
                command = new Configure();
                break;
            case "team":
                command = new Team(event);
                break;
            default:
                event.reply("Error, invalid command").queue();
                return;
        }

        if(command.shouldDefer()){
            event.deferReply(command.isEphemeral()).queue();
        }
        Object replyContent = command.execute(event);
        if(replyContent.getClass().equals(ModalImpl.class)){
            event.replyModal((ModalImpl) replyContent).queue();
        }
        else{
            Bot.replyToEvent(event.getHook(), replyContent, command.getActionRows());
        }
    }
}
