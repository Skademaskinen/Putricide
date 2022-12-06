package skademaskinen.Listeners;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skademaskinen.Bot;
import skademaskinen.Utils.Loggable;

public class LoggingListener extends ListenerAdapter implements Loggable{
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        log(true, new String[]{"User: "+event.getUser().getAsTag()," Guild: "+event.getGuild().getName()});
        Bot.getJda().getGuildById(Bot.getConfig().get("guild:id")).getTextChannelById(Bot.getConfig().get("log:channel")).sendMessage(
            "User joined the server: "+event.getUser().getAsTag()+"\nMember mention: "+event.getMember().getAsMention()+"\nMember count: "+event.getGuild().getMemberCount()
        ).queue();
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        log(true, new String[]{"User: "+event.getUser().getAsTag()," Guild: "+event.getGuild().getName()});
        Bot.getJda().getGuildById(Bot.getConfig().get("guild:id")).getTextChannelById(Bot.getConfig().get("log:channel")).sendMessage(
            "User left the server: "+event.getUser().getAsTag()+"\nMember mention: "+event.getUser().getAsMention()+"\nMember count: "+event.getGuild().getMemberCount()
        ).queue();
    }

}
