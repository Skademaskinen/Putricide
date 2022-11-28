package skademaskinen.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Version implements Command {

    private boolean success;

    public static CommandData configure(){
        return Commands.slash(Version.class.getSimpleName().toLowerCase(), "Show the changelog of the discord bot.");
    }
    public String run(SlashCommandInteractionEvent event){
        success = true;
        return "```Refactoring work, reimplemented the following features: Version, Roll```";
    }

    public boolean isSuccess(){
        return success;
    }

}
