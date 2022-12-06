package skademaskinen.Listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;
import skademaskinen.Bot;
import skademaskinen.Commands.Command;
import skademaskinen.Commands.Pvp;
import skademaskinen.Commands.Raid;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Shell.println(Shell.green("Button event: "));
        Shell.println(Shell.yellow("Timestamp:    ")+Utils.timestamp());
        Shell.println(Shell.yellow("Guild:        ")+event.getGuild().getName());
        Shell.println(Shell.yellow("Member:       ")+event.getUser().getAsTag());
        Shell.println(Shell.yellow("Button ID:    ")+event.getComponentId());

        Command command;
        switch(event.getButton().getId().split("::")[0].toLowerCase()){
            case "raid":
                command = new Raid(event);
                break;
            case "pvp":
                command = new Pvp(event);
                break;
            default:
                event.reply("Error, invalid button").setEphemeral(true).queue();
                return;
        }
        if(command.requiresAdmin() && !event.getMember().hasPermission(Permission.ADMINISTRATOR)){
            event.reply("Error, you are not an administrator!").setEphemeral(true).queue();
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
