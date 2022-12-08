package skademaskinen.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import skademaskinen.Music.Player;

public class Disconnect implements Command {

    public static CommandData configure(){
        return Commands.slash(Disconnect.class.getSimpleName().toLowerCase(), "Disconnects the music bot from a voice channel");
    }

    public Disconnect(SlashCommandInteractionEvent event) {
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        Player.getPlayer(event.getGuild()).disconnect(event.getGuild());
        return "Successfully disconnected bot!";
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
    
}
