package skademaskinen.Listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import skademaskinen.Bot;
import skademaskinen.Commands.Command;
import skademaskinen.Commands.Configure;
import skademaskinen.Commands.Pvp;
import skademaskinen.Commands.Raid;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;

public class ModalListener extends ListenerAdapter{
    
    public void onModalInteraction(ModalInteractionEvent event) {
        Shell.println(Shell.green("Modal event: "));
        Shell.println(Shell.yellow("Timestamp:    ")+Utils.timestamp());
        Shell.println(Shell.yellow("Guild:        ")+event.getGuild().getName());
        Shell.println(Shell.yellow("Member:       ")+event.getUser().getAsTag());
        Shell.println(Shell.yellow("Modal ID:     ")+event.getModalId());
        for(ModalMapping mapping : event.getValues()) Shell.println(Shell.yellow("value("+mapping.getId()+"): ")+mapping.getAsString());


        Command command;
        switch(event.getModalId().split("::")[0].toLowerCase()){
            case "configure":
                command = new Configure();
                break;
            case "raid":
                command = new Raid(event);
                break;
            case "pvp":
                command = new Pvp(event);
                break;
            default:
                event.reply("Error, invalid command").queue();
                return;
        }

        if(command.requiresAdmin() && !event.getMember().hasPermission(Permission.ADMINISTRATOR)){
            event.reply("Error, you are not an administrator!").setEphemeral(true).queue();
            return;
        }

        if(command.shouldDefer()){
            event.deferReply(command.isEphemeral()).queue();

        }
        Object replyContent = command.ModalExecute(event);
        Bot.replyToEvent(event.getHook(), replyContent, command.getActionRows());
    }
}
