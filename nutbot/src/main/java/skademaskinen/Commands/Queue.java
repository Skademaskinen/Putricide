package skademaskinen.Commands;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import skademaskinen.Music.Player;
import skademaskinen.Utils.Utils;

public class Queue implements Command {

    public static CommandData configure(){
        SlashCommandData command = Commands.slash(Queue.class.getSimpleName().toLowerCase(), "Show the queue of the music bot");
        return command;
    }

    public Queue(SlashCommandInteractionEvent event) {

    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        if(!Player.isInitialized(event.getGuild())) return "The queue is empty!";
        List<AudioTrack> queue = Player.getPlayer(event.getGuild()).getQueue();
        if(queue.size() == 0) return "The queue is empty!";
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Track queue!");
        builder.setThumbnail("http://img.youtube.com/vi/"+queue.get(0).getIdentifier()+"/0.jpg");
        for(int i = 0; i < (queue.size() <= 25 ? queue.size() : 25); i++){
            builder.appendDescription("["+i+"]: ["+queue.get(i).getInfo().title+"]("+queue.get(i).getInfo().uri+")\n");
            builder.appendDescription("Duration: " + Utils.getTime(queue.get(i).getDuration())+"\n");
        }
        return builder.build();
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
    
}
