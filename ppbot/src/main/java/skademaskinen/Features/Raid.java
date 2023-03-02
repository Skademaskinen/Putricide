package skademaskinen.Features;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
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
import skademaskinen.WorldOfWarcraft.RaidTeam;

/**
 * This class handles the raid commands to manage the raid team
 */
public class Raid implements Feature {
    protected boolean success = false;
    private boolean shouldEphemeral = true;
    protected List<ActionRow> actionRows = new ArrayList<>();
    private boolean defer = true;
    private boolean requiresAdmin = true;

    /**
     * The method to configure a given command, this must be implemented as a static method of each command
     * @return All command data to register a command in discord
     */
    public static CommandData configure(){
        SlashCommandData command = Commands.slash(Raid.class.getSimpleName().toLowerCase(), "Admin command: Handle the raid team");
        SubcommandData add = new SubcommandData("add", "Add a raider to the raid team manually");
        OptionData raider = new OptionData(OptionType.USER, "user", "Mention of the user", true);
        OptionData name = new OptionData(OptionType.STRING, "name", "Character name", true, true);
        OptionData server = new OptionData(OptionType.STRING, "server", "Character server", false, true);
        OptionData role = new OptionData(OptionType.STRING, "role", "Character role", true, true);
        OptionData notes = new OptionData(OptionType.STRING, "notes", "Notes for this raider");
        OptionData bench = new OptionData(OptionType.BOOLEAN, "bench", "Whether this user should be benched", false);
        add.addOptions(raider, name, role, server, notes, bench);
        SubcommandData remove = new SubcommandData("remove", "Remove a raider from the raid team manually");
        remove.addOptions(raider);
        SubcommandData edit = new SubcommandData("edit", "Edit a single raider's data");
        edit.addOptions(raider, name.setRequired(false), role.setRequired(false), server.setRequired(false), notes.setRequired(false).setDescription("Notes for this user, (% to clear)"), bench);
        SubcommandData update = new SubcommandData("update", "Update the raid team message");
        SubcommandData form = new SubcommandData("form", "Create a raid team application form");
        SubcommandData configure = new SubcommandData("configure", "Configure the requirements for the raid team");
        command.addSubcommands(add, remove, edit, update, form, configure);
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

    /**
     * This is the constructor for a button interaction event
     * @param event The button interaction event
     */
    public Raid(ButtonInteractionEvent event) {
        if(getSubId(event).equals("apply") || getSubId(event).matches("teamErrorName|teamErrorServer")){
            defer = false;
            shouldEphemeral = false;
            requiresAdmin = false;
        }
        else{
            defer = true;
            shouldEphemeral = true;
            requiresAdmin = true;
        }
    }

    /**
     * This is the constructor for the modal interaction event
     * @param event The modal interaction event
     */
    public Raid(ModalInteractionEvent event){
        defer = true;
        shouldEphemeral = event.getModalId().split("::")[1].equals("configure");
        requiresAdmin = false;
    }

    /**
     * This is the constructor for a slash command interaction event
     * @param event This is the slash command interaction event
     */
    public Raid(SlashCommandInteractionEvent event){
        switch(event.getSubcommandName()){
            case "add":
            case "remove":
            case "update":
                shouldEphemeral = true;
                break;
            case "form":
                shouldEphemeral = false;
                break;
            case "configure":
                defer = false;
                break;
        }
    }

    /**
     * This is the constructor for a auto complete interaction event
     * @param event This is the auto complete interaction event
     */
    public Raid(CommandAutoCompleteInteractionEvent event) {
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

    public Object update(SlashCommandInteractionEvent event) {
        return RaidTeam.update(event.getGuild());
    }

    /**
     * This is the method to add a user to the raid team
     * @param user The Discord user to be added
     * @param name The name of the discord user's wow character
     * @param role The role ingame this character has
     * @param server The server this character is on
     * @return A message showing the result of this command
     */
    public String add(SlashCommandInteractionEvent event){
        success = RaidTeam.add(event.getOption("user").getAsMember(), 
            event.getOption("name").getAsString(),
            event.getOption("role").getAsString(),
            event.getOption("server") == null ? ServerConfig.get(event.getGuild()).getString("realm").toLowerCase().replace(" ", "-") : event.getOption("server").getAsString(),
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
     * @param event The Discord user to be removed
     * @return A message showing the result of the command
     */
    public String remove(SlashCommandInteractionEvent event){
        RaidTeam.remove(event.getOption("user").getAsMember());
        return "Successfully removed user from raid team!";
    }

    /**
     * This method returns a message that can spawn a modal to apply to the raid team
     * @return THe message embed to be used to apply to the raid team
     */
    public MessageEmbed form(SlashCommandInteractionEvent event){
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject config = ServerConfig.get(event.getGuild());
        String guildName = config.getString("name");
        builder.setTitle("Apply to the "+this.getClass().getSimpleName().toLowerCase()+" team of "+guildName+"!");
        builder.setDescription("Hi, here you can apply to the "+this.getClass().getSimpleName().toLowerCase()+" team!\nYou will receive a pop-up form to add your character's details.");
        if(config.getString("image") != null){
            builder.setImage(config.getString("image"));
        }
        actionRows.add(ActionRow.of(Button.primary(buildSubId("apply", null), "Apply here!")));
        return builder.build();
    }

    public Object edit(SlashCommandInteractionEvent event){
        try{

            for(OptionMapping option : event.getOptions()){
                switch(option.getName()){
                    case "name":
                    RaidTeam.editName(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                    case "server":
                    RaidTeam.editServer(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                    case "notes":
                    RaidTeam.editNote(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                    case "role":
                    RaidTeam.editRole(event.getOption("user").getAsMember(), option.getAsString());
                    break;
                    case "bench":
                    RaidTeam.editBench(event.getOption("user").getAsMember(), option.getAsBoolean());
                    default:
                    continue;
                }
            }
            RaidTeam.update(event.getGuild());
            return "Successfully edited user's entry in the "+this.getClass().getSimpleName()+" team";
        }
        catch(Exception e){
            Shell.exceptionHandler(e, event.getGuild());
            return "Error, contact Skademanden!";
        }
    }


    @Override
    public Object run(ButtonInteractionEvent event) {
        Object result;
        JSONObject config;
        if(event.isFromGuild()) config = ServerConfig.get(event.getGuild());
        else config = new JSONObject();
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
                TextInput raidtimes = TextInput.create("raidtimes", "Wednesday and Sunday 19:30 - 22:30?", TextInputStyle.SHORT)
                    .setPlaceholder("Can you raid with us? (yes/no)")
                    .setValue("yes")
                    .build();
    
                Modal modal = Modal.create(buildSubId("modal", null), "Application form")
                    .addActionRows(ActionRow.of(name), ActionRow.of(role), ActionRow.of(server), ActionRow.of(raidtimes))
                    .build();
                success = true;
                result = modal;
                break;
            case "approve":
                String[] data = event.getComponentId().split("::")[2].split(",");
                RaidTeam.add(event.getGuild().retrieveMemberById(data[0]).complete(), data[1], data[2], data[3], null, false);
                event.getMessageChannel().deleteMessageById(event.getMessageId()).queue();
                success = true;
                result = "Successfully added raider: `"+data[1]+"` to raid team";
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
                RaidTeam.remove(Bot.getJda().getGuildById(guildId).getMemberById(event.getUser().getId()));
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
        boolean raidtimes = event.getValue("raidtimes").getAsString().strip().equalsIgnoreCase("yes") ? true : false;

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
        builder.appendDescription("\n**Availability**: "+ (raidtimes ? "yes" : "no"));
        builder.appendDescription("\n----------------------------");
        builder.setThumbnail(character.getAvatarURL());

        int score = 0;
        List<Object> filled = config.getJSONObject("raid").getJSONArray("filled").toList();
        List<Object> preferred = config.getJSONObject("raid").getJSONArray("preferred").toList();
        int ilvl = config.getJSONObject("raid").getInt("ilvl");
        List<Field> fields = new ArrayList<>();
        if(!filled.contains(role.toLowerCase())){
            if(character.getAverageIlvl() >= ilvl) score++;
            else fields.add(new Field("Too low item level", "", true));
            if(preferred.contains(character._getClass().toLowerCase())) score++;
            else fields.add(new Field("We are not actively looking for:\n"+character._getClass(), "", true));
            if(raidtimes) score++;
        }
        else fields.add(new Field("We do not need any more:\n"+role, "", false));
        
        for(Field field : fields) builder.addField(field);
        builder.setColor(score == 3 ? Color.GREEN : (score > 0 ? Color.YELLOW : Color.RED));

        actionRows.add(ActionRow.of(
            Button.success(buildSubId("approve", event.getMember().getId()+","+name+","+role+","+server), "Approve"),
            Button.danger(buildSubId("decline", event.getMember().getId()+","+name+","+role+","+server), "Decline")
        ));

        success = true;
        log(success, new String[]{event.getMember().getId()+","+name+","+role+","+server});
        return builder.build();
    }
    private Object teamErrorName(ModalInteractionEvent event) {
        String name = event.getValues().get(0).getAsString();
        Guild guild = Bot.getJda().getGuildById(event.getModalId().split("::")[2]);
        Member member = guild.getMemberById(event.getUser().getId());
        RaidTeam.editName(member, name);
        success = true;
        RaidTeam.update(guild);
        return "Successfully updated name of your character!";


    }
    
    private Object teamErrorServer(ModalInteractionEvent event) {
        String name = event.getValues().get(0).getAsString();
        Guild guild = Bot.getJda().getGuildById(event.getModalId().split("::")[2]);
        Member member = guild.getMemberById(event.getUser().getId());
        RaidTeam.editName(member, name);
        success = true;
        RaidTeam.update(guild);
        return "Successfully updated server of your character!";
    }

    /**
     * This method runs the configure modal
     * @param event The object containing information about the event
     * @return A response to be shown in discord
     */
    protected Object configureModal(ModalInteractionEvent event) {
        String className = this.getClass().getSimpleName().toLowerCase();
        String[] entries = event.getValues().get(0).getAsString().split("\n");
        for(String entry : entries){
            String key = entry.split(": ")[0];
            String value = entry.split(": ").length == 2 ?entry.split(": ")[1] : "";
            JSONObject config = ServerConfig.get(event.getGuild());
            switch(key){
                case "Filled Roles":
                    config.getJSONObject("raid").put("filled", value.replace(", ", ",").replace("\"", "").split(","));
                    ServerConfig.write(event.getGuild(), config);
                    break;
                case "Preferred Classes":
                    config.getJSONObject("raid").put("preferred", value.replace(", ", ",").replace("\"", "").split(","));
                    ServerConfig.write(event.getGuild(), config);
                    break;
                case "Minimum Item Level":
                    config.getJSONObject("raid").put("ilvl", value);
                    ServerConfig.write(event.getGuild(), config);
                    break;
                case "Channel ID":
                    config.getJSONObject("raid").put("channel", value);
                    ServerConfig.write(event.getGuild(), config);
                    break;
                case "Message ID":
                    config.getJSONObject("raid").put("message", value);
                    ServerConfig.write(event.getGuild(), config);
                    break;
                default:
                    return "Error, the configuration option: "+key+" is invalid!";
            }
        }
        return "Succesfully configured the "+className+" team";
    }

    /**
     * This method is run when a slash command subcommand has the configure command.
     * @param event The object containing a lot of information about a slash command event
     * @return A response to be shown in discord
     */
    public Object configure(SlashCommandInteractionEvent event){
        String className = this.getClass().getSimpleName().toLowerCase();
        JSONObject config = ServerConfig.get(event.getGuild());
        String filled = config.getJSONObject("raid").getJSONArray("filled").join(", ");
        String preferred = config.getJSONObject("raid").getJSONArray("preferred").join(", ");
        int ilvl = config.getJSONObject("raid").getInt("ilvl");
        String channel = config.getJSONObject("raid").getString("channel");
        String message = config.getJSONObject("raid").getString("message");
        String value = "Filled Roles: "+filled+"\n";
        value+="Preferred Classes: "+preferred+"\n";
        value+="Minimum Item Level: "+ilvl+"\n";
        value+="Channel ID: "+channel+"\n";
        value+="Message ID: "+message;
        TextInput input = TextInput.create("input", "Lists are seperated by ', '", TextInputStyle.PARAGRAPH)
            .setValue(value)
            .build();
        return Modal.create(buildSubId("configure", null), "Configure "+className+" options").addActionRow(input).build();

    }

    @Override
    public List<Choice> run(CommandAutoCompleteInteractionEvent event) {
        switch(event.getFocusedOption().getName()){
            case "role":
                String[] roles = {"Tank", "Healer", "Melee Damage", "Ranged Damage"};
                return Stream.of(roles)
                    .filter(role -> role.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
                    .map(role -> new Choice(role, role))
                    .collect(Collectors.toList());
            case "server":
                JSONObject serverJSON = BattleNetAPI.getRealmData();
                List<String> servers = new ArrayList<>();
                for(Object object : serverJSON.getJSONArray("realms")){
                    servers.add(((JSONObject) object).getString("name"));
                }
                return Stream.of(servers.toArray(new String[0]))
                    .filter(server -> server.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
                    .map(server -> new Choice(server, server))
                    .limit(25)
                    .collect(Collectors.toList());
            case "name":
                JSONObject guildJSON = BattleNetAPI.getGuildMemberList(event.getGuild());
                List<String> members = new ArrayList<>();
                for(Object object : guildJSON.getJSONArray("members")) members.add(((JSONObject) object).getJSONObject("character").getString("name"));
                return Stream.of(members.toArray(new String[0]))
                    .filter(member -> member.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
                    .map(member -> new Choice(member, member))
                    .limit(25)
                    .collect(Collectors.toList());
            default:
                return null;
        }
    }

    @Override
    public boolean requiresAdmin() {
        return requiresAdmin;
    }
    
}
