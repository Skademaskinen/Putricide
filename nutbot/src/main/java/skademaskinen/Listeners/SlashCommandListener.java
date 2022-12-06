package skademaskinen.Listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;
import skademaskinen.Bot;
import skademaskinen.Commands.Command;
import skademaskinen.Commands.Configure;
import skademaskinen.Commands.Pvp;
import skademaskinen.Commands.Roll;
import skademaskinen.Commands.Raid;
import skademaskinen.Commands.Version;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;

public class SlashCommandListener extends ListenerAdapter{
    
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Shell.println(Shell.green("Slash command event:  "));
        Shell.println(Shell.yellow("Timestamp:     ")+Utils.timestamp());
        Shell.println(Shell.yellow("Guild:         ")+event.getGuild().getName());
        Shell.println(Shell.yellow("Member:        ")+event.getUser().getAsTag());
        Shell.println(Shell.yellow("Command ID:    ")+event.getName());
        if(event.getSubcommandName() != null) Shell.println(Shell.yellow("Subcommand ID: ")+event.getSubcommandName());
        for(OptionMapping option : event.getOptions()) Shell.println(Shell.yellow("option("+option.getName()+"): ")+option.getAsString());

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
        Object replyContent;
        try{
            replyContent = command.run(event);
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
            if(command.shouldDefer()){
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
            Bot.replyToEvent(event.getHook(), replyContent, command.getActionRows());
        }
    }
}
