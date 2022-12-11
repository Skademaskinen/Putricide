package skademaskinen.Commands;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import skademaskinen.Music.Player;
import skademaskinen.Utils.Utils;

public class Music implements Command {
    private boolean isEphemeral = true;
    private boolean success = false;

    public static CommandData configure(){
        SlashCommandData data = Commands.slash(Music.class.getSimpleName().toLowerCase(), "Control the music bot");

        SubcommandData play = new SubcommandData("play", "Play a song")
            .addOption(OptionType.STRING, "track", "URL or search term", true, true)
            .addOption(OptionType.BOOLEAN, "soundcloud", "Should the bot search soundcloud", false)
            .addOption(OptionType.BOOLEAN, "top", "Should this song be placed at the top of the queue?", false);
        SubcommandData queue = new SubcommandData("queue", "Show the track queue!");
        SubcommandData pause = new SubcommandData("pause", "Pause or unpause the music bot");
        SubcommandData playing = new SubcommandData("playing", "Show the currently playing track");
        SubcommandData skip = new SubcommandData("skip", "Skip a song")
            .addOption(OptionType.STRING, "index", "The index in the queue to skip");
        SubcommandData disconnect = new SubcommandData("disconnect", "Disconnect the music bot");
        SubcommandData loop = new SubcommandData("loop", "Loop the currently playing track");

        data.addSubcommands(play, queue, pause, playing, skip, disconnect, loop);
        return data;
    }

    public Music(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())){
            Player.initialize(event.getMember().getVoiceState().getChannel().asVoiceChannel());
        }
        switch(event.getSubcommandName()){
            case "play":
                isEphemeral = false;
                if(!event.getGuild().getAudioManager().isConnected()){
                    Player.getPlayer(event.getGuild()).connect(event.getMember().getVoiceState().getChannel().asVoiceChannel());
                }
                break;
        }
    }

    public Music(CommandAutoCompleteInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild()) && event.getMember().getVoiceState().getGuild().equals(event.getGuild())){
            Player.initialize(event.getMember().getVoiceState().getChannel().asVoiceChannel());
        }
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
    @Override
    public boolean requiresAdmin() {
        return false;
    }
    @Override
    public boolean isEphemeral() {
        return isEphemeral;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        switch(event.getSubcommandName()){
            case "play":
                return play(event);
            case "queue":
                return queue(event);
            case "pause":
                return pause(event);
            case "playing":
                return playing(event);
            case "skip":
                return skip(event);
            case "disconnect":
                return disconnect(event);
            case "loop":
                return loop(event);
            default:
                return "Error, invalid command";
        }
    }

    @Override
    public List<Choice> AutoComplete(CommandAutoCompleteInteractionEvent event) {
        switch(event.getSubcommandName()){
            case "play":
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
                success = true;
                return choices;
            default:
                success = false;
                return null;
        }
    }


    private Object play(SlashCommandInteractionEvent event) {
        String searchTerm = event.getOption("track").getAsString();
        if(!Utils.isURLValid(searchTerm)){
            if(event.getOption("soundcloud") != null && event.getOption("soundcloud").getAsBoolean()){
                searchTerm = "scsearch"+searchTerm;
            }
            else{
                searchTerm = "ytsearch"+searchTerm;
            }
        }
        AudioTrack track;
        if(event.getOption("top") != null && event.getOption("top").getAsBoolean()) track = Player.getPlayer(event.getGuild()).put(0, searchTerm);
        else track = Player.getPlayer(event.getGuild()).enqueue(searchTerm);
        if(track == null) return "Error, failed to load track!";
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Successfully queued track!");
        builder.setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")");
        builder.setThumbnail("http://img.youtube.com/vi/"+track.getIdentifier()+"/0.jpg");
        builder.setFooter("Duration: " + Utils.getTime(track.getDuration()));
        success = true;
        return builder.build();
    }

    private Object queue(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The queue is empty!";
        List<AudioTrack> queue = Player.getPlayer(event.getGuild()).getQueue();
        if(queue.size() == 0) return "The queue is empty!";
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Track queue!");
        builder.setThumbnail("http://img.youtube.com/vi/"+queue.get(0).getIdentifier()+"/0.jpg");
        for(int i = 0; i < (queue.size() <= 25 ? queue.size() : 25); i++){
            builder.appendDescription("**["+(i+1)+"]:** ["+queue.get(i).getInfo().title+"]("+queue.get(i).getInfo().uri+")\n");
            builder.appendDescription("**Duration:** " + Utils.getTime(queue.get(i).getDuration())+"\n");
        }
        Long duration = 0L;
        for(AudioTrack track : queue) duration+= track.getDuration();
        builder.setFooter("Total queue size: "+queue.size()+" tracks | Total queue duration: "+Utils.getTime(duration));
        AudioPlayer player = Player.getPlayer(event.getGuild()).getPlayer();
        builder.addField("Currently playing track:", "["+player.getPlayingTrack().getInfo().title+"]("+player.getPlayingTrack().getInfo().uri+")\nDuration: "+Utils.getTime(player.getPlayingTrack().getDuration()), false);
        success = true;
        return builder.build();
    }

    private Object pause(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        if(Player.getPlayer(event.getGuild()).getPlayer().isPaused()){
            Player.getPlayer(event.getGuild()).getPlayer().setPaused(false);
            success = true;
            return "Player unpaused!";
        }
        else Player.getPlayer(event.getGuild()).getPlayer().setPaused(true);
        success = true;
        return "Player paused!";
    }

    private Object playing(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        AudioTrack track = Player.getPlayer(event.getGuild()).getPlayer().getPlayingTrack();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Currently playing track");
        builder.setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")\n");
        builder.setThumbnail("http://img.youtube.com/vi/"+track.getIdentifier()+"/0.jpg");
        builder.setFooter("Position: " + Utils.getTime(track.getPosition())+"/"+Utils.getTime(track.getDuration()));
        success = true;
        return builder.build();
    }

    private Object skip(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        if(event.getOption("index") != null) return remove(event);
        AudioTrack track = Player.getPlayer(event.getGuild()).skip();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Successfully skipped track!, Next track:");
        builder.setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")");
        builder.setThumbnail("http://img.youtube.com/vi/"+track.getIdentifier()+"/0.jpg");
        builder.setFooter("Duration: " + Utils.getTime(track.getDuration()));
        success = true;
        return builder.build();
    }

    private Object remove(SlashCommandInteractionEvent event) {
        AudioTrack track = Player.getPlayer(event.getGuild()).getQueue().remove(event.getOption("index").getAsInt());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Successfully removed track!");
        builder.setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")");
        builder.setThumbnail("http://img.youtube.com/vi/"+track.getIdentifier()+"/0.jpg");
        builder.setFooter("Duration: " + Utils.getTime(track.getDuration()));
        success = true;
        return builder.build();
    }

    private Object disconnect(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        Player.getPlayer(event.getGuild()).disconnect(event.getGuild());
        success = true;
        return "Successfully disconnected bot!";
    }

    private Object loop(SlashCommandInteractionEvent event){
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        return Player.getPlayer(event.getGuild()).getScheduler().toggleLoop();
        
    }
    
}
