package skademaskinen.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public interface Loggable {

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
