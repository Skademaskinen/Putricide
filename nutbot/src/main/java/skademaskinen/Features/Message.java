package skademaskinen.Features;

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
import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

public class Message implements Feature{
    private boolean success;
    private List<ActionRow> actionRows = new ArrayList<>();
    private boolean isEphemeral = false;
    private boolean defer = true;

    public static CommandData configure(){
        SlashCommandData data = Commands.slash(Message.class.getSimpleName().toLowerCase(), "Manage summoned embedded messages");
        SubcommandData create = new SubcommandData("create", "creates a new embed");
        SubcommandData clear = new SubcommandData("clear", "clears an embed")
            .addOption(OptionType.STRING, "id", "The id of the message", true);
        SubcommandData addfield = new SubcommandData("field", "add a field to the message")
            .addOption(OptionType.STRING, "id", "The id of the message", true)
            .addOption(OptionType.STRING, "title", "title of the field", true)
            .addOption(OptionType.STRING, "description", "Description of the field", true)
            .addOption(OptionType.BOOLEAN, "inline", "Should the field be inline", false);
        SubcommandData title = new SubcommandData("title", "sets the title of the message")
            .addOption(OptionType.STRING, "id", "The id of the message", true)
            .addOption(OptionType.STRING, "title", "The title to be set");
        SubcommandData description = new SubcommandData("description", "sets the description of the message")
            .addOption(OptionType.STRING, "id", "The id of the message", true)
            .addOption(OptionType.STRING, "title", "The description to be set");
        SubcommandData post = new SubcommandData("post", "Post the message in the announcements channel")
            .addOption(OptionType.STRING, "id", "The id of the message", true);
        data.addSubcommands(create, clear, addfield, title, description, post);
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
            case "title":
            case "description":
            case "post":
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
        try {
            return subCommandLoader(event).invoke(this, event);
        } catch (Exception e) {
            Shell.exceptionHandler(e);
            return null;
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
    public Object run(ButtonInteractionEvent event) {
        switch(getSubId(event)){
            case "clear":
                event.getMessage().editMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
                return "Successfully cleared message";
            case "addfield":
                return Modal.create(buildSubId("addfield", event.getMessageId()), "Add field").addActionRows(
                    ActionRow.of(TextInput.create("title", "Title", TextInputStyle.SHORT).build()), 
                    ActionRow.of(TextInput.create("description", "Description", TextInputStyle.PARAGRAPH).build()),
                    ActionRow.of(TextInput.create("inline", "Inline? true/false", TextInputStyle.SHORT).setRequired(false).build())).build();
            case "title":
                return Modal.create(buildSubId("title", event.getMessageId()), "Set Title")
                    .addActionRow(TextInput.create("title", "Title", TextInputStyle.SHORT).build())
                    .build();
            case "description":
                return Modal.create(buildSubId("description", event.getMessageId()), "Set Description")
                    .addActionRow(TextInput.create("description", "Description", TextInputStyle.PARAGRAPH).build())
                    .build();
            case "post":
                return Modal.create(buildSubId("post", event.getMessage().getId()), "Are you sure?")
                    .addActionRow(TextInput.create("confirmation", "Confirmation", TextInputStyle.SHORT).build())
                    .build();

            default:
                return "Error, invalid button";
        }
    }

    @Override
    public Object run(ModalInteractionEvent event) {
        String id;
        net.dv8tion.jda.api.entities.Message message;
        String title;
        String description;
        EmbedBuilder builder;

        switch(getSubId(event)){
            case "addfield":
                id = event.getModalId().split("::")[2];
                message = event.getChannel().getHistoryAround(id, 1).complete().getMessageById(id);
                title = event.getValue("title").getAsString();
                description = event.getValue("description").getAsString();
                boolean inline = event.getValue("inline").getAsString().equalsIgnoreCase("true");
                builder = reinit(message.getEmbeds().get(0));
                builder.addField(title, description, inline);
                message.editMessageEmbeds(builder.build()).queue();
                return "Successfully added a field to the message";
            case "title":
                id = event.getModalId().split("::")[2];
                message = event.getChannel().getHistoryAround(id, 1).complete().getMessageById(id);
                title = event.getValue("title").getAsString();
                builder = reinit(message.getEmbeds().get(0));
                builder.setTitle(title);
                message.editMessageEmbeds(builder.build()).queue();
                return "Successfully set the title of the message";
            case "description":
                id = event.getModalId().split("::")[2];
                message = event.getChannel().getHistoryAround(id, 1).complete().getMessageById(id);
                description = event.getValue("description").getAsString();
                builder = reinit(message.getEmbeds().get(0));
                builder.setDescription(description);
                message.editMessageEmbeds(builder.build()).queue();
                return "Successfully set the description of the message";
            case "post":
                id = event.getModalId().split("::")[2];
                message = event.getChannel().getHistoryAround(id, 1).complete().getMessageById(id);
                if(event.getValues().get(0).getAsString().equalsIgnoreCase("yes")){
                    event.getGuild().getTextChannelById(Bot.getConfig().get("guild:announcements")).sendMessageEmbeds(message.getEmbeds().get(0)).queue();
                    return "Sent announcement!";
                }
                return "Did not send announcement!";


            default:
                return "Error, invalid modal!";
        }
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }

    public Object create(SlashCommandInteractionEvent event){
        actionRows.add(ActionRow.of(
            Button.secondary(buildSubId("clear", null), "Clear"),
            Button.secondary(buildSubId("addfield", null), "Create Field"),
            Button.secondary(buildSubId("title", null), "Set Title"),
            Button.secondary(buildSubId("description", null), "Set Description"),
            Button.primary(buildSubId("post", null), "Post")));
        return new EmbedBuilder().setTitle("init").build();
    }

    public Object clear(SlashCommandInteractionEvent event){
        String id = event.getOption("id").getAsString();
        event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id).editMessageEmbeds(new EmbedBuilder().setTitle("init").build()).queue();
        return "cleared message";
    }

    public Object field(SlashCommandInteractionEvent event){
        String id = event.getOption("id").getAsString();
        String title = event.getOption("title").getAsString();
        String description = event.getOption("description").getAsString();
        boolean inline = event.getOption("inline") != null ? event.getOption("inline").getAsBoolean() : false;
        net.dv8tion.jda.api.entities.Message message = event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id);
        EmbedBuilder builder = reinit(message.getEmbeds().get(0));
        builder.addField(title, description, inline);
        message.editMessageEmbeds(builder.build()).queue();
        return "Successfully added a field to the message";
    }

    public Object title(SlashCommandInteractionEvent event){
        String id = event.getOption("id").getAsString();
        String title = event.getOption("title").getAsString();
        net.dv8tion.jda.api.entities.Message message = event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id);
        EmbedBuilder builder = reinit(message.getEmbeds().get(0));
        builder.setTitle(title);
        message.editMessageEmbeds(builder.build()).queue();
        return "Successfully set the title of the embed";
    }

    public Object description(SlashCommandInteractionEvent event){
        String id = event.getOption("id").getAsString();
        String description = event.getOption("description").getAsString();
        net.dv8tion.jda.api.entities.Message message = event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id);
        EmbedBuilder builder = reinit(message.getEmbeds().get(0));
        builder.setDescription(description);
        message.editMessageEmbeds(builder.build()).queue();
        return "Successfully set the title of the embed";
    }

    public Object post(SlashCommandInteractionEvent event){
        String id = event.getOption("id").getAsString();
        net.dv8tion.jda.api.entities.Message message = event.getMessageChannel().getHistoryAround(id, 1).complete().getMessageById(id);
        return Modal.create(buildSubId("post", message.getId()), "Are you sure?")
            .addActionRow(TextInput.create("confirmation", "Confirmation", TextInputStyle.SHORT).build())
            .build();
    }
    
}
