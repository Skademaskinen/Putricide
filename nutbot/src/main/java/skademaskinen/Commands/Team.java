package skademaskinen.Commands;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import skademaskinen.Bot;
import skademaskinen.Utils.Utils;
import skademaskinen.WorldOfWarcraft.BattleNetAPI;
import skademaskinen.WorldOfWarcraft.Character;
import skademaskinen.WorldOfWarcraft.RaidTeam;

public class Team implements Command {
    private boolean success = false;
    private boolean shouldEphemeral = true;
    private List<ActionRow> actionRows = new ArrayList<>();
    private boolean defer = true;

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

    @Override
    public boolean shouldDefer() {
        return defer;
    }

    public Team(ButtonInteractionEvent event) {
        defer = false;
        shouldEphemeral = false;
    }

    public Team(ModalInteractionEvent event){
        defer = true;
        shouldEphemeral = false;
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
        actionRows.add(ActionRow.of(Button.primary(buildButtonId("button", null), "Apply here!")));
        return builder.build();
    }

    @Override
    public Modal ButtonExecute(ButtonInteractionEvent event) {
        TextInput name = TextInput.create("name", "Character name", TextInputStyle.SHORT)
                .setPlaceholder("Your character name")
                .build();
            TextInput server = TextInput.create("server", "Character server", TextInputStyle.SHORT)
                .setPlaceholder("Your character server, example: argent-dawn")
                .setValue("argent-dawn")
                .build();
            TextInput role = TextInput.create("role", "Your role", TextInputStyle.SHORT)
                .setPlaceholder("Healer, Tank, Ranged Damage or Melee Damage")
                .build();
            TextInput raidtimes = TextInput.create("raidtimes", "Wednesday and Sunday 19:30 - 22:30?", TextInputStyle.SHORT)
                .setPlaceholder("Can you raid with us? (yes/no)")
                .setValue("yes")
                .build();

            Modal modal = Modal.create(buildSubId("modal", null), "Application form")
                .addActionRows(ActionRow.of(name), ActionRow.of(role), ActionRow.of(server), ActionRow.of(raidtimes))
                .build();
            return modal;

    }

    @Override
    public Object ModalExecute(ModalInteractionEvent event) {
        String name = event.getValue("name").getAsString();
        String server = event.getValue("server").getAsString().toLowerCase().replace(" ", "-");
        String role = event.getValue("role").getAsString();
        boolean raidtimes = event.getValue("raidtimes").getAsString().equalsIgnoreCase("yes") ? true : false;

        if(!BattleNetAPI.verifyCharacter(name, server)){
            return "Error, this character is not valid - check the name or server, or check your battle.net account's security settings";
        }
        if(!role.toLowerCase().matches("tank|healer|melee damage|ranged damage")){
            return "Error, the specified role does not match any of the valid roles!";
        }

        Character character = new Character(name, server);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Raid team application");
        builder.setAuthor(event.getMember().getEffectiveName(), "https://discordapp.com/users/"+event.getMember().getId(), event.getMember().getEffectiveAvatarUrl());
        builder.setDescription("----------------------------");
        builder.appendDescription("\n**Name**: " +"["+name+"](https://worldofwarcraft.com/en-gb/character/eu/"+server+"/"+name+") ("+Utils.capitalize(server.replace("-", " "))+")");
        builder.appendDescription("\n**Class**: "+character._getClass());
        builder.appendDescription("\n**Specialization**: "+character.getSpecialization());
        builder.appendDescription("\n**Role**: "+Utils.capitalize(role));
        builder.appendDescription("\n**Item level**: "+character.getIlvl()+"/"+character.getAverageIlvl());
        builder.appendDescription("\n**Availability**: "+ (raidtimes ? "yes" : "no"));

        builder.setThumbnail(character.getAvatarURL());

        return builder.build();
    }
    
}
