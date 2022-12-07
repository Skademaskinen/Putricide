package skademaskinen.Music;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import skademaskinen.Utils.Shell;

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
        scheduler = new Scheduler();
        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
        player.addListener(scheduler);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        audioManager.openAudioConnection(channel);
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
    

}
