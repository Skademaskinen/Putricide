package skademaskinen.Commands;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class Message implements Command{
    private boolean success;
    private List<ActionRow> actionRows = new ArrayList<>();
    private boolean isEphemeral = false;
    private boolean defer = true;

    public static CommandData configure(){
        SlashCommandData data = Commands.slash(Message.class.getSimpleName().toLowerCase(), "Manage summoned embedded messages");
        SubcommandData create = new SubcommandData("create", "creates a new embed");
        SubcommandData clear = new SubcommandData("clear", "clears an embed")
            .addOption(OptionType.STRING, "id", "The id of the message", true);
        SubcommandData addfield = new SubcommandData("addfield", "add a field to the message")
            .addOption(OptionType.STRING, "id", "The id of the message", true)
            .addOption(OptionType.STRING, "title", "title of the field", true)
            .addOption(OptionType.STRING, "description", "Description of the field", true)
            .addOption(OptionType.BOOLEAN, "inline", "Should the field be inline", false);
        data.addSubcommands(create, clear, addfield);
        return data;
    }

    public Message(SlashCommandInteractionEvent event) {
        switch(event.getSubcommandName()){
            case "create":
                isEphemeral = false;
                break;
            default:
                isEphemeral = true;
        }
    }

    public Message(ButtonInteractionEvent event){
        switch(getSubId(event)){
            case "addfield":
                defer = false;
        }
        isEphemeral = true;
    }
    public Message(ModalInteractionEvent event) {
        isEphemeral = true;
    }

    @Override
    public boolean shouldDefer() {
        return defer;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public List<ActionRow> getActionRows() {
        return actionRows;
    }

    @Override
    public boolean isEphemeral() {
        return isEphemeral;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        String id;
        switch(event.getSubcommandName()){
            case "create":
                actionRows.add(ActionRow.of(
                    Button.secondary(buildSubId("clear", null), "clear"),
                    Button.secondary(buildSubId("addfield", null), "Field")));
                return new EmbedBuilder().setTitle("init").build();
            case "clear":
                id = event.getOption("id").getAsString();
                event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id).editMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
                return "cleared message";
            case "addfield":
                id = event.getOption("id").getAsString();
                String title = event.getOption("title").getAsString();
                String description = event.getOption("description").getAsString();
                boolean inline = event.getOption("inline") != null ? event.getOption("inline").getAsBoolean() : false;
                net.dv8tion.jda.api.entities.Message message = event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id);
                EmbedBuilder builder = reinit(message.getEmbeds().get(0));
                builder.addField(title, description, inline);
                message.editMessageEmbeds(builder.build()).queue();
                return "Successfully added a field to the message";
            default:
                return "Error, invalid subcommand";
        }
    }

    private EmbedBuilder reinit(MessageEmbed embed) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(embed.getTitle());
        builder.setDescription(embed.getDescription());
        for(Field field : embed.getFields()){
            builder.addField(field);
        }
        return builder;
    }

    @Override
    public Object ButtonExecute(ButtonInteractionEvent event) {
        switch(getSubId(event)){
            case "clear":
                event.getMessage().editMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
                return "Successfully cleared message";
            case "addfield":
                return Modal.create(buildSubId("addfield", event.getMessageId()), "Add field").addActionRows(
                    ActionRow.of(TextInput.create("title", "Title", TextInputStyle.SHORT).build()), 
                    ActionRow.of(TextInput.create("description", "Description", TextInputStyle.PARAGRAPH).build()),
                    ActionRow.of(TextInput.create("inline", "Inline? true/false", TextInputStyle.SHORT).setRequired(false).build())).build();
            default:
                return "Error, invalid button";
        }
    }

    @Override
    public Object ModalExecute(ModalInteractionEvent event) {
        String id;
        switch(getSubId(event)){
            case "addfield":
                id = event.getModalId().split("::")[2];
                net.dv8tion.jda.api.entities.Message message = event.getChannel().getHistoryAround(id, 1).complete().getMessageById(id);
                String title = event.getValue("title").getAsString();
                String description = event.getValue("description").getAsString();
                boolean inline = event.getValue("inline").getAsString().equalsIgnoreCase("true");
                EmbedBuilder builder = reinit(message.getEmbeds().get(0));
                builder.addField(title, description, inline);
                message.editMessageEmbeds(builder.build()).queue();
                return "Successfully added a field to the message";
            default:
                return "Error, invalid modal!";
        }
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
    
}
