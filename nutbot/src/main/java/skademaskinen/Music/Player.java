package skademaskinen.Music;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import skademaskinen.Utils.Shell;

/**
 * This class describes handles the player for the specific
 */
public class Player {
    private static Map<Guild, Player> players = new HashMap<>();
    private static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private AudioPlayer player;
    private AudioManager audioManager;
    private Scheduler scheduler;
    private CyclicBarrier barrier;

    public Player(VoiceChannel channel) {
        audioManager = channel.getGuild().getAudioManager();
        player = playerManager.createPlayer();
        scheduler = new Scheduler(channel.getGuild());
        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
        player.addListener(scheduler);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public AudioTrack enqueue(String searchTerm) {
        barrier = new CyclicBarrier(2);
        ResultHandler handler = new ResultHandler(this);
        playerManager.loadItem(searchTerm, handler);
        try {
            barrier.await();
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
        switch(handler.getType()){
            case "playlist":
            case "single":
                return handler.getTrack();
            case "fail":
            default:
                return null;
        }
    }

    public static boolean isInitialized(Guild guild) {
        return players.containsKey(guild);
    }

    public static void initialize(VoiceChannel channel) {
        players.put(channel.getGuild(), new Player(channel));
    }
    public void connect(VoiceChannel channel){
        audioManager.openAudioConnection(channel);
    }

    public static Player getPlayer(Guild guild) {
        return players.get(guild);
    }

    public AudioPlayer getPlayer() {
        return player;
    }
    public CyclicBarrier getBarrier() {
        return barrier;
    }
    public Scheduler getScheduler() {
        return scheduler;
    }

    private class QueryHandler implements AudioLoadResultHandler{
        private CyclicBarrier QueryBarrier = new CyclicBarrier(2);
        private AudioPlaylist result;
        public AudioPlaylist getResult() {
            return result;
        }
        public CyclicBarrier getBarrier() {
            return QueryBarrier;
        }
        public void await(){
            try {
                QueryBarrier.await();
            } catch (Exception e) {
                Shell.exceptionHandler(e);
            }

        }
        @Override
        public void loadFailed(FriendlyException e) {
            result = null;
            await();

        }
        @Override
        public void noMatches() {
            result = null;
            await();
        }
        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            result = playlist;
            await();
        }
        @Override
        public void trackLoaded(AudioTrack track) {}

    }
    public AudioPlaylist searchQuery(String query) {
        QueryHandler handler = new QueryHandler();
        playerManager.loadItem(query, handler);
        try {
            handler.getBarrier().await();
        } catch (Exception e){
            Shell.exceptionHandler(e);
        }
        return handler.getResult();
    }

    public List<AudioTrack> getQueue() {
        return scheduler.getQueue();
    }

    public AudioTrack skip() {
        AudioTrack track;
        if(getQueue().size() > 0) track = scheduler.getNext();
        else track = null;
        player.stopTrack();
        return track;
    }

    public AudioTrack put(int index, String searchTerm) {
        barrier = new CyclicBarrier(2);
        ResultHandler handler = new ResultHandler(this, index);
        playerManager.loadItem(searchTerm, handler);
        try {
            barrier.await();
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
        switch(handler.getType()){
            case "playlist":
            case "single":
                return handler.getTrack();
            case "fail":
            default:
                return null;
        }
    }

    public void disconnect(Guild guild) {
        audioManager.closeAudioConnection();
        players.remove(guild);
    }
    

}
