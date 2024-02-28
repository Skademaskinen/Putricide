package skademaskinen.Utils;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.FileUpload;
import skademaskinen.Bot;

/**
 * This class represents the Command Line Interface (CLI), or shell where a lot of commands can be run instead of being typed directly into discord. This also can function as a chatting function through the bot
 */
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

    /**
     * This is the constructor for the Shell class, it initializes the terminal reader, this is needed such that we are sure when the object is initialized
     */
    public Shell() {
        reader = LineReaderBuilder.builder().build();
    }

    /**
     * This method generates a prompt based on data around this class
     * @return a String containing the prompt that is shown in the command line
     */
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


    /**
     * This is the supertype method run of the interface Runnable, it handles the terminal input and executes the commands given to the command line.
     */
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
                        println("Invalid arguments");
                    }
                    break;
                case "cd":
                    if(args.length == 2) cd(args[1]);
                    else println("Invalid arguments");
                    break;
                case "ls":
                    if(args.length == 2) ls(args[1]);
                    else ls(guild != null ? guild.getName() : ".");
                    break;
                default:
                    println("Error, invalid command by ["+args[0]+"]");
                    break;

            }
        }
        System.exit(0);
    }

    /**
     * This method handles the channel command, it can list all channels for a server or choose a specific channel for a server.
     * @param args The arguments passed to the command line, it specifies subcommands and values
     */
    private void channel(String[] args){
        if(guild == null){
            print("invalid guild");
            return;
        }
        switch(args[1]){
            case "list":
                for(TextChannel channel : guild.getTextChannels()){
                    println(channel.getId()+": "+channel.getName());
                }
                break;
            case "set":
                if(args.length == 3){
                    channel = guild.getTextChannelById(args[2]);
                }
                else{
                    println("Too few arguments");
                }
                break;
        }
    }

    /**
     * This method handles the guild command, it can list all guilds or choose a specific guild.
     * @param args The arguments passed to the command line, it specifies subcommands and values
     */
    private void guild(String[] args){
        switch(args[1]){
            case "list":
                for(Guild guild : Bot.getJda().getGuilds()){
                    println(guild.getId()+": "+guild.getName());
                }
                break;
            case "set":
                if(args.length == 3){
                    channel = null;
                    guild = Bot.getJda().getGuildById(args[2]);
                }
                else{
                    println("Too few arguments");
                }
        }
    }

    private void cd(String path){
        String guildName = guild == null ? path.split("/")[0] : guild.getName();
        String channelName = guild == null ? (path.split("/").length == 2 ? path.split("/")[1] : null) : path.split("/")[path.split("/").length-1];
        if(channelName == null) this.channel = null;
        for(Guild guild : Bot.getJda().getGuilds()){
            if(guild.getName().equals(guildName)){
                this.guild = guild;
                break;
            }
        }
        for(GuildChannel channel : this.guild.getChannels()){
            if(channel.getName().equals(channelName)){
                if(channel.getType().equals(ChannelType.TEXT)){
                    this.channel = (TextChannel)channel;
                }
            }
        }
    }

    private void ls(String path) {
        if(path.equals(".")){
            for(Guild guild : Bot.getJda().getGuilds()){
                println(guild.getName());
            }
        }
        else{
            for(GuildChannel channel : Bot.getJda().getGuildsByName(path, false).get(0).getChannels()){
                if(channel.getType().equals(ChannelType.TEXT)) println(channel.getName());
            }
        }
    }

    /**
     * This method handles the send command, it sends a message to the channel that is selected
     * @param message The message to be sent
     */
    private void send(String message){
        channel.sendMessage(message).queue();
        println("Successfully sent message: "+message);
    }

    /**
     * Prints a string above the prompt
     * @param message The string to be printed
     */
    public void print(String message){
        reader.printAbove(message);
    }

    /**
     * Static method to call the initialized shell object and print the inputted object
     * @param message The message object to be printed
     */
    public static void println(Object message){
        if(message.getClass().equals(String.class))
            Bot.getShell().print((String) message);

        else if(message.getClass().equals(Integer.class) || 
            message.getClass().equals(Long.class) || 
            message.getClass().equals(Double.class) || 
            message.getClass().equals(Float.class))
                Bot.getShell().print(String.valueOf(message));
        else{
            Bot.getShell().print(message.toString());
        }
    }

    /**
     * This is the global exceptionhandler for the entire Bot software, it prints the stack trace above the prompt and sends it as a message to the configured log channel
     * @param e The exception to be logged
     */
    public static void exceptionHandler(Throwable e, Guild guild) {
        String message = e.toString();
        println(yellow(e.toString()));
        for(StackTraceElement element : e.getStackTrace()){
            message+="\n\t"+element.toString();
            println("\t"+red(element.toString()));
        }
        if(message.length() > 2000){
            System.exit(1);
            TextChannel channel = guild.getTextChannelById(ServerConfig.get(guild).getJSONObject("channels").getString("log"));
            channel.sendFiles(FileUpload.fromData(message.getBytes(), e.getClass().getSimpleName()+".txt")).queue();
        }
        else guild.getTextChannelById(ServerConfig.get(guild).getJSONObject("channels").getString("log")).sendMessage("```\n"+message+"```").queue();
    }

    public static void exceptionHandler(Throwable e){
        if(Bot.getJda() != null){
            exceptionHandler(e, Bot.getJda().getGuildById("692410386657574952"));
        }
        else{
            e.printStackTrace();
        }
    }

    /**
     * This channel checks if the shell is currently configured to be in a specific channel
     * @param guild The guild being checked for
     * @param channel The channel being checked
     * @return
     */
    public boolean isInChannel(Guild guild, MessageChannelUnion channel) {
        if(guild.equals(this.guild) && channel.asTextChannel().equals(this.channel)) return true;
        return false;
    }

}
