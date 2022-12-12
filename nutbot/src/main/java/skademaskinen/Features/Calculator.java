package skademaskinen.Features;

import java.util.ArrayList;
import java.util.List;

import org.mariuszgromada.math.mxparser.Expression;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput.Builder;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class Calculator implements Feature {
    private boolean success;
    private List<ActionRow> actionRows = new ArrayList<>();
    private boolean ephemeral;
    private boolean deferEdit;
    private boolean defer;

    public static CommandData configure(){
        return Commands.slash(Calculator.class.getSimpleName().toLowerCase(), "create a calculator message");
    }

    public Calculator(SlashCommandInteractionEvent event) {
        ephemeral = false;
        deferEdit = false;
        defer = true;
    }

    public Calculator(ButtonInteractionEvent event) {
        if(!getSubId(event).equals("expr")){
            ephemeral = true;
            deferEdit = true;
            defer = false;
        }
        else{
            deferEdit = false;
            defer = false;
            ephemeral = false;
        }
    }

    public Calculator(ModalInteractionEvent event) {
        ephemeral = true;
        deferEdit = true;
        defer = false;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean isEphemeral() {
        return ephemeral;
    }

    @Override
    public boolean shouldDeferEdit(){
        return deferEdit;
    }

    @Override
    public boolean shouldDefer() {
        return defer;
    }

    @Override
    public Object run(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("`         Calculator!         `");
        builder.setDescription("```python\n \n```");

        actionRows.add(ActionRow.of(
            Button.danger(buildSubId("AC", null), "AC"),
            Button.secondary(buildSubId("<<=", null), "<<="),
            Button.secondary(buildSubId("%", null), "%"),
            Button.secondary(buildSubId("/", null), "/") 
        ));
        actionRows.add(ActionRow.of(
            Button.primary(buildSubId("7", null), "7"),
            Button.primary(buildSubId("8", null), "8"),
            Button.primary(buildSubId("9", null), "9"),
            Button.secondary(buildSubId("*", null), "*")
        ));
        actionRows.add(ActionRow.of(
            Button.primary(buildSubId("4", null), "4"),
            Button.primary(buildSubId("5", null), "5"),
            Button.primary(buildSubId("6", null), "6"),
            Button.secondary(buildSubId("-", null), "-")
        ));
        actionRows.add(ActionRow.of(
            Button.primary(buildSubId("1", null), "1"),
            Button.primary(buildSubId("2", null), "2"),
            Button.primary(buildSubId("3", null), "3"),
            Button.secondary(buildSubId("+", null), "+") 
        ));
        actionRows.add(ActionRow.of(
            Button.danger(buildSubId("expr", null), "expr"),
            Button.primary(buildSubId("0", null), "0"),
            Button.secondary(buildSubId(".", null), "."),
            Button.secondary(buildSubId("=", null), "=")
        ));

        return builder.build();
    }

    @Override
    public Object run(ButtonInteractionEvent event) {
        switch(getSubId(event)){
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
            case "%":
            case "/":
            case "*":
            case "-":
            case "+":
            case ".":
                EmbedBuilder builder = new EmbedBuilder();
                MessageEmbed oldEmbed = event.getMessage().getEmbeds().get(0);
                String val1 = oldEmbed.getDescription().split("\n")[1].strip();
                String val2 = event.getComponentId().split("::")[1];

                builder.setTitle(oldEmbed.getTitle());
                builder.setDescription("```python\n");
                builder.appendDescription(" "+val1+val2+"\n```");

                event.getMessage().editMessageEmbeds(builder.build()).queue();
                return null;
            case "AC":
                EmbedBuilder builder1 = new EmbedBuilder();
                MessageEmbed oldEmbed1 = event.getMessage().getEmbeds().get(0);
                builder1.setTitle(oldEmbed1.getTitle());
                builder1.setDescription("```python\n \n```");
                event.getMessage().editMessageEmbeds(builder1.build()).queue();
                return null;
            case "<<=":
                EmbedBuilder builder2 = new EmbedBuilder();
                MessageEmbed oldEmbed2 = event.getMessage().getEmbeds().get(0);
                String val = oldEmbed2.getDescription().split("\n")[1].strip();
                builder2.setTitle(oldEmbed2.getTitle());
                if(val.length() > 0) builder2.setDescription("```python\n "+val.substring(0, val.length()-1)+"\n```");
                else builder2.setDescription("```python\n \n```");
                event.getMessage().editMessageEmbeds(builder2.build()).queue();
                return null;
            case "=":
                EmbedBuilder builder3 = new EmbedBuilder();
                MessageEmbed oldEmbed3 = event.getMessage().getEmbeds().get(0);
                builder3.setTitle(oldEmbed3.getTitle());
                Expression expression = new Expression(oldEmbed3.getDescription().split("\n")[1].strip().replace("%", "#"));
                double result = expression.calculate();
                if((result % 1) == 0) builder3.setDescription("```python\n "+(int)result+"\n```");
                else builder3.setDescription("```python\n "+result+"\n```");
                event.getMessage().editMessageEmbeds(builder3.build()).queue();
                return null;
            case "expr":
                MessageEmbed oldEmbed4 = event.getMessage().getEmbeds().get(0);
                String val3 = oldEmbed4.getDescription().split("\n")[1].strip();
                Builder textinputBuilder = TextInput.create("expression", "Write expression", TextInputStyle.SHORT);
                if(!val3.equals("")) textinputBuilder.setValue(val3);
                Modal modal = Modal.create(buildSubId("expr", null), "Write custom expression")
                    .addActionRow(textinputBuilder.build()).build();
                return modal;
            default:
                return null;

        }
    }

    @Override
    public Object run(ModalInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        MessageEmbed oldEmbed = event.getMessage().getEmbeds().get(0);

        builder.setTitle(oldEmbed.getTitle());
        builder.setDescription("```python\n");
        builder.appendDescription(" "+event.getValues().get(0).getAsString()+"\n```");

        event.getMessage().editMessageEmbeds(builder.build()).queue();
        return null;

    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }

    @Override
    public List<ActionRow> getActionRows() {
        return actionRows;
    }
    
}
