package skademaskinen.Commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
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
import skademaskinen.WorldOfWarcraft.PvpTeam;

public class Pvp extends Raid {
    
    public static CommandData configure(){
        SlashCommandData command = Commands.slash(Pvp.class.getSimpleName().toLowerCase(), "Admin command: Handle the pvp team");
        SubcommandData add = new SubcommandData("add", "Add a user to the pvp team manually");
        OptionData raider = new OptionData(OptionType.USER, "user", "Mention of the user", true);
        OptionData name = new OptionData(OptionType.STRING, "name", "Character name", true, true);
        OptionData server = new OptionData(OptionType.STRING, "server", "Character server", false, true);
        OptionData role = new OptionData(OptionType.STRING, "role", "Character role", true, true);
        add.addOptions(raider,name,role,server);
        SubcommandData remove = new SubcommandData("remove", "Remove a user from the pvp team manually");
        remove.addOptions(raider);
        SubcommandData update = new SubcommandData("update", "Update the pvp team message");
        SubcommandData form = new SubcommandData("form", "Create a pvp team application form");
        command.addSubcommands(add,remove,update,form);
        return command;
    }

    public Pvp(ButtonInteractionEvent event) {
        super(event);
    }
    public Pvp(ModalInteractionEvent event) {
        super(event);
    }
    public Pvp(SlashCommandInteractionEvent event) {
        super(event);
    }

    public Pvp(CommandAutoCompleteInteractionEvent event) {
        super(event);
    }

    @Override
    public Object ButtonExecute(ButtonInteractionEvent event) {
        Object result;
        switch(event.getComponentId().split("::")[1]){
            case "apply":
                TextInput name = TextInput.create("name", "Character name", TextInputStyle.SHORT)
                    .setPlaceholder("Your character name")
                    .build();
                TextInput server = TextInput.create("server", "Character server", TextInputStyle.SHORT)
                    .setPlaceholder("Your character server, example: "+Bot.getConfig().get("guildServer"))
                    .setValue(Bot.getConfig().get("guildServer"))
                    .build();
                TextInput role = TextInput.create("role", "Your role", TextInputStyle.SHORT)
                    .setPlaceholder("Healer, Tank, Ranged Damage or Melee Damage")
                    .build();
    
                Modal modal = Modal.create(buildSubId("modal", null), "Application form")
                    .addActionRows(ActionRow.of(name), ActionRow.of(role), ActionRow.of(server))
                    .build();
                success = true;
                result = modal;
                break;
            case "approve":
                String[] data = event.getComponentId().split("::")[2].split(",");
                PvpTeam.add(event.getUser(), data[0], data[2], data[1]);
                event.getMessageChannel().deleteMessageById(event.getMessageId()).queue();
                success = true;
                result = "Successfully added user: `"+data[0]+"` to pvp team";
                break;
            case "decline":
                String[] data1 = event.getComponentId().split("::")[2].split(",");
                event.getMessageChannel().deleteMessageById(event.getMessageId()).queue();
                success = true;
                result = "Successfully declined application for: `"+data1[0]+"`!";
                break;
            default:
                success = false;
                result = "Error, invalid button identified by id: "+event.getComponentId();
                break;
        }
        log(success, new String[]{event.getComponentId().split("::")[1]});
        return result;
    }
    @Override
    public Object ModalExecute(ModalInteractionEvent event) {
        String name = event.getValue("name").getAsString();
        String server = event.getValue("server").getAsString().toLowerCase().replace(" ", "-");
        String role = event.getValue("role").getAsString();

        if(!BattleNetAPI.verifyCharacter(name, server)){
            success = false;
            log(success, new String[]{name+", "+server+", "+role});
            return "Error, this character is not valid - check the name or server, or check your battle.net account's security settings";
        }
        if(!role.toLowerCase().matches("tank|healer|melee damage|ranged damage")){
            success = false;
            log(success, new String[]{name+", "+server+", "+role});
            return "Error, the specified role does not match any of the valid roles!";
        }

        Character character = new Character(name, server);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(this.getClass().getSimpleName()+" team application");
        builder.setAuthor(event.getMember().getEffectiveName(), "https://discordapp.com/users/"+event.getMember().getId(), event.getMember().getEffectiveAvatarUrl());
        builder.setDescription("----------------------------");
        builder.appendDescription("\n**Name**: " +"["+name+"](https://worldofwarcraft.com/en-gb/character/eu/"+server+"/"+name+") ("+Utils.capitalize(server.replace("-", " "))+")");
        builder.appendDescription("\n**Class**: "+character._getClass());
        builder.appendDescription("\n**Specialization**: "+character.getSpecialization());
        builder.appendDescription("\n**Role**: "+Utils.capitalize(role));
        builder.appendDescription("\n**Item level**: "+character.getIlvl()+"/"+character.getAverageIlvl());
        builder.appendDescription("\n----------------------------");
        builder.setThumbnail(character.getAvatarURL());

        int score = 0;
        List<String> filled = Arrays.asList(Bot.getConfig().get(this.getClass().getSimpleName().toLowerCase() + ":filled").split(","));
        List<String> preferred = Arrays.asList(Bot.getConfig().get(this.getClass().getSimpleName().toLowerCase() + ":preferred").split(","));
        List<String> needed = Arrays.asList(Bot.getConfig().get(this.getClass().getSimpleName().toLowerCase() + ":needed").split(","));

        int ilvl = Integer.parseInt(Bot.getConfig().get(this.getClass().getSimpleName().toLowerCase() + ":ilvl"));
        List<Field> fields = new ArrayList<>();
        if(!filled.contains(role.toLowerCase())){
            if(character.getAverageIlvl() >= ilvl) score++;
            else fields.add(new Field("Too low item level", "", true));
            if(preferred.contains(character._getClass().toLowerCase())) score++;
            else fields.add(new Field("We are not actively looking for:\n"+character._getClass(), "", true));
            if(needed.contains(role.toLowerCase())) score++;
            else fields.add(new Field("We do not need any more:\n"+role, "", true));
        }
        else fields.add(new Field("We do not need any more:\n"+role, "", false));
        
        for(Field field : fields) builder.addField(field);
        builder.setColor(score >= 2 ? Color.GREEN : (score > 0 ? Color.YELLOW : Color.RED));

        actionRows.add(ActionRow.of(
            Button.success(buildSubId("approve", name+","+server+","+role), "Approve"),
            Button.danger(buildSubId("decline", name+","+server+","+role), "Decline")
        ));
        success = true;
        log(success, new String[]{name+", "+server+", "+role});
        return builder.build();
    }
    
    public Object run(SlashCommandInteractionEvent event) {
        Object result = "";
        switch(event.getSubcommandName()){
            case "add":
                result = event.getOption("server") == null ? 
                    add(event.getOption("user").getAsUser(), event.getOption("name").getAsString(), event.getOption("role").getAsString(), Bot.getConfig().get("guildServer")) :
                    add(event.getOption("user").getAsUser(), event.getOption("name").getAsString(), event.getOption("role").getAsString(), event.getOption("server").getAsString());

                break;
            case "remove":
                result = remove(event.getOption("raider").getAsUser());
                break;
            case "update":
                result = PvpTeam.update();
                break;
            case "form":
                result = form();
        }                
        if(result == null){
            success = false;
            return "Command failed!";
        }
        success = true;
        return result;
    }
    private String add(User user, String name, String role, String server){
        success = PvpTeam.add(user,name,role,server);
        if(success){
            return "Successfully added member to raid team!";
        }
        else{
            return "Error, failed to add member to raid team!";
        }
    }
    private String remove(User user){
        PvpTeam.remove(user);
        return "Successfully removed user from raid team!";
    }
}
