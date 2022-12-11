package skademaskinen.Features;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import skademaskinen.Utils.Loggable;

/**
 * This interface is used for each Feature, they will implement this such that many of the methods doesn't have to be written again and the execution is consistent
 */
public interface Feature extends Loggable {

    /**
     * This method returns whether the given Feature is successful or not
     * @return successfulness of the Feature
     */
    public boolean isSuccess();

    /**
     * This method returns whether the bot should make the message ephemeral or not, the default is true to reduce spam
     * @return A boolean representing whether the message should be ephemeral
     */
    default public boolean isEphemeral(){
        return true;
    }

    /**
     * this method returns whether the bot should reply immediately or if it should wait until the execution of the command finishes, this is useful if you wish to acknowledge the command with a modal
     * @return a boolean showing whether the interaction should be replied to immediately
     */
    default public boolean shouldDefer(){
        return true;
    }

    /**
     * This method returns the actionrows of this software, by default it is null such that if there is no actionrows, it will detect that.
     * @return a list of actionrows, or null if there is no actionrows
     */
    default public List<ActionRow> getActionRows(){
        return null;
    }

    /**
     * This method runs the slash command
     * @param event The slash command event, this contains a lot of information about the event
     * @return An object which is a Modal, a String, or a MessageEmbed.
     */
    public Object run(SlashCommandInteractionEvent event);

    /**
     * This method executes the run method, and logs the method
     * @param event The slash command event, this contains a lot of information about the event
     * @return An object which is a Modal, a String, or a MessageEmbed.
     */
    public default Object execute(SlashCommandInteractionEvent event){
        Object response = run(event);
        log(isSuccess(),  new String[]{
            "User: "+event.getUser().getAsTag(),
            "Guild: "+event.getGuild().getName(),
        });
        return response;
    }

    /**
     * This is a method that describes whether it is only administrators that can execute this command, or if all users can do it.
     * @return a boolean describing whether normal users can execute the command
     */
    public boolean requiresAdmin();

    /**
     * This method executes the modal handling function for a given event
     * @param event The modal interaction event, this object contains a lot of data about the event
     * @return An object which is a Modal, a String, or a MessageEmbed.
     */
    default public Object ModalExecute(ModalInteractionEvent event){
        return null;
    }

    /**
     * This method executes the button handling function for a given event
     * @param event The button interaction event, this object contains a lot of data about the event
     * @return An object which is a button, a String, or a MessageEmbed.
     */
    default public Object ButtonExecute(ButtonInteractionEvent event){
        return null;
    }

    default public Object SelectMenuExecute(StringSelectInteractionEvent event){
        return null;
    }

    /**
     * This method builds an id that can be handled in the interaction handlers, that bases the id on the method that generated it, it also supports embedding data into the id
     * @param id The custom id to specify the specific button, this can be anything if there is only one button
     * @param data The data to be embedded into the id
     * @return A unique id that represents the given button, an example id is: "Raid::approve::skademanden,melee damage,argent dawn"
     */
    default public String buildSubId(String id, String data){
        if(data != null) return this.getClass().getSimpleName()+"::"+id+"::"+data;
        else return this.getClass().getSimpleName()+"::"+id;
    }

    default public String getSubId(ButtonInteractionEvent event){
        return event.getComponentId().split("::")[1];
    }
    default public String getSubId(ModalInteractionEvent event){
        return event.getModalId().split("::")[1];
    }

    /**
     * This method executes the auto complete handling function for a given event
     * @param event The auto complete interaction event, this object contains a lot of data about the event
     * @return A list of choices that can be replied to the event with.
     */
    default public List<Choice> AutoComplete(CommandAutoCompleteInteractionEvent event){
        return null;
    }

    /**
     * The method to configure a given feature, this must be implemented as a static method of each command
     * @return All command data to register a feature in discord
     */
    public static CommandData configure(){
        return null;
    }

}
