package skademaskinen.Commands;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import skademaskinen.Utils.Loggable;

public interface Command extends Loggable {
    public boolean isSuccess();
    default public boolean isEphemeral(){
        return true;
    }
    default public boolean shouldDefer(){
        return true;
    }
    public Object run(SlashCommandInteractionEvent event);
    public default Object execute(SlashCommandInteractionEvent event){
        Object response = run(event);
        log(isSuccess(),  new String[]{
            "User: "+event.getUser().getAsTag(),
            "Guild: "+event.getGuild().getName(),
        });
        return response;
    }

    default public Object ModalExecute(ModalInteractionEvent event){
        return null;
    }

    default public Object ButtonExecute(){
        return null;
    }

    public static String getName(){
        return null;
    }

    default public String buildSubId(String id, String data){
        if(data != null) return this.getClass().getSimpleName()+"::"+id+"::"+data;
        else return this.getClass().getSimpleName()+"::"+id;
    }
}
