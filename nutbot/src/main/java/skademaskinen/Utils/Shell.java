package skademaskinen.Utils;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import skademaskinen.Bot;

public class Shell implements Runnable {
    private LineReader reader;
    private TextChannel channel = null;
    private Guild guild = null;

    public Shell() {
        reader = LineReaderBuilder.builder().build();
    }

    public String prompt(){
        String prompt = "["+Bot.getJda().getSelfUser().getName()+" /";
        
        if(guild != null){
            prompt +=guild.getName()+"/";
            if(channel != null){
                prompt += channel.getName()+"/";
            }
        }
        

        return prompt+"] > ";
    }


    @Override
    public void run() {
        for(String line = ""; !line.equals("exit"); line = reader.readLine(prompt())){
            String[] args = line.split(" ");
            switch(args[0]){
                case "":
                    continue;
                case "channel":
                    channel(args);
                    break;
                case "guild":
                    guild(args);
                    break;
                case "send":
                    if(args.length == 2){
                        send(args[1]);
                    }
                    else{
                        print("Invalid arguments");
                    }
                    break;
                default:
                    print("Error, invalid command by ["+args[0]+"]");
                    break;

            }
        }
        System.exit(0);
    }
    private void channel(String[] args){
        if(guild == null){
            print("invalid guild");
            return;
        }
        switch(args[1]){
            case "list":
                for(TextChannel channel : guild.getTextChannels()){
                    print(channel.getId()+": "+channel.getName());
                }
                break;
            case "set":
                if(args.length == 3){
                    channel = guild.getTextChannelById(args[2]);
                }
                else{
                    print("Too few arguments");
                }
                break;
        }
    }

    private void guild(String[] args){
        switch(args[1]){
            case "list":
                for(Guild guild : Bot.getJda().getGuilds()){
                    print(guild.getId()+": "+guild.getName());
                }
                break;
            case "set":
                if(args.length == 3){
                    channel = null;
                    guild = Bot.getJda().getGuildById(args[2]);
                }
                else{
                    print("Too few arguments");
                }
        }
    }

    private void send(String message){
        channel.sendMessage(message).queue();
        print("Successfully sent message: "+message);
    }

    public void print(String message){
        reader.printAbove(message);
    }

    public static void println(Object message){
        if(message.getClass().equals(String.class))
            Bot.getShell().print((String) message);

        else if(message.getClass().equals(Integer.class) || 
            message.getClass().equals(Long.class) || 
            message.getClass().equals(Double.class) || 
            message.getClass().equals(Float.class))
                Bot.getShell().print(String.valueOf(message));
    }

    public static void exceptionHandler(Exception e) {
        e.printStackTrace();
    }
}
