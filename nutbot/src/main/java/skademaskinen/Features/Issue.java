package skademaskinen.Features;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import skademaskinen.Bot;

public class Issue implements Feature{

    public static CommandData configure(){
        return Commands.slash(Issue.class.getSimpleName().toLowerCase(), "Write an issue report, this can be a feature request or a bug report etc.")
            .addOption(OptionType.STRING, "issue", "The issue being reported", true)
            .addOption(OptionType.STRING, "description", "The description of the issue", false);
    }

    private boolean success = false;

    public Issue(SlashCommandInteractionEvent event) {
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        String title = event.getOption("issue").getAsString();
        String description = event.getOption("description") == null ? "" : event.getOption("description").getAsString();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setDescription(description);
        builder.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getEffectiveAvatarUrl());
        builder.setColor(event.getMember().getColor());
        builder.setFooter(event.getGuild().getName());
        if(Bot.getConfig().get("issues:channel") != null){
            event.getGuild().getTextChannelById(Bot.getConfig().get("issues:channel")).sendMessageEmbeds(builder.build()).queue();
            success = true;
            return "Sent issue report!";
        }
        else return "Failed to send issue report! bot is not configured correctly by administrators!";
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
}
