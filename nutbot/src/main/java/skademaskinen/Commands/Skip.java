package skademaskinen.Commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import skademaskinen.Music.Player;
import skademaskinen.Utils.Utils;

public class Skip implements Command{

    public static CommandData configure(){
        return Commands.slash(Skip.class.getSimpleName().toLowerCase(), "Skip the currently playing track!")
            .addOption(OptionType.STRING, "index", "skip a song at a given index", false);
    }

    public Skip(SlashCommandInteractionEvent event) {
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The player is not active";
        if(event.getOption("index") != null) return remove(event);
        AudioTrack track = Player.getPlayer(event.getGuild()).skip();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Successfully skipped track!, Next track:");
        builder.setDescription("["+track.getInfo().title+"]("+track.getInfo().uri+")");
        builder.setThumbnail("http://img.youtube.com/vi/"+track.getIdentifier()+"/0.jpg");
        builder.setFooter("Duration: " + Utils.getTime(track.getDuration()));
        return builder.build();
    }

    private Object remove(SlashCommandInteractionEvent event) {
        AudioTrack track = Player.getPlayer(event.getGuild()).getQueue().remove(event.getOption("index").getAsInt());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Successfully removed track!");
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
