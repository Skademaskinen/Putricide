package skademaskinen.Music;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import skademaskinen.Utils.Shell;

public class ResultHandler implements AudioLoadResultHandler {
    private CyclicBarrier barrier;
    String type;
    private AudioPlaylist playlist;
    private AudioTrack track;
    private AudioPlayer player;
    private Scheduler scheduler;

    public ResultHandler(Player player) {
        this.player = player.getPlayer();
        this.scheduler = player.getScheduler();
        this.barrier = player.getBarrier();
    }

    @Override
    public void loadFailed(FriendlyException e) {
        Shell.exceptionHandler(e);
        type = "fail";
        await();
    }

    @Override
    public void noMatches() {
        Shell.println("No matches!");
        type = "fail";
        await();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        List<AudioTrack> tracks = playlist.getTracks();
        this.track = tracks.get(0);
        if(player.getPlayingTrack() == null) player.playTrack(tracks.remove(0));
        for(AudioTrack track : tracks) scheduler.enqueue(track);
        type = "playlist";
        this.playlist = playlist;
        await();
        
    }

    @Override
    public void trackLoaded(AudioTrack track){
        if(player.getPlayingTrack() != null){
            scheduler.enqueue(track);
        }
        else{
            player.playTrack(track);
        }
        type = "single";
        this.track = track;
        await();
        
    }

    private void await(){
        try {
            barrier.await();
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
    }

    public String getType() {
        return type;
    }
    public AudioPlaylist getPlaylist() {
        return playlist;
    }
    public AudioTrack getTrack() {
        return track;
    }

    public CyclicBarrier getBarrier() {
        return barrier;
    }
    
    
}
