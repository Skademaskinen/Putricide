package skademaskinen.Commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import skademaskinen.Music.Player;
import skademaskinen.Utils.Utils;

public class Playing implements Command {

    public static CommandData configure(){
        return Commands.slash(Playing.class.getSimpleName().toLowerCase(), "Show the currently playing track");
    }

    public Playing(SlashCommandInteractionEvent event) {

    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        AudioTrack track = Player.getPlayer(event.getGuild()).getPlayer().getPlayingTrack();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Currently playing track");
        builder.setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")\n");
        builder.setThumbnail("http://img.youtube.com/vi/"+track.getIdentifier()+"/0.jpg");
        builder.setFooter("Position: " + Utils.getTime(track.getPosition())+"/"+Utils.getTime(track.getDuration()));
        return builder.build();
        
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
    
}
