package skademaskinen.Commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import skademaskinen.Music.Player;
import skademaskinen.Utils.Utils;

public class Play implements Command{
    private boolean success;

    public static CommandData configure(){
        SlashCommandData command = Commands.slash(Play.class.getSimpleName().toLowerCase(), "Play a track or playlist on the musicbot");
        command.addOption(OptionType.STRING, "track", "URL or search term", true, true);
        return command;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public Play(SlashCommandInteractionEvent event){
        if(!Player.isInitialized(event.getGuild())){
            Player.initialize(event.getMember().getVoiceState().getChannel().asVoiceChannel());
        }
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        String searchTerm = event.getOption("track").getAsString();
        if(!Utils.isURLValid(searchTerm)){
            searchTerm = "ytsearch:"+searchTerm;
        }
        AudioTrack track = Player.getPlayer(event.getGuild()).enqueue(searchTerm);
        if(track == null) return "Error, failed to load track!";
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Successfully queued track!");
        builder.setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")");
        builder.setThumbnail("http://img.youtube.com/vi/"+track.getIdentifier()+"/0.jpg");
        builder.setFooter("Duration: " + Utils.getTime(track.getDuration()));
        return builder.build();
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
    
}
