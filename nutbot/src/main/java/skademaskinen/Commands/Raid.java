package skademaskinen.Commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
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
import skademaskinen.Utils.Utils;
import skademaskinen.WorldOfWarcraft.BattleNetAPI;
import skademaskinen.WorldOfWarcraft.Character;
import skademaskinen.WorldOfWarcraft.RaidTeam;

/**
 * This class handles the raid commands to manage the raid team
 */
public class Raid implements Command {
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
        add.addOptions(raider,name,role,server);
        SubcommandData remove = new SubcommandData("remove", "Remove a raider from the raid team manually");
        remove.addOptions(raider);
        SubcommandData update = new SubcommandData("update", "Update the raid team message");
        SubcommandData form = new SubcommandData("form", "Create a raid team application form");
        SubcommandData configure = new SubcommandData("configure", "Configure the requirements for the raid team");
        command.addSubcommands(add,remove,update,form, configure);
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
        defer = !event.getComponentId().split("::")[1].equals("apply");
        shouldEphemeral = !event.getComponentId().split("::")[1].equals("apply");
        switch(event.getComponentId()){
            case "apply":
                requiresAdmin = false;
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

    public Object run(SlashCommandInteractionEvent event) {
        Object result = "";
        switch(event.getSubcommandName()){
            case "add":
                result = event.getOption("server") == null ? 
                    add(event.getOption("raider").getAsUser(), event.getOption("name").getAsString(), event.getOption("role").getAsString(), Bot.getConfig().get("guild:realm")) :
                    add(event.getOption("raider").getAsUser(), event.getOption("name").getAsString(), event.getOption("role").getAsString(), event.getOption("server").getAsString());
                
                break;
            case "remove":
                result = remove(event.getOption("user").getAsUser());
                break;
            case "update":
                result = RaidTeam.update();
                break;
            case "form":
                result = form();
                break;
            case "configure":
                result = configureCommand(event);
                break;
        }                
        if(result == null){
            success = false;
            return "Command failed!";
        }
        success = true;
        return result;
    }

    /**
     * This is the method to add a user to the raid team
     * @param user The Discord user to be added
     * @param name The name of the discord user's wow character
     * @param role The role ingame this character has
     * @param server The server this character is on
     * @return A message showing the result of this command
     */
    private String add(User user, String name, String role, String server){
        success = RaidTeam.add(user,name,role,server);
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
    private String remove(User user){
        RaidTeam.remove(user);
        return "Successfully removed user from raid team!";
    }

    /**
     * This method returns a message that can spawn a modal to apply to the raid team
     * @return THe message embed to be used to apply to the raid team
     */
    protected MessageEmbed form(){
        EmbedBuilder builder = new EmbedBuilder();
        String guildName = Bot.getConfig().get("guild:name");
        builder.setTitle("Apply to the "+this.getClass().getSimpleName().toLowerCase()+" team of "+guildName+"!");
        builder.setDescription("Hi, here you can apply to the "+this.getClass().getSimpleName().toLowerCase()+" team!\nYou will receive a pop-up form to add your character's details.");
        if(Bot.getConfig().get("guild:image") != null){
            builder.setImage(Bot.getConfig().get("guild:image"));
        }
        actionRows.add(ActionRow.of(Button.primary(buildSubId("apply", null), "Apply here!")));
        return builder.build();
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
                    .setPlaceholder("Your character server, example: "+Bot.getConfig().get("guild:realm"))
                    .setValue(Bot.getConfig().get("guild:realm"))
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
                RaidTeam.add(event.getUser(), data[0], data[2], data[1]);
                event.getMessageChannel().deleteMessageById(event.getMessageId()).queue();
                success = true;
                result = "Successfully added raider: `"+data[0]+"` to raid team";
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
        if(event.getModalId().split("::")[1].equals("configure")) return configureModal(event);
        String name = event.getValue("name").getAsString();
        String server = event.getValue("server").getAsString().toLowerCase().replace(" ", "-");
        String role = event.getValue("role").getAsString();
        boolean raidtimes = event.getValue("raidtimes").getAsString().equalsIgnoreCase("yes") ? true : false;

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
            if(raidtimes) score++;
        }
        else fields.add(new Field("We do not need any more:\n"+role, "", false));
        
        for(Field field : fields) builder.addField(field);
        builder.setColor(score >= 3 ? Color.GREEN : (score > 0 ? Color.YELLOW : Color.RED));

        actionRows.add(ActionRow.of(
            Button.success(buildSubId("approve", name+","+server+","+role), "Approve"),
            Button.danger(buildSubId("decline", name+","+server+","+role), "Decline")
        ));

        success = true;
        log(success, new String[]{name+", "+server+", "+role});
        return builder.build();
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
            String value = entry.split(": ")[1];
            String configKey;
            switch(key){
                case "Filled Roles":
                    configKey = className+":filled";
                    break;
                case "Preferred Classes":
                    configKey = className+":preferred";
                    break;
                case "Needed Roles":
                    configKey = className+":needed";
                    break;
                case "Minimum Item Level":
                    configKey = className+":ilvl";
                    break;
                case "Channel ID":
                    configKey = className+":channel";
                    break;
                case "Message ID":
                    configKey = className+":message";
                    break;
                default:
                    return "Error, the configuration option: "+key+" is invalid!";
            }
            Bot.getConfig().set(configKey, value);
        }
        Bot.getConfig().write();
        return "Succesfully configured the "+className+" team";
    }

    /**
     * This method is run when a slash command subcommand has the configure command.
     * @param event The object containing a lot of information about a slash command event
     * @return A response to be shown in discord
     */
    protected Object configureCommand(SlashCommandInteractionEvent event){
        String className = this.getClass().getSimpleName().toLowerCase();
        String filled = Bot.getConfig().get(className+":"+"filled");
        String preferred = Bot.getConfig().get(className+":"+"preferred");
        String needed = Bot.getConfig().get(className+":"+"needed");
        String ilvl = Bot.getConfig().get(className+":"+"ilvl");
        String channel = Bot.getConfig().get(className+":"+"channel");
        String message = Bot.getConfig().get(className+":"+"message");
        String value = "Filled Roles: "+filled+"\n";
        value+="Preferred Classes: "+preferred+"\n";
        value+="Needed Roles: "+needed+"\n";
        value+="Minimum Item Level: "+ilvl+"\n";
        value+="Channel ID: "+channel+"\n";
        value+="Message ID: "+message;
        TextInput input = TextInput.create("input", "Filled, preferred and needed has to be comma seperated", TextInputStyle.PARAGRAPH)
            .setValue(value)
            .build();
        return Modal.create(buildSubId("configure", null), "Configure "+className+" options").addActionRow(input).build();

    }

    @Override
    public List<Choice> AutoComplete(CommandAutoCompleteInteractionEvent event) {
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
                JSONObject guildJSON = BattleNetAPI.getGuildMemberList();
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
