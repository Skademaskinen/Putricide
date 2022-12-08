package skademaskinen.Listeners;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skademaskinen.Bot;
import skademaskinen.Utils.Loggable;

/**
 * This is the log listener, it logs specific guild events such that you know when a member joins and leaves a discord server.
 */
public class LoggingListener extends ListenerAdapter implements Loggable{

    /**
     * This is the eventhandler for join events, it writes them into the log and writes them to a channel in discord
     * @param event This is the event, it contains a lot of information about how this event happened
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        log(true, new String[]{"User: "+event.getUser().getAsTag()," Guild: "+event.getGuild().getName()});
        Bot.getJda().getGuildById(Bot.getConfig().get("guild:id")).getTextChannelById(Bot.getConfig().get("log:channel")).sendMessage(
            "User joined the server: "+event.getUser().getAsTag()+"\nMember mention: "+event.getMember().getAsMention()+"\nMember count: "+event.getGuild().getMemberCount()
        ).queue();
    }

    /**
     * This is the eventhandler for leave events, it writes them into the log and writes them to a channel in discord
     * @param event This is the event, it contains a lot of information about how this event happened
     */
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        log(true, new String[]{"User: "+event.getUser().getAsTag()," Guild: "+event.getGuild().getName()});
        Bot.getJda().getGuildById(Bot.getConfig().get("guild:id")).getTextChannelById(Bot.getConfig().get("log:channel")).sendMessage(
            "User left the server: "+event.getUser().getAsTag()+"\nMember mention: "+event.getUser().getAsMention()+"\nMember count: "+event.getGuild().getMemberCount()
        ).queue();
    }

}
