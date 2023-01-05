package skademaskinen.Features;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.Modal;
import skademaskinen.Bot;

public class Init implements Feature {
    List<ActionRow> actionRows = new ArrayList<>();
    String cross = "❎";
    String check = "✅";
    
    public static CommandData configure(){
        return Commands.slash(Init.class.getSimpleName().toLowerCase(), "Initialize this server to be used with this bot");
    }



    @Override
    public boolean isSuccess() {
        // TODO Auto-generated method stub
        return false;
    }

    public Init(SlashCommandInteractionEvent event) {
    }

    public Init(ButtonInteractionEvent event) {
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Initialization message");
        builder.setColor(Color.ORANGE);
        builder.setThumbnail(Bot.getJda().getSelfUser().getAvatarUrl());
        builder.setDescription("Hi, i'm the developer of The Nut Bot, to make this bot able to handle multiple discord servers i've chosen to make this little initialization process.\n Below you can see a list of files that have yet to be initialized and some buttons to initialize them.");
        builder.appendDescription("\n"+cross+ " Config file");
        builder.appendDescription("\n"+cross+ " Rolepicker file");
        builder.appendDescription("\n"+cross+ " PvP file");
        builder.appendDescription("\n"+cross+ " Raid file");

        actionRows.add(ActionRow.of(
            Button.secondary(buildSubId("config", null), "Config"),
            Button.secondary(buildSubId("rolepicker", null), "Rolepicker"),
            Button.secondary(buildSubId("pvp", null), "PvP"),
            Button.secondary(buildSubId("raid", null), "Raid")));
        return builder.build();
    }

    @Override
    public Object run(ButtonInteractionEvent event) {
        switch(getSubId(event)){
            case "config":
                return new EmbedBuilder()
                    .setTitle("Config")
                    .setDescription("Edit the configuration file");
            case "rolepicker":
            case "pvp":
            case "raid":
            default:
                return "Error, invalid button";
            
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
