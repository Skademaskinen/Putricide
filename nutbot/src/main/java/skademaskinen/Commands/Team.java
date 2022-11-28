package skademaskinen.Commands;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import skademaskinen.Bot;
import skademaskinen.WorldOfWarcraft.RaidTeam;

public class Team implements Command {
    private boolean success = false;
    private boolean shouldEphemeral = true;
    private List<ActionRow> actionRows = new ArrayList<>();

    public static CommandData configure(){
        SlashCommandData command = Commands.slash(Team.class.getSimpleName().toLowerCase(), "Admin command: Handle the raid team");
        SubcommandData add = new SubcommandData("add", "Add a raider to the raid team manually");
        OptionData raider = new OptionData(OptionType.USER, "raider", "Mention of the raider", true);
        OptionData name = new OptionData(OptionType.STRING, "name", "Character name", true);
        OptionData server = new OptionData(OptionType.STRING, "server", "Character server", false, true);
        OptionData role = new OptionData(OptionType.STRING, "role", "Character role", true, true);
        add.addOptions(raider,name,role,server);
        SubcommandData remove = new SubcommandData("remove", "Remove a raider from the raid team manually");
        remove.addOptions(raider);
        SubcommandData update = new SubcommandData("update", "Update the raid team message");
        SubcommandData form = new SubcommandData("form", "Create a raid team application form");
        command.addSubcommands(add,remove,update,form);
        return command;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public boolean isEphemeral(){
        return shouldEphemeral;
    }

    @Override
    public List<ActionRow> getActionRows() {
        return actionRows;
    }

    public Team(SlashCommandInteractionEvent event){
        switch(event.getSubcommandName()){
            case "add":
            case "remove":
            case "update":
                shouldEphemeral = true;
                break;
            case "form":
                shouldEphemeral = false;
                break;
        }
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        Object result = "";
        switch(event.getSubcommandName()){
            case "add":
                result = event.getOption("server") == null ? 
                    add(event.getOption("raider").getAsUser(), event.getOption("name").getAsString(), event.getOption("role").getAsString(), "argent-dawn") :
                    add(event.getOption("raider").getAsUser(), event.getOption("name").getAsString(), event.getOption("role").getAsString(), event.getOption("server").getAsString());

                break;
            case "remove":
                result = remove(event.getOption("raider").getAsUser());
                break;
            case "update":
                result = RaidTeam.update();
                break;
            case "form":
                result = form();
        }                
        if(result == null){
            success = false;
            return "Command failed!";
        }
        return result;
    }

    private String add(User user, String name, String role, String server){
        success = RaidTeam.add(user,name,role,server);
        if(success){
            return "Successfully added member to raid team!";
        }
        else{
            return "Error, failed to add member to raid team!";
        }
    }

    private String remove(User user){
        RaidTeam.remove(user);
        return "Successfully removed user from raid team!";
    }


    private MessageEmbed form(){
        EmbedBuilder builder = new EmbedBuilder();
        String guildName = Bot.getConfig().get("guildName");
        builder.setTitle("Apply to the raid team of "+guildName+"!");
        builder.setDescription("Hi, here you can apply to the raid team!\nYou will receive a pop-up form to add your character's details.");
        if(Bot.getConfig().get("guildImage") != null){
            builder.setImage(Bot.getConfig().get("guildImage"));
        }
        actionRows.add(ActionRow.of(Button.primary(buildButtonId("apply", null), "Apply here!")));
        return builder.build();
    }
    
}
