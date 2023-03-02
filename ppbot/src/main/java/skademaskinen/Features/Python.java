package skademaskinen.Features;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.python.util.PythonInterpreter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import skademaskinen.Utils.Shell;

public class Python implements Feature {
    
    public static CommandData configure() {
        SlashCommandData command = Commands.slash(Python.class.getSimpleName().toLowerCase(), "Create a python interpreter");
        return command;
    }

    private boolean deferEdit = false;
    private boolean defer = false;

    public Python(SlashCommandInteractionEvent event) {
        defer = true;
    }

    public Python(ButtonInteractionEvent event) {
        if(getSubId(event).equals("run")) deferEdit = true;
        else defer = false;

    }

    public Python(ModalInteractionEvent event) {
        deferEdit = true;
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
    public boolean isSuccess() {
        return true;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        if(!event.getMember().getId().equals("214752462769356802")) return "Error, wrong user";
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("`         Python Interpreter!         `");
        builder.setDescription(wrap(" ", " "));

        return builder.build();
    }

    @Override
    public Object run(ButtonInteractionEvent event) {
        if(!event.getMember().getId().equals("214752462769356802")) return "Error, wrong user";
        switch(getSubId(event)){
            case "run":
                Message message = event.getMessage();
                MessageEmbed oldEmbed = message.getEmbeds().get(0);
                String code = getCode(oldEmbed.getDescription());
                String result;
                try(PythonInterpreter interpreter = new PythonInterpreter()){
                    StringWriter out = new StringWriter();
                    interpreter.setOut(out);
                    interpreter.exec(code);
                    result = out.toString();
                }
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(oldEmbed.getTitle());
                builder.setDescription(wrap(code, result));
                message.editMessageEmbeds(builder.build()).queue();
                return null;
                
            case "setcode":
                TextInput codeField = TextInput.create("modal", "Set Code", TextInputStyle.PARAGRAPH).build();
                return Modal.create(buildSubId("modal", event.getMessageId()), "Set Code").addActionRow(codeField).build();
            default:
                return "error!";
        }
    }

    @Override
    public Object run(ModalInteractionEvent event) {
        if(!event.getMember().getId().equals("214752462769356802")) return "Error, wrong user";
        Message message = event.getChannel().retrieveMessageById(event.getModalId().split("::")[2]).complete();
        MessageEmbed oldEmbed = message.getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(oldEmbed.getTitle());
        builder.setDescription(wrap(event.getValues().get(0).getAsString(), " "));
        message.editMessageEmbeds(builder.build()).queue();
        return null;
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public List<ActionRow> getActionRows() {
        List<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(Button.secondary(buildSubId("setcode", ""), "Set Code"), Button.primary(buildSubId("run", ""), "Run")));
        return actionRows;
    }

    private String wrap(String code, String output){
        return "**Code:**\n```python\n"+code+"\n```**Output:**\n```\n"+output+"\n```";
    }

    private String getCode(String source){
        return source.replace("**Code:**\n```python\n", "").split("\n```\\*\\*Output:\\*\\*\n```\n")[0];
    }
    
}
