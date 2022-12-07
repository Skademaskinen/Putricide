package skademaskinen.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import skademaskinen.Music.Player;

public class Pause implements Command{

    public static CommandData configure(){
        return Commands.slash(Pause.class.getSimpleName().toLowerCase(), "Pause the music bot");
    }

    public Pause(SlashCommandInteractionEvent event) {
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        if(Player.getPlayer(event.getGuild()).getPlayer().isPaused()){
            Player.getPlayer(event.getGuild()).getPlayer().setPaused(false);
            return "Player unpaused!";
        }
        else Player.getPlayer(event.getGuild()).getPlayer().setPaused(true);
        return "Player paused!";
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
    
}
