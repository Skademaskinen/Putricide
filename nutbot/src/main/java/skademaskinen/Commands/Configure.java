package skademaskinen.Commands;

import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import skademaskinen.Bot;
import skademaskinen.Utils.Config.Entry;

/**
 * This command can edit the configuration of the bot from within discord
 */
public class Configure implements Command {
    private boolean success = false;
    private boolean defer = true;

    /**
     * The method to configure a given command, this must be implemented as a static method of each command
     * @return All command data to register a command in discord
     */
    public static CommandData configure() {
        SlashCommandData command = Commands.slash(Configure.class.getSimpleName().toLowerCase(), "Edit the bot configuration");

        return command;
    }

    public Configure(SlashCommandInteractionEvent event) {
        defer = false;
    }

    public Configure(ModalInteractionEvent event) {
        defer = true;
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
    public Object ModalExecute(ModalInteractionEvent event) {
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
    public Object run(SlashCommandInteractionEvent event) {
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
    
    @Override
    public boolean requiresAdmin() {
        return true;
    }
    
}
