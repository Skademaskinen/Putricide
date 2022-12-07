package skademaskinen.Commands;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
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
        command.addOption(OptionType.BOOLEAN, "soundcloud", "Should the bot search soundcloud", false);
        return command;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    public Play(SlashCommandInteractionEvent event){
        if(!Player.isInitialized(event.getGuild())){
            Player.initialize(event.getMember().getVoiceState().getChannel().asVoiceChannel());
        }
        if(!event.getGuild().getAudioManager().isConnected()){
            Player.getPlayer(event.getGuild()).connect(event.getMember().getVoiceState().getChannel().asVoiceChannel());
        }
    }

    public Play(CommandAutoCompleteInteractionEvent event){
        if(!Player.isInitialized(event.getGuild())){
            Player.initialize(event.getMember().getVoiceState().getChannel().asVoiceChannel());
        }
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        String searchTerm = event.getOption("track").getAsString();
        if(!Utils.isURLValid(searchTerm)){
            if(event.getOption("soundcloud") != null && event.getOption("soundcloud").getAsBoolean()){
                searchTerm = "scsearch"+searchTerm;
            }
            else{
                searchTerm = "ytsearch"+searchTerm;
            }

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

    @Override
    public List<Choice> AutoComplete(CommandAutoCompleteInteractionEvent event) {
        String searchTerm = event.getFocusedOption().getValue();
        if(event.getOption("soundcloud") != null && event.getOption("soundcloud").getAsBoolean()){
            searchTerm = "scsearch:"+searchTerm;
        }
        else{
            searchTerm = "ytsearch:"+searchTerm;
        }
        AudioPlaylist playlist = Player.getPlayer(event.getGuild()).searchQuery(searchTerm);
        if(playlist == null) return null;
        List<Choice> choices = new ArrayList<>();
        for(AudioTrack track : playlist.getTracks()){
            choices.add(new Choice(track.getInfo().title, track.getInfo().uri));
            if(choices.size() == 25) break;
        }
        return choices;
    }
    
}
