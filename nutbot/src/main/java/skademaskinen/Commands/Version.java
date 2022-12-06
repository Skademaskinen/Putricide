package skademaskinen.Commands;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;

public class Version implements Command {

    private boolean success;

    public static CommandData configure(){
        return Commands.slash(Version.class.getSimpleName().toLowerCase(), "Show the changelog of the discord bot.");
    }
    public String run(SlashCommandInteractionEvent event){
        success = true;
        return "```Refactoring work, reimplemented the following features: Version, Roll\nImplemented shell\nreimplemented raid team manager```";
    }

    @Override
    public List<ActionRow> getActionRows() {
        return null;
    }

    public boolean isSuccess(){
        return success;
    }
    @Override
    public boolean requiresAdmin() {
        return false;
    }

}
