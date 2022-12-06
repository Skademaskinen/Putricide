package skademaskinen.Commands;

import java.util.Map;
import java.util.Map.Entry;

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

public class Configure implements Command {
    private boolean success = false;

    public static CommandData configure() {
        SlashCommandData command = Commands.slash(Configure.class.getSimpleName().toLowerCase(), "Edit the bot configuration");

        return command;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean shouldDefer() {
        return false;
    }

    @Override
    public Object ModalExecute(ModalInteractionEvent event) {
        String config = event.getValue("config").getAsString();
        for(String option : config.split("\n")){
            String key = option.split("=")[0];
            String value = option.split("=").length == 2 ? option.split("=")[1] : "";
            Bot.getConfig().set(key, value);
        }
        success = true;
        return "Successfully updated configuration";
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)){
            return "Error, you are not an administrator!";
        }
        Map<String, String> config = Bot.getConfig().getConfig();
        TextInput.Builder builder = TextInput.create("config", "Edit configuration", TextInputStyle.PARAGRAPH);
        String content = "";
        for(Entry<String, String> entry : config.entrySet()){
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
