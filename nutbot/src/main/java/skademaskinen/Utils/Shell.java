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
	private static final String RESET = "\u001B[0m";
	private static final String BLACK = "\u001B[30;1m";
	private static final String RED = "\u001B[31m";
	private static final String GREEN = "\u001B[32m";
	private static final String YELLOW = "\u001B[33m";
	private static final String BLUE = "\u001B[34m";
	private static final String PURPLE = "\u001B[35m";
	private static final String CYAN = "\u001B[36m";
	private static final String WHITE = "\u001B[37m";
    public static String black(String string){return BLACK+string+RESET;}
    public static String red(String string){return RED+string+RESET;}
    public static String green(String string){return GREEN+string+RESET;}
    public static String yellow(String string){return YELLOW+string+RESET;}
    public static String blue(String string){return BLUE+string+RESET;}
    public static String purple(String string){return PURPLE+string+RESET;}
    public static String cyan(String string){return CYAN+string+RESET;}
    public static String white(String string){return WHITE+string+RESET;}

    public Shell() {
        reader = LineReaderBuilder.builder().build();
    }

    public String prompt(){
        String prompt = "["+green(Bot.getJda().getSelfUser().getName())+" /";
        
        if(guild != null){
            prompt +=yellow(guild.getName())+"/";
            if(channel != null){
                prompt += yellow(channel.getName())+"/";
            }
        }
        

        return prompt+"] "+CYAN+">"+RESET+" ";
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
        String message = "```\n"+e.toString()+": "+e.getMessage();
        println(yellow(e.toString()+": "+e.getMessage()));
        for(StackTraceElement element : e.getStackTrace()){
            message+="\n\t"+element.toString();
            println("\t"+red(element.toString()));
        }
        Bot.getJda().getGuildById("692410386657574952").getTextChannelById("958070914245886113").sendMessage(message+"```").queue();
    }

}
