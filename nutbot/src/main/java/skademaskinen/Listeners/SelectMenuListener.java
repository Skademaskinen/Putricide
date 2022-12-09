package skademaskinen.Listeners;

import java.lang.reflect.Constructor;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skademaskinen.Bot;
import skademaskinen.Commands.Command;
import skademaskinen.Utils.Shell;
import skademaskinen.Utils.Utils;

public class SelectMenuListener extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        Shell.println(Shell.green("Selectmenu event: "));
        Shell.println(Shell.yellow("Timestamp:    ")+Utils.timestamp());
        Shell.println(Shell.yellow("Guild:        ")+event.getGuild().getName());
        Shell.println(Shell.yellow("Member:       ")+event.getUser().getAsTag());
        Shell.println(Shell.yellow("Menu ID:      ")+event.getComponentId());

        try {
            Class<?> CommandClass = Class.forName("skademaskinen.Commands."+Utils.capitalize(event.getComponentId().split("::")[0]));
            Constructor<?> constructor = CommandClass.getConstructor(StringSelectInteractionEvent.class);
            Command command = (Command) constructor.newInstance(new Object[]{event});            
            
            if(command.requiresAdmin() && !event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                event.reply("Error, you are not an administrator!").setEphemeral(true).queue();
                return;
            }

            if(command.shouldDefer()){
                event.deferReply(command.isEphemeral()).queue();
            }
            Object replyContent;
            try{
                replyContent = command.SelectMenuExecute(event);
            }
            catch(Exception e){
                Shell.exceptionHandler(e);
                if(command.shouldDefer()){
                    event.getHook().editOriginal(e.getMessage()).queue();
                }
                else{
                    event.reply(e.getMessage()).queue();
                }
                return;
            }
            Bot.replyToEvent(event.getHook(), replyContent, command.getActionRows());

            
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
    }
}
