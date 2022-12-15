package skademaskinen.Features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

public class Newrolepicker implements Feature {
    private boolean success;
    private boolean requiresAdmin;
    List<Category> categories = new ArrayList<>();
    private List<ActionRow> actionRows = new ArrayList<>();
    private boolean isEphemeral;
    private Guild guild;
    
    public static CommandData configure(){
        return Commands.slash(Newrolepicker.class.getSimpleName().toLowerCase(), "Rolepicker command group").addSubcommands(
            new SubcommandData("create", "Create a rolepicker message"),
            new SubcommandData("addcategory", "Add a new category")
                .addOption(OptionType.STRING, "name", "The name of the category to be added"),
            new SubcommandData("removecategory", "Removes a category")
                .addOption(OptionType.STRING, "name", "The name of the category"),
            new SubcommandData("addrole", "Adds a role to a category")
                .addOption(OptionType.STRING, "category", "Name of the category")
                .addOption(OptionType.MENTIONABLE, "role", "The role to be added"),
            new SubcommandData("removerole", "removes a role from a category")
                .addOption(OptionType.STRING, "category", "Name of the category")
                .addOption(OptionType.MENTIONABLE, "role", "The role to be removed")
            );
    }

    public Newrolepicker(SlashCommandInteractionEvent event) {
        guild = event.getGuild();
        try(BufferedReader reader = new BufferedReader(new FileReader(new File("files/rolepicker.json")))){
            JSONObject object = new JSONObject(new JSONTokener(reader));
            for(String key : object.getJSONObject(guild.getId()).keySet()){
                JSONArray roles = object.getJSONObject(guild.getId()).getJSONArray(key);
                List<GuildRole> guildRoles = new ArrayList<>();
                for(Object obj : roles){
                    JSONObject role = (JSONObject) obj;
                    guildRoles.add(new GuildRole(role.getString("id"), role.getString("name"), role.getString("description"), role.getString("emoji")));
                }
                categories.add(new Category(key, guildRoles));
            }

        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }    

    public Newrolepicker(StringSelectInteractionEvent event) {
        isEphemeral = true;
    }

    @Override
    public boolean isSuccess() {
        return success;
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
                if(emoji != null) builder.addOption(name, id, description, Emoji.fromFormatted(emoji));
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
        builder.setTitle("Welcome to "+Bot.getConfig().get("guild:name")+"!");
        try{

            builder.setImage(Bot.getConfig().get("guild:image"));
            builder.setDescription("Hi, below is some menus you can scroll through and pick the roles you like :)\nSome of these roles will get occational pings!");
            builder.setFooter(event.getGuild().getName());
            
            for(Category category : categories){
                actionRows.add(category.build());
            }
        }
        catch(Exception e){
            Shell.exceptionHandler(e);

        }
        return builder.build();
    
    }

    private JSONObject read(){
        try(FileReader reader = new FileReader(new File("files/rolepicker.json"))){
            return new JSONObject(new JSONTokener(reader));
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
            return null;
        }
    }

    private void write(){
        try(FileWriter writer = new FileWriter(new File("files/rolepicker.json"))){
            JSONObject current = read().getJSONObject(guild.getId());
            for(Category category : categories){
                JSONArray array = new JSONArray();
                for(GuildRole role : category.roles){
                    JSONObject object = new JSONObject();
                    object.put("id", role.getId());
                    object.put("name", role.getName());
                    object.put("description", role.getDescription());
                    object.put("emoji", role.getEmoji());
                    array.put(object);
                }
                current.put(category.name, array);
            }
            writer.write(current.toString(4));
            

        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }
    }
}
