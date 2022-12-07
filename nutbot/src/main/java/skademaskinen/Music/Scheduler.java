package skademaskinen.Music;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.Guild;
import skademaskinen.Utils.Shell;

public class Scheduler extends AudioEventAdapter {
    private List<AudioTrack> queue = new ArrayList<>();
    private Guild guild;

    public Scheduler(Guild guild){
        this.guild = guild;
    }

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(queue.size() > 0) player.playTrack(queue.remove(0));
        else guild.getAudioManager().closeAudioConnection();
        Shell.println("Track ended! Queue size: "+queue.size());
    }

    public void enqueue(AudioTrack track) {
        queue.add(track);
    }

    public List<AudioTrack> getQueue() {
        return queue;
    }

    public AudioTrack getNext() {
        return queue.get(0);
    }
}
