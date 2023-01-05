package skademaskinen.Features;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import skademaskinen.Bot;
import skademaskinen.Utils.ServerConfig;
import skademaskinen.Utils.Shell;

public class Rolepicker implements Feature {
    private boolean success;
    private boolean requiresAdmin;
    List<Category> categories = new ArrayList<>();
    private List<ActionRow> actionRows = new ArrayList<>();
    private boolean isEphemeral;
    private Guild guild;
    private boolean deferEdit = false;
    private boolean defer = true;
    
    public static CommandData configure(){
        return Commands.slash(Rolepicker.class.getSimpleName().toLowerCase(), "Rolepicker command group").addSubcommands(
            new SubcommandData("create", "Create a rolepicker message"),
            new SubcommandData("addcategory", "Add a new category")
                .addOption(OptionType.STRING, "name", "The name of the category to be added", true),
            new SubcommandData("removecategory", "Removes a category")
                .addOption(OptionType.STRING, "name", "The name of the category", true, true),
            new SubcommandData("addrole", "Adds a role to a category")
                .addOption(OptionType.STRING, "category", "Name of the category", true, true)
                .addOption(OptionType.ROLE, "role", "The role to be added", true)
                .addOption(OptionType.STRING, "name", "The name of the role", false, true)
                .addOption(OptionType.STRING, "description", "The description of the role", false, true)
                .addOption(OptionType.STRING, "emoji", "The emoji to be displayed in the rolepicker", false, true),
            new SubcommandData("editrole", "Edit the specified role for a category")
                .addOption(OptionType.STRING, "category", "The name of the category", true, true)
                .addOption(OptionType.ROLE, "role", "The role to be edited", true)
                .addOption(OptionType.STRING, "name", "The name of the role", false, true)
                .addOption(OptionType.STRING, "description", "The description of the role", false, true)
                .addOption(OptionType.STRING, "emoji", "The emoji to be displayed in the rolepicker", false, true),
            new SubcommandData("removerole", "removes a role from a category")
                .addOption(OptionType.STRING, "category", "Name of the category", true, true)
                .addOption(OptionType.ROLE, "role", "The role to be removed", true),
            new SubcommandData("update", "Updates a rolepicker with the current roles")
                .addOption(OptionType.STRING, "id", "ID of the message", true),
            new SubcommandData("customize", "Customize the rolepicker message")
            );
    }

    public Rolepicker(SlashCommandInteractionEvent event) {
        guild = event.getGuild();
        JSONObject object = ServerConfig.rolepickerGet(guild);
        if(object.keySet().size() == 0){
            object = new JSONObject()
                .put("categories", new JSONObject())
                .put("configuration", new JSONObject()
                    .put("title", "Generic rolepicker")
                    .put("description", "Generic rolepicker description")
                    .put("image", "https://www.interprint-services.co.uk/wp-content/uploads/2019/04/placeholder-banner.png"));
            ServerConfig.rolepickerWrite(guild, object);
        }
        loadCategories(event.getGuild());
        defer = !event.getSubcommandName().equals("customize");
        isEphemeral = !event.getSubcommandName().equals("create");
    }    

    public Rolepicker(CommandAutoCompleteInteractionEvent event) {
        loadCategories(event.getGuild());

    }

    public Rolepicker(StringSelectInteractionEvent event) {
        deferEdit = true;
        isEphemeral = false;
        defer = false;
    }

    private void loadCategories(Guild guild){
        this.guild = guild;
        JSONObject object = ServerConfig.rolepickerGet(guild);
        for(String key : object.getJSONObject("categories").keySet()){
            JSONObject roles = object.getJSONObject("categories").getJSONObject(key);
            List<GuildRole> guildRoles = new ArrayList<>();
            for(String innerKey : roles.keySet()){
                JSONObject role = roles.getJSONObject(innerKey);
                guildRoles.add(new GuildRole(innerKey, role.getString("name"), role.getString("description"), role.getString("emoji")));
            }
            categories.add(new Category(key, guildRoles));
        }

    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public Rolepicker(ModalInteractionEvent event) {
        defer = true;
        isEphemeral = true;
        loadCategories(event.getGuild());
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

    @Override
    public boolean shouldDeferEdit() {
        return deferEdit;
    }
    @Override
    public boolean shouldDefer() {
        return defer;
    }
    
    @Override
    public Object run(StringSelectInteractionEvent event) {
        Member member = event.getMember();
        List<Role> roles = new ArrayList<>(member.getRoles());
        for(SelectOption option : event.getSelectMenu().getOptions()){
            String id = option.getValue();
            roles.remove(event.getGuild().getRoleById(id));
        }

        for(String id : event.getValues()){
            roles.add(event.getGuild().getRoleById(id));
        }

        event.getGuild().modifyMemberRoles(member, roles).queue();
        return "Successfully updated roles for member: "+member.getAsMention();
    }

    @Override
    public List<Choice> run(CommandAutoCompleteInteractionEvent event) {
        List<Choice> reply = new ArrayList<>();
        switch(event.getSubcommandName()){
            case "removecategory":
                for(Category category : categories){
                    reply.add(new Choice(category.name, category.name));
                }
                break;
            case "addrole":
                switch(event.getFocusedOption().getName()){
                    case "category":
                    for(Category category : categories){
                            reply.add(new Choice(category.name, category.name));
                        }
                        break;
                    case "name":
                        String value = guild.getRoleById(event.getOption("role").getAsString()).getName();
                        reply.add(new Choice(value, value));
                        break;
                    case "emoji":
                        List<RichCustomEmoji> emojis = Bot.getJda().getEmojis();
                        emojis = emojis.stream().filter(emoji -> emoji.getName().toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase())).limit(25).toList();
                        for(RichCustomEmoji emoji : emojis) reply.add(new Choice(emoji.getName(), emoji.getFormatted()));
                        if(reply.size() < 25) for(com.vdurmont.emoji.Emoji emoji : EmojiManager.getAll()){
                            if(reply.size() == 25) break;
                            if(emoji.getAliases().stream().filter(alias -> alias.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase())).toList().size() > 0){
                                reply.add(new Choice(emoji.getUnicode(), emoji.getUnicode()));
                            }
                        }
                        break;
                    case "description":
                        if(event.getOption("role") == null) return null;
                        String roleName = guild.getRoleById(event.getOption("role").getAsString()).getName();
                        String[] suggestions = {"The "+roleName+" role", "The "+roleName+" role does this", "The super awesome role called \""+roleName+"\""};
                        for(String suggestion : suggestions){
                            reply.add(new Choice(suggestion, suggestion));
                        }
                        break;
                }
                break;
            case "editrole":
                switch(event.getFocusedOption().getName()){
                    case "category":
                        for(Category category : categories){
                            reply.add(new Choice(category.name, category.name));
                        }
                        break;
                    case "name":
                        String value = guild.getRoleById(event.getOption("role").getAsString()).getName();
                        reply.add(new Choice(value, value));
                        break;
                    case "emoji":
                        List<RichCustomEmoji> emojis = Bot.getJda().getEmojis();
                        emojis = emojis.stream().filter(emoji -> emoji.getName().toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase())).limit(25).toList();
                        for(RichCustomEmoji emoji : emojis) reply.add(new Choice(emoji.getName(), emoji.getFormatted()));
                        if(reply.size() < 25) for(com.vdurmont.emoji.Emoji emoji : EmojiManager.getAll()){
                            if(reply.size() == 25) break;
                            if(emoji.getAliases().stream().filter(alias -> alias.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase())).toList().size() > 0){
                                reply.add(new Choice(emoji.getUnicode(), emoji.getUnicode()));
                            }
                        }
                        break;
                    case "description":
                        if(event.getOption("role") == null) return null;
                        String roleName = guild.getRoleById(event.getOption("role").getAsString()).getName();
                        String[] suggestions = {"The "+roleName+" role", "The "+roleName+" does this", "The super awesome role called \""+roleName+"\""};
                        for(String suggestion : suggestions){
                            reply.add(new Choice(suggestion, suggestion));
                        }
                        break;
                }
                break;
            case "removerole":
                for(Category category : categories){
                    reply.add(new Choice(category.name, category.name));
                }
                break;
        }
        return reply;

    }


    //test data
    //https://cdn.discordapp.com/attachments/642853163774509116/922532262459867196/The_nut_hut.gif
    //Hi, below is some menus you can scroll through and pick the roles you like :)
    //Some of these roles will get occational pings!
    //Welcome to The Nut Hut!


    @Override
    public Object run(ModalInteractionEvent event) {
        //customize
        JSONObject file = ServerConfig.rolepickerGet(guild);
        JSONObject config = file.getJSONObject("configuration");
        for(ModalMapping option : event.getValues()){
            config.put(option.getId(), option.getAsString());
        }
        ServerConfig.rolepickerWrite(guild, file);

        return "Successfully edited message";
    }

    @Override
    public boolean requiresAdmin() {
        return requiresAdmin;
    }

    @Override
    public boolean isEphemeral() {
        return isEphemeral;
    }

    @Override
    public List<ActionRow> getActionRows() {
        return actionRows;
    }

    public class GuildRole{
        private String emoji;
        private String description;
        private String name;
        private String id;

        public GuildRole(String id, String name, String description, String emoji) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.emoji = emoji;
        }

        public String getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getDescription() {
            return description;
        }
        public String getEmoji() {
            return emoji;
        }


    }

    public class Category{
        private List<GuildRole> roles;
        private String name;

        public Category(String name, List<GuildRole> roles){
            this.name = name;
            this.roles = roles;
        }

        public ActionRow build() {
            Builder builder = StringSelectMenu.create(buildSubId(name, null));
            for(GuildRole role : roles){
                String id = role.getId();
                String name = role.getName();
                String description = role.getDescription() != null ? role.getDescription() : "";
                String emoji = role.getEmoji();
                if(!emoji.equals("")) builder.addOption(name, id, description, Emoji.fromFormatted(emoji));
                else builder.addOption(name, id, description);
            }
            builder.setRequiredRange(1, roles.size());
            builder.setPlaceholder(name);
            builder.setMinValues(0);
            return ActionRow.of(builder.build());
        }
    }

    public Object create(SlashCommandInteractionEvent event){
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject config = ServerConfig.rolepickerGet(guild).getJSONObject("configuration");
        builder.setTitle(config.getString("title"));
        builder.setImage(config.getString("image"));
        builder.setDescription(config.getString("description"));
        builder.setFooter(guild.getName());
        
        for(Category category : categories){
            if(category.roles.size() == 0) continue;
            actionRows.add(category.build());
        }
        return builder.build();
    
    }

    private void write(){
        JSONObject current = ServerConfig.rolepickerGet(guild);
        JSONObject value = new JSONObject();
        for(Category category : categories){
            JSONObject inner = new JSONObject();
            for(GuildRole role : category.roles){
                JSONObject object = new JSONObject();
                object.put("name", role.getName());
                object.put("description", role.getDescription());
                object.put("emoji", role.getEmoji());
                inner.put(role.getId(), object);
            }
            value.put(category.name, inner);
        }
        current.put("categories", value);
        ServerConfig.rolepickerWrite(guild, current);
            

    }
    public Object addcategory(SlashCommandInteractionEvent event){
        categories.add(new Category(event.getOption("name").getAsString(), new ArrayList<>()));
        write();
        return "Successfully added category";
    }

    public Object removecategory(SlashCommandInteractionEvent event){
        for(Category category : categories){
            if(category.name.equals(event.getOption("name").getAsString())){
                categories.remove(category);
                write();
                return "Successfully removed category!";
            }
        }
        return "Error, no category found by: " + event.getOption("name").getAsString();
    }

    public Object addrole(SlashCommandInteractionEvent event){
        if(event.getOption("role").getAsRole().getPosition() > event.getGuild().getBotRole().getPosition()){
            return "Error, this role is a higher rank than the bots own role.\nFix this issue by going to: `Server Settings -> Roles` and dragging the `"+guild.getBotRole().getName()+"` role over the `"+event.getOption("role").getAsRole().getName()+"` role.";
        }

        for(Category category : categories){
            if(category.name.equals(event.getOption("category").getAsString())){
                Role role = event.getOption("role").getAsRole();
                String name = event.getOption("name") != null ? event.getOption("name").getAsString() : role.getName();
                String description = event.getOption("description") != null ? event.getOption("description").getAsString() : "";
                String emoji = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : ""; 
                if(emoji.startsWith(":") && emoji.endsWith(":")){
                    emoji = EmojiParser.parseToUnicode(emoji);
                    if(emoji.contains(":")) return "Error, failed to parse emoji";
                }
                category.roles.add(new GuildRole(role.getId(), name, description, emoji));
                write();
                return "Successfully added role";
            }
        }
        return "Error, category doesn't exist";
    }

    public Object editrole(SlashCommandInteractionEvent event){
        for(Category category : categories){
            if(category.name.equals(event.getOption("category").getAsString())){
                for(GuildRole role : category.roles){
                    if(role.getId().equals(event.getOption("role").getAsRole().getId())){
                        for(OptionMapping option : event.getOptions()){
                            switch(option.getName()){
                                case "category":
                                case "role":
                                    continue;
                                case "name":
                                    role.name = option.getAsString();
                                    break;
                                case "description":
                                    role.description = option.getAsString();
                                    break;
                                case "emoji":
                                    role.emoji = option.getAsString();
                                    if(role.getEmoji().startsWith(":") && role.getEmoji().endsWith(":")){
                                        role.emoji = EmojiParser.parseToUnicode(role.getEmoji());
                                        if(role.getEmoji().contains(":")) return "Error, failed to parse emoji";
                                    }
                                    break;
                            }
                        }
                        write();
                        return "Successfully edited role";
                    }
                }
                return "Error, invalid role";
            }
        }
        return "Error, category doesn't exist";
        
    }

    public Object removerole(SlashCommandInteractionEvent event){
        for(Category category : categories){
            if(category.name.equals(event.getOption("category").getAsString())){
                for(GuildRole role : category.roles){ 
                    if(role.getId().equals(event.getOption("role").getAsRole().getId())){
                        category.roles.remove(role);
                        write();
                        return "Successfully removed role!";
                    }
                }
            }
        }
        return "Error, failed to remove role!";
    }

    public Object update(SlashCommandInteractionEvent event){
        String id = event.getOption("id").getAsString();
        net.dv8tion.jda.api.entities.Message message = event.getChannel().asTextChannel().getHistoryAround(id, 1).complete().getMessageById(id);
        
        List<ActionRow> actionRows = new ArrayList<>();
        for(Category category : categories){
            if(category.roles.size() == 0) continue;
            actionRows.add(category.build());
        }
        message.editMessageComponents(actionRows).queue();
        return "Successfully updated rolepicker";
        
    }

    public Object customize(SlashCommandInteractionEvent event){
        JSONObject config = ServerConfig.rolepickerGet(guild).getJSONObject("configuration");
        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT).setValue(config.getString("title")).build();
        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH).setValue(config.getString("description")).build();
        TextInput image = TextInput.create("image", "Image", TextInputStyle.SHORT).setValue(config.getString("image")).build();
        return Modal.create(buildSubId("customize", null), "Customize").addActionRows(ActionRow.of(title), ActionRow.of(description), ActionRow.of(image)).build();
    }
}
