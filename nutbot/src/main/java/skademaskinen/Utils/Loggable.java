package skademaskinen.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * The purpose of this interface is to build logging functionality into everything that implements it
 */
public interface Loggable {

    /**
     * This method logs a call to a method inside an object, the object can also specify arguments to be logged as well and a success tag.
     * @param successfulness The success tag, this is a boolean symbolizing whether this command succeeded or not
     * @param args The custom arguments passed to the loggging function.
     */
    public default void log(boolean successfulness, String[] args){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("files/log.log"), true))){
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            String message = Utils.timestamp() + " " + this.getClass().getSimpleName()+"::"+stack[2].getMethodName()+"[";
            for(String arg : args){
                message+=arg+", ";
            }
            message+="success: "+successfulness+"]";
            writer.write(message);
            writer.newLine();
            writer.flush();
        }
        catch(Exception e){

        }
    }

}
