package skademaskinen.Listeners;

import java.lang.reflect.Constructor;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skademaskinen.Bot;
import skademaskinen.Features.Feature;
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
            Class<?> featureClass = Class.forName("skademaskinen.Features."+Utils.capitalize(event.getComponentId().split("::")[0]));
            Constructor<?> constructor = featureClass.getConstructor(StringSelectInteractionEvent.class);
            Feature feature = (Feature) constructor.newInstance(new Object[]{event});            
            
            if(feature.requiresAdmin() && !event.getMember().hasPermission(Permission.ADMINISTRATOR)){
                event.reply("Error, you are not an administrator!").setEphemeral(true).queue();
                return;
            }

            if(feature.shouldDefer()){
                event.deferReply(feature.isEphemeral()).queue();
            }
            Object replyContent;
            try{
                replyContent = feature.execute(event);
                if(replyContent == null) if(feature.shouldDefer()) event.getHook().editOriginal("An unhandled error occured, contact Mast3r_waf1z#0420 for more info").queue();
                else event.reply("An unhandled error occured, contact Mast3r_waf1z#0420 for more info").queue();
            }
            catch(Exception e){
                Shell.exceptionHandler(e);
                if(feature.shouldDefer()){
                    event.getHook().editOriginal(e.getMessage()).queue();
                }
                else{
                    event.reply(e.getMessage()).queue();
                }
                return;
            }
            Bot.replyToEvent(event.getHook(), replyContent, feature.getActionRows());

            
        } catch (Exception e) {
            Shell.exceptionHandler(e);
        }
    }
}
