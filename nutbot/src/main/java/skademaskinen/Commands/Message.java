package skademaskinen.Commands;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;

public class Message implements Command{
    private boolean success;

    public static CommandData configure(){
        SlashCommandData data = Commands.slash(Message.class.getSimpleName().toLowerCase(), "Manage summoned embedded messages");
        data.addSubcommands(new SubcommandData("create", "creates a new embed"));
        data.addSubcommands(new SubcommandData("clear", "clears an embed"));
        return data;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public List<ActionRow> getActionRows() {
        return null;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        switch(event.getSubcommandName()){
            case "create":
                return new EmbedBuilder().setTitle("init").build();
            case "clear":
                String id = event.getOption("id").getAsString();
                event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id).editMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
                return "cleared message";
            default:
                return "Error, invalid subcommand";
        }
    }
    
}
