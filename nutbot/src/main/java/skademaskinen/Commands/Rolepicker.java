package skademaskinen.Commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import skademaskinen.Bot;
import skademaskinen.Utils.Shell;

public class Rolepicker implements Command{
    String filepath = "files/rolepicker.conf";
    List<Category> categories = new ArrayList<>();
    List<ActionRow> actionRows = new ArrayList<>();

    public static CommandData configure(){
        return Commands.slash(Rolepicker.class.getSimpleName().toLowerCase(), "Create a rolepicker for the guild");
    }

    public Rolepicker(SlashCommandInteractionEvent event) {
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
        }
        catch(Exception e){
            Shell.exceptionHandler(e);
        }

    }
    @Override
    public boolean isEphemeral() {
        return false;
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
            Builder SelectMenuBuilder = StringSelectMenu.create(buildSubId(category.title, null));
            for(String RoleName : category.roles.keySet()){
                SelectMenuBuilder.addOption(RoleName, category.roles.get(RoleName));
                SelectMenuBuilder.setPlaceholder(category.title);
            }
            actionRows.add(ActionRow.of(SelectMenuBuilder.build()));
        }
        return builder.build();

    }

    @Override
    public boolean requiresAdmin() {
        // TODO Auto-generated method stub
        return false;
    }

    private class Category{
        private String title;
        private Map<String, String> roles = new HashMap<>();

        public Category(String title, String rawText) {
            this.title = title;
            for(String line : rawText.split("\n")){
                String name = line.split("=")[0];
                String id = line.split("=")[1];
                roles.put(name, id);
            }

        }
    }
    
}
