package skademaskinen.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * This command returns the changelog of the bot
 */
public class Version implements Command {

    private boolean success;


    /**
     * The method to configure a given command, this must be implemented as a static method of each command
     * @return All command data to register a command in discord
     */
    public static CommandData configure(){
        return Commands.slash(Version.class.getSimpleName().toLowerCase(), "Show the changelog of the discord bot.");
    }
    public String run(SlashCommandInteractionEvent event){
        success = true;
        return "```Refactoring work, reimplemented the following features: Version, Roll\nImplemented shell\nreimplemented raid team manager```";
    }

    public Version(SlashCommandInteractionEvent event) {
    }

    public boolean isSuccess(){
        return success;
    }
    @Override
    public boolean requiresAdmin() {
        return false;
    }

}
