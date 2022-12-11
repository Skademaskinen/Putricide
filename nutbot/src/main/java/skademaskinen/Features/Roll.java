package skademaskinen.Features;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * This command creates a roll embed for a given amount of entries, it can be used to roll out loot on a raid team
 */
public class Roll implements Feature{
    private boolean success;

    /**
     * The method to configure a given command, this must be implemented as a static method of each command
     * @return All command data to register a command in discord
     */
    public static CommandData configure() {
        SlashCommandData command = Commands.slash(Roll.class.getSimpleName().toLowerCase(), "create a roll between 1 and 100 for each entry");
        for(int i = 0; i < 10; i++){
            command.addOption(OptionType.USER, "user"+i, "add a user");

        }
        return command;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public boolean isEphemeral(){
        return false;
    }

    public Roll(SlashCommandInteractionEvent event) {
    }

    @Override
    public MessageEmbed run(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        ArrayList<String> entries = new ArrayList<>();
        for(OptionMapping option : event.getOptions()){
            entries.add(option.getAsUser().getAsMention());
        }
        Map<String, Integer> results = new HashMap<String, Integer>();
        for(String entry : entries){
            int roll = (int) (Math.random()*100);
            results.put(entry, roll);
            builder.appendDescription("**"+entry+"**: "+roll+"\n");
        }
        int winnerValue = Collections.max(results.values());
        String winner = "";
        for(String key : results.keySet()){
            if(results.get(key).equals(winnerValue)){
                winner = key;
            }
        }
        builder.addField("", "**"+winner+"** has won the roll", false);
        builder.setColor(Color.blue);
        builder.setThumbnail("https://cdn.discordapp.com/attachments/692410386657574955/889818089066221578/dice.png");
        builder.setTitle("Rolls");
        success = true;

        return builder.build();
    }

    @Override
    public boolean requiresAdmin() {
        return false;
    }
    
}
