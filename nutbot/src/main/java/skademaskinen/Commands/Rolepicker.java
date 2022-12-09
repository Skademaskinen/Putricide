package skademaskinen.Commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

public class Rolepicker implements Command{
    String filepath = "files/rolepicker.conf";
    List<Category> categories = new ArrayList<>();
    List<ActionRow> actionRows = new ArrayList<>();
    private boolean isEphemeral;

    public static CommandData configure(){
        return Commands.slash(Rolepicker.class.getSimpleName().toLowerCase(), "Create a rolepicker for the guild");
    }

    public Rolepicker(SlashCommandInteractionEvent event) {
        isEphemeral = false;
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)))){
            String title = "";
            String categoryRaw = "";
            for(String line : reader.lines().toList()){
                if(line.startsWith("[") && line.endsWith("]")) title = line.replace("[", "").replace("]", "");
                else if(line.equals("") && !title.equals("")){
                    categories.add(new Category(title, categoryRaw.substring(0, categoryRaw.length()-1)));
                    categoryRaw = "";
                    title = "";
                }
                else categoryRaw+=line+"\n";
            }
            categories.add(new Category(title, categoryRaw.substring(0, categoryRaw.length()-1)));
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }

    }

    public Rolepicker(StringSelectInteractionEvent event) {
        isEphemeral = true;
    }


    @Override
    public boolean isEphemeral() {
        return isEphemeral;
    }

    @Override
    public boolean isSuccess() {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public List<ActionRow> getActionRows() {
        return actionRows;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Welcome to "+Bot.getConfig().get("guild:name")+"!");
        builder.setImage(Bot.getConfig().get("guild:image"));
        builder.setDescription("Hi, below is some menus you can scroll through and pick the roles you like :)\nSome of these roles will get occational pings!");
        builder.setFooter(event.getGuild().getName());

        for(Category category : categories){
            actionRows.add(ActionRow.of(category.builder.build()));
        }
        return builder.build();

    }

    @Override
    public boolean requiresAdmin() {
        // TODO Auto-generated method stub
        return true;
    }

    private class Category{
        private Builder builder;

        public Category(String title, String rawText) {
            builder = StringSelectMenu.create(buildSubId(title, null));
            for(String line : rawText.split("\n")){
                String name = line.split("=")[0];
                String id = line.split("=")[1].split(", ")[0];
                String description = line.split("=")[1].split(", ").length > 1 ? line.split("=")[1].split(", ")[1] : "";
                if(line.split("=")[1].split(", ").length == 3){
                    Emoji emoji = Emoji.fromFormatted(line.split("=")[1].split(", ")[2]);
                    builder.addOption(name, id, description, emoji);
                }
                else builder.addOption(name, id, description);
                
            }
            builder.setRequiredRange(1, rawText.split("\n").length);
            builder.setPlaceholder(title);
            builder.setMinValues(0);
        }
    }

    @Override
    public Object SelectMenuExecute(StringSelectInteractionEvent event) {
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
}
