package skademaskinen.Features;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import skademaskinen.Bot;
import skademaskinen.Utils.ServerConfig;
import skademaskinen.Utils.Shell;

public class Init implements Feature {
    List<ActionRow> actionRows = new ArrayList<>();
    String cross = "❎";
    String check = "✅";
    private boolean defer;
    private boolean ephemeral = false;
    private boolean deferEdit;
    
    public static CommandData configure(){
        return Commands.slash(Init.class.getSimpleName().toLowerCase(), "Initialize this server to be used with this bot");
    }



    @Override
    public boolean isSuccess() {
        // TODO Auto-generated method stub
        return false;
    }

    public Init(SlashCommandInteractionEvent event) {
        defer = true;
    }

    public Init(ButtonInteractionEvent event) {
        if(getSubId(event).matches("configImage|configName|configRealm|rolepickerImage|rolepickerDescription|rolepickerTitle")){
            defer = false;
        }
        else if(getSubId(event).matches("configFinish|rolepickerFinish")) deferEdit = true;
        else defer = true;
    }

    public Init(StringSelectInteractionEvent event) {
        defer = false;
        deferEdit = true;
    }

    public Init(ModalInteractionEvent event) {
        defer = false;
        deferEdit = true;
    }

    @Override
    public boolean shouldDefer() {
        return defer;
    }

    @Override
    public boolean isEphemeral() {
        return ephemeral;
    }

    @Override
    public boolean shouldDeferEdit() {
        return deferEdit;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Initialization message");
        builder.setColor(Color.ORANGE);
        builder.setThumbnail(Bot.getJda().getSelfUser().getAvatarUrl());
        builder.setDescription("Hi, i'm the developer of The Nut Bot, to make this bot able to handle multiple discord servers i've chosen to make this little initialization process.\n Below you can see a list of files that have yet to be initialized and some buttons to initialize them.");
        builder.addField("Progress:", cross+" Config file"+"\n"+cross+" Rolepicker file", false);

        actionRows.add(ActionRow.of(
            Button.secondary(buildSubId("config", null), "Config"),
            Button.secondary(buildSubId("rolepicker", null), "Rolepicker")));
        return builder.build();
    }

    public Object run(ButtonInteractionEvent event) {
        String messageId = getSubId(event).equals("config") || getSubId(event).equals("rolepicker") ? event.getMessageId() : event.getComponentId().split("::")[2];
        switch(getSubId(event)){
            case "config":
                actionRows.add(ActionRow.of(
                    Button.success(buildSubId("configFinish", messageId), "Finish"),
                    Button.secondary(buildSubId("configImage", messageId), "Set Image"),
                    Button.secondary(buildSubId("configName", messageId), "Exact guild name"),
                    Button.secondary(buildSubId("configRealm", messageId), "Guild realm")));
                actionRows.add(ActionRow.of(StringSelectMenu.create(buildSubId("configRegion", event.getMessageId()))
                    .addOptions(SelectOption.of("EU", "eu"), SelectOption.of("US", "us"))
                    .setPlaceholder("Region")
                    .setMaxValues(1)
                    .build()));
                List<SelectOption> channelOptions = new ArrayList<>();
                for(TextChannel channel : event.getGuild().getTextChannels()){
                    channelOptions.add(SelectOption.of(channel.getName(), channel.getId()));
                }
                actionRows.add(ActionRow.of(StringSelectMenu.create(buildSubId("configLog", event.getMessageId()))
                    .addOptions(channelOptions)
                    .setPlaceholder("Log channel")
                    .setMaxValues(1)
                    .build()));
                actionRows.add(ActionRow.of(StringSelectMenu.create(buildSubId("configAnnouncements", event.getMessageId()))
                    .addOptions(channelOptions)
                    .setPlaceholder("Announcements channel")
                    .setMaxValues(1)
                    .build()));
                actionRows.add(ActionRow.of(StringSelectMenu.create(buildSubId("configIssues", event.getMessageId()))
                    .addOptions(channelOptions)
                    .setPlaceholder("Issues channel")
                    .setMaxValues(1)
                    .build()));
                
                
                return new EmbedBuilder()
                    .setTitle("Config")
                    .setDescription("Edit the configuration file:")
                    .appendDescription("\n```json\n"+ServerConfig.get(event.getGuild()).toString(4)+"```")
                    .build();
            case "rolepicker":
                actionRows.add(ActionRow.of(
                    Button.success(buildSubId("rolepickerFinish", messageId), "Finish"),
                    Button.secondary(buildSubId("rolepickerImage", messageId), "Set Image"),
                    Button.secondary(buildSubId("rolepickerDescription", messageId), "Set Description"),
                    Button.secondary(buildSubId("rolepickerTitle", messageId), "Set Title")
                ));
                return new EmbedBuilder()
                    .setTitle("Rolepicker")
                    .setDescription("Edit the rolepicker configuration:")
                    .appendDescription("\n```json\n"+ServerConfig.rolepickerGet(event.getGuild()).toString(4)+"```")
                    .build();
            case "configFinish":
                updateMainMessage(event.getMessageChannel().retrieveMessageById(messageId).complete(), "config");
                event.getMessage().delete().queue();
                return "Finished initializing configuration file";
            case "configImage":
                return Modal.create(buildSubId(getSubId(event), event.getMessageId()), "Set Image")
                    .addActionRow(TextInput.create("content", "Image URL", TextInputStyle.SHORT).build())
                    .build();
            case "configName":
                return Modal.create(buildSubId(getSubId(event), event.getMessageId()), "Set Guild Name")
                    .addActionRow(TextInput.create("content", "Guild Name", TextInputStyle.SHORT).build())
                    .build();
            case "configRealm":
                return Modal.create(buildSubId(getSubId(event), event.getMessageId()), "Set Guild Name")
                    .addActionRow(TextInput.create("content", "Guild Realm", TextInputStyle.SHORT).build())
                    .build();
            case "rolepickerFinish":
                updateMainMessage(event.getMessageChannel().retrieveMessageById(messageId).complete(), "rolepicker");
                event.getMessage().delete().queue();
                return "Finished initializing rolepicker configuration file";
            case "rolepickerImage":
                return Modal.create(buildSubId(getSubId(event), event.getMessageId()), "Set image")
                    .addActionRow(TextInput.create("content", "Image URL", TextInputStyle.SHORT).build())
                    .build();
            case "rolepickerDescription":
                return Modal.create(buildSubId(getSubId(event), event.getMessageId()), "Set Description")
                    .addActionRow(TextInput.create("content", "Rolepicker Description", TextInputStyle.PARAGRAPH).build())
                    .build();
            case "rolepickerTitle":
                return Modal.create(buildSubId(getSubId(event), event.getMessageId()), "Set Title")
                    .addActionRow(TextInput.create("content", "Rolepicker Title", TextInputStyle.SHORT).build())
                    .build();
            default:
                return "Error, invalid button";
            
        }
    }

    private void updateMainMessage(Message message, String type) {
        MessageEmbed embed = message.getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(embed.getTitle());
        builder.setDescription(embed.getDescription());
        if(type.equals("config")){
            builder.addField("Progress:", embed.getFields().get(0).getValue().replace(cross+" Config file", check+" Config file"), false);
        }
        else if(type.equals("rolepicker")){
            builder.addField("Progress:", embed.getFields().get(0).getValue().replace(cross+" Rolepicker file", check+" Rolepicker file"), false);
        }
        if(!builder.getFields().get(0).getValue().contains(cross)){
            message.delete().queue();
        }
        else message.editMessageEmbeds(builder.build()).queue();
    }

    private void updateConfigMessage(Message message){
        MessageEmbed embed = message.getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(embed.getTitle());
        builder.setDescription("Edit the configuration file:\n```json\n"+ServerConfig.get(message.getGuild()).toString(4)+"```");
        message.editMessageEmbeds(builder.build()).queue();
    }

    private void updateRolepickerMessage(Message message){
        MessageEmbed embed = message.getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(embed.getTitle());
        builder.setDescription("Edit the rolepicker configuration:\n```json\n"+ServerConfig.rolepickerGet(message.getGuild()).toString(4)+"```");
        message.editMessageEmbeds(builder.build()).queue();
    }



    @Override
    public Object run(StringSelectInteractionEvent event) {
        String id = event.getComponentId().split("::")[1];
        JSONObject config =ServerConfig.get(event.getGuild());
        String content = event.getSelectedOptions().get(0).getValue();
        switch(id){
            case "configLog":
                config.getJSONObject("channels").put("log", content);
                ServerConfig.write(event.getGuild(), config);
                updateConfigMessage(event.getMessageChannel().retrieveMessageById(event.getMessageId()).complete());
                return "updated log channel id";
            case "configAnnouncements":
                config.getJSONObject("channels").put("announcements", content);
                ServerConfig.write(event.getGuild(), config);
                updateConfigMessage(event.getMessageChannel().retrieveMessageById(event.getMessageId()).complete());
                return "updated announcements channel id";
            case "configIssues":
                config.getJSONObject("channels").put("issues", content);
                ServerConfig.write(event.getGuild(), config);
                updateConfigMessage(event.getMessageChannel().retrieveMessageById(event.getMessageId()).complete());
                return "updated issues channel id";
            case "configRegion":
                config.put("region", content);
                ServerConfig.write(event.getGuild(), config);
                updateConfigMessage(event.getMessageChannel().retrieveMessageById(event.getMessageId()).complete());
                return "updated region specification";
            default:
                return "Error, invalid option";
        }
    }

    @Override
    public Object run(ModalInteractionEvent event) {
        String id = getSubId(event);
        JSONObject config = ServerConfig.get(event.getGuild());
        JSONObject rolepickerConfig = ServerConfig.rolepickerGet(event.getGuild());
        String content = event.getValues().get(0).getAsString();
        switch(id){
            case "configImage":
                config.put("image", content);
                ServerConfig.write(event.getGuild(), config);
                updateConfigMessage(event.getMessageChannel().retrieveMessageById(event.getModalId().split("::")[2]).complete());
                return "Updated config image";
            case "configName":
                config.put("name", content);
                ServerConfig.write(event.getGuild(), config);
                updateConfigMessage(event.getMessageChannel().retrieveMessageById(event.getModalId().split("::")[2]).complete());
                return "Updated config name";
            case "configRealm":
                config.put("realm", content);
                ServerConfig.write(event.getGuild(), config);
                updateConfigMessage(event.getMessageChannel().retrieveMessageById(event.getModalId().split("::")[2]).complete());
                return "Updated config realm";
            case "rolepickerImage":
                rolepickerConfig.getJSONObject("configuration").put("image", content);
                ServerConfig.rolepickerWrite(event.getGuild(), rolepickerConfig);
                updateRolepickerMessage(event.getMessageChannel().retrieveMessageById(event.getModalId().split("::")[2]).complete());
                return "Updated rolepicker image";
            case "rolepickerDescription":
                rolepickerConfig.getJSONObject("configuration").put("description", content);
                ServerConfig.rolepickerWrite(event.getGuild(), rolepickerConfig);
                updateRolepickerMessage(event.getMessageChannel().retrieveMessageById(event.getModalId().split("::")[2]).complete());
                return "Updated rolepicker description";
            case "rolepickerTitle":
                rolepickerConfig.getJSONObject("configuration").put("title", content);
                ServerConfig.rolepickerWrite(event.getGuild(), rolepickerConfig);
                updateRolepickerMessage(event.getMessageChannel().retrieveMessageById(event.getModalId().split("::")[2]).complete());
                return "Updated rolepicker title";
            default:
                return "Error, invalid modal";
        }
    }

    @Override
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public List<ActionRow> getActionRows() {
        return actionRows;
    }
    
}
