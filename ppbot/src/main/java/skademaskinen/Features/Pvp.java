package skademaskinen.Features;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
import skademaskinen.Utils.ServerConfig;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;
import skademaskinen.WorldOfWarcraft.BattleNetAPI;
import skademaskinen.WorldOfWarcraft.Character;
import skademaskinen.WorldOfWarcraft.PvpTeam;

public class Pvp extends Raid {    
    
    /**
    * The method to configure a given command, this must be implemented as a static method of each command
    * @return All command data to register a command in discord
    */
    public static CommandData configure(){
        SlashCommandData command = Commands.slash(Pvp.class.getSimpleName().toLowerCase(), "Admin command: Handle the pvp team");
        SubcommandData add = new SubcommandData("add", "Add a user to the pvp team manually");
        OptionData raider = new OptionData(OptionType.USER, "user", "Mention of the user", true);
        OptionData name = new OptionData(OptionType.STRING, "name", "Character name", true, true);
        OptionData server = new OptionData(OptionType.STRING, "server", "Character server", false, true);
        OptionData role = new OptionData(OptionType.STRING, "role", "Character role", true, true);
        OptionData notes = new OptionData(OptionType.STRING, "notes", "Notes for this raider");
        OptionData bench = new OptionData(OptionType.BOOLEAN, "bench", "Whether this user should be benched", false);
        add.addOptions(raider, name, role, server, notes, bench);
        SubcommandData remove = new SubcommandData("remove", "Remove a user from the pvp team manually");
        SubcommandData edit = new SubcommandData("edit", "Edit a single raider's data");
        edit.addOptions(raider, name.setRequired(false), role.setRequired(false), server.setRequired(false), notes.setRequired(false).setDescription("Notes for this user, (% to clear)"), bench);
        remove.addOptions(raider);
        SubcommandData update = new SubcommandData("update", "Update the pvp team message")
            .addOption(OptionType.BOOLEAN, "ask", "Ask users with errors in a DM", false);
        SubcommandData form = new SubcommandData("form", "Create a pvp team application form");
        SubcommandData configure = new SubcommandData("configure", "Configure the requirements for the raid team");
        command.addSubcommands(add, remove, edit, update, form, configure);
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
    public Object run(ButtonInteractionEvent event) {
        JSONObject config;
        if(event.isFromGuild()) config = ServerConfig.get(event.getGuild());
        else config = new JSONObject();
        Object result;
        switch(event.getComponentId().split("::")[1]){
            case "apply":
                TextInput name = TextInput.create("name", "Character name", TextInputStyle.SHORT)
                    .setPlaceholder("Your character name")
                    .build();
                TextInput server = TextInput.create("server", "Character server", TextInputStyle.SHORT)
                    .setPlaceholder("Your character server, example: "+config.getString("realm"))
                    .setValue(config.getString("realm"))
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
                PvpTeam.add(event.getGuild().retrieveMemberById(data[0]).complete(), data[1], data[2], data[3], null, false);
                event.getMessageChannel().deleteMessageById(event.getMessageId()).queue();
                success = true;
                result = "Successfully added user: `"+data[1]+"` to pvp team";
                break;
            case "decline":
                String[] data1 = event.getComponentId().split("::")[2].split(",");
                event.getMessageChannel().deleteMessageById(event.getMessageId()).queue();
                success = true;
                result = "Successfully declined application for: `"+data1[1]+"`!";
                break;
            case "teamErrorName":
                String id = event.getComponentId().split("::")[2].split(",")[0];
                modal = Modal.create(buildSubId("teamErrorName", id), "Edit character name")
                    .addActionRow(TextInput.create("Name", "name", TextInputStyle.SHORT).build())
                    .build();
                result = modal;
                success = true;
                break;
            case "teamErrorServer":
                String id2 = event.getComponentId().split("::")[2].split(",")[0];
                modal = Modal.create(buildSubId("teamErrorServer", id2), "Edit character server")
                    .addActionRow(TextInput.create("Server", "server", TextInputStyle.SHORT).build())
                    .build();
                result = modal;
                success = true;
                break;
            case "teamErrorRemove":
                String guildId = event.getComponentId().split("::")[2].split(",")[0];
                PvpTeam.remove(Bot.getJda().getGuildById(guildId).getMemberById(event.getUser().getId()));
                result = "Successfully removed you from the team!";
                success = true;
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
    public Object run(ModalInteractionEvent event) {
        if(getSubId(event).equals("configure")) return configureModal(event);
        if(getSubId(event).equals("teamErrorName")) return teamErrorName(event);
        if(getSubId(event).equals("teamErrorServer")) return teamErrorServer(event);
        JSONObject config = ServerConfig.get(event.getGuild());
        String name = event.getValue("name").getAsString().toLowerCase().strip();
        String server = event.getValue("server").getAsString().toLowerCase().replace(" ", "-").strip();
        String role = event.getValue("role").getAsString().strip();

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
        List<Object> filled = config.getJSONObject("pvp").getJSONArray("filled").toList();
        List<Object> preferred = config.getJSONObject("pvp").getJSONArray("preferred").toList();
        int ilvl = config.getJSONObject("pvp").getInt("ilvl");

        List<Field> fields = new ArrayList<>();
        if(!filled.contains(role.toLowerCase())){
            if(character.getAverageIlvl() >= ilvl) score++;
            else fields.add(new Field("Too low item level", "", true));
            if(preferred.contains(character._getClass().toLowerCase())) score++;
            else fields.add(new Field("We are not actively looking for:\n"+character._getClass(), "", true));
        }
        else fields.add(new Field("We do not need any more:\n"+role, "", false));
        
        for(Field field : fields) builder.addField(field);
        builder.setColor(score >= 2 ? Color.GREEN : (score > 0 ? Color.YELLOW : Color.RED));

        actionRows.add(ActionRow.of(
            Button.success(buildSubId("approve", event.getMember().getId()+","+name+","+role+","+server), "Approve"),
            Button.danger(buildSubId("decline", event.getMember().getId()+","+name+","+role+","+server), "Decline")
        ));
        success = true;
        log(success, new String[]{name+", "+server+", "+role});
        return builder.build();
    }
    private Object teamErrorName(ModalInteractionEvent event) {
        String name = event.getValues().get(0).getAsString();
        Guild guild = Bot.getJda().getGuildById(event.getModalId().split("::")[2]);
        Member member = guild.getMemberById(event.getUser().getId());
        PvpTeam.editName(member, name);
        success = true;
        PvpTeam.update(guild, false);
        return "Successfully updated name of your character!";
    }

    private Object teamErrorServer(ModalInteractionEvent event) {
        String server = event.getValues().get(0).getAsString();
        Guild guild = Bot.getJda().getGuildById(event.getModalId().split("::")[2]);
        Member member = guild.getMemberById(event.getUser().getId());
        PvpTeam.editServer(member, server);
        success = true;
        PvpTeam.update(guild, false);
        return "Successfully updated server of your character!";
    }
    
    @Override
    public Object run(SlashCommandInteractionEvent event) {
        try {
            return subCommandLoader(event).invoke(this, event);
        } catch (Exception e) {
            Shell.exceptionHandler(e, event.getGuild());
            return null;
        }
    }

    /**
     * This is the method to add a user to the pvp team
     * @param user The Discord user to be added
     * @param name The name of the discord user's wow character
     * @param role The role ingame this character has
     * @param server The server this character is on
     * @return A message showing the result of this command
     */
    public String add(SlashCommandInteractionEvent event){
        JSONObject config = ServerConfig.get(event.getGuild());
        success = PvpTeam.add(event.getOption("user").getAsMember(), 
            event.getOption("name").getAsString(),
            event.getOption("role").getAsString(),
            event.getOption("server") == null ? config.getString("realm").toLowerCase().replace(" ", "-") : event.getOption("server").getAsString(),
            event.getOption("notes") == null ? null : event.getOption("notes").getAsString(),
            event.getOption("bench") == null ? false : event.getOption("bench").getAsBoolean());
        
            if(success){
            return "Successfully added member to raid team!";
        }
        else{
            return "Error, failed to add member to raid team!";
        }
    }

    /**
     * This is the method to remove a user from the raid team
     * @param user The Discord user to be removed
     * @return A message showing the result of the command
     */
    public String remove(SlashCommandInteractionEvent event){
        PvpTeam.remove(event.getOption("user").getAsMember());
        return "Successfully removed user from raid team!";
    }

    public Object update(SlashCommandInteractionEvent event){
        return PvpTeam.update(event.getGuild(), event.getOption("ask") != null ? event.getOption("ask").getAsBoolean() : false);
    }
    public Object edit(SlashCommandInteractionEvent event){
        for(OptionMapping option : event.getOptions()){
            switch(option.getName()){
                case "name":
                    PvpTeam.editName(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                case "server":
                    PvpTeam.editServer(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                case "notes":
                    PvpTeam.editNote(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                case "role":
                    PvpTeam.editRole(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                case "bench":
                    PvpTeam.editBench(event.getOption("user").getAsMember(), option.getAsBoolean());
                    break;
            }
        }
        PvpTeam.update(event.getGuild(), false);
        return "Successfully edited user's entry in the "+this.getClass().getSimpleName()+" team";
    }
}
