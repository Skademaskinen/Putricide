package skademaskinen.Features;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import skademaskinen.Bot;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Config.Entry;

/**
 * This command can edit the configuration of the bot from within discord
 */
public class Configure implements Feature {
    private boolean success = false;
    private boolean defer = true;

    /**
     * The method to configure a given command, this must be implemented as a static method of each command
     * @return All command data to register a command in discord
     */
    public static CommandData configure() {
        SlashCommandData command = Commands.slash(Configure.class.getSimpleName().toLowerCase(), "Edit the bot configuration");

        SubcommandData show = new SubcommandData("edit", "Edit the bot configuration in a modal");
        SubcommandData edit = new SubcommandData("set", "set a single configuration entry")
            .addOption(OptionType.STRING, "key", "Key of the option", true, true)
            .addOption(OptionType.STRING, "value", "Value of the option", true);
        SubcommandData get = new SubcommandData("get", "Get the configuration file");
        
        command.addSubcommands(show, edit, get);
        return command;
    }

    public Configure(SlashCommandInteractionEvent event) {
        switch(event.getSubcommandName()){
            case "edit":
                defer = false;
                break;
            default:
                defer = true;
                break;
        }
    }

    public Configure(ModalInteractionEvent event) {
        defer = true;
    }

    public Configure(CommandAutoCompleteInteractionEvent event) {
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean shouldDefer() {
        return defer;
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
    public boolean requiresAdmin() {
        return true;
    }

    @Override
    public Object run(ButtonInteractionEvent event) {
        return null;
    }

    @Override
    public Object run(ModalInteractionEvent event) {
        String config = event.getValue("config").getAsString();
        for(String option : config.split("\n")){
            String key = option.split("=")[0];
            String value = option.split("=").length == 2 ? option.split("=")[1] : "";
            Bot.getConfig().set(key, value);
        }
        Bot.getConfig().write();
        success = true;
        return "Successfully updated configuration";
    }

    @Override
    public Object run(StringSelectInteractionEvent event) {
        return null;
    }

    @Override
    public List<Choice> run(CommandAutoCompleteInteractionEvent event) {
        List<String> choices = new ArrayList<>();
        List<Entry> config = Bot.getConfig().getConfig();
        for(Entry entry : config){
            choices.add(entry.getKey());
        }
        List<Choice> result = Stream.of(choices.toArray(new String[0]))
            .filter(choice -> choice.toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
            .map(choice -> new Choice(choice, choice))
            .limit(25)
            .collect(Collectors.toList());
        return result;
    }

    public Object edit(SlashCommandInteractionEvent event){
        List<Entry> config = Bot.getConfig().getConfig();
        TextInput.Builder builder = TextInput.create("config", "Edit configuration", TextInputStyle.PARAGRAPH);
        String content = "";
        for(Entry entry : config){
            content+= entry.getKey()+"="+entry.getValue()+"\n";
        }
        builder.setValue(content);
        String id = buildSubId("modal", null);
        Modal modal = Modal.create(id, "Configure the configuration file").addActionRow(builder.build()).build();
    
        success = true;
        return modal;
    }

    public Object set(SlashCommandInteractionEvent event){
        Bot.getConfig().set(event.getOption("key").getAsString(), event.getOption("value").getAsString());
        Bot.getConfig().write();
        success = true;
        return "Successfully set option '"+event.getOption("key").getAsString()+"' to '"+event.getOption("value").getAsString()+"'";
    }

    public Object get(SlashCommandInteractionEvent event){
        success = true;
        return FileUpload.fromData(new File("files/config.conf"));
    }
    
}
