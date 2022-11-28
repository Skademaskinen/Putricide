package skademaskinen.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Utils {
    
    public static String timestamp(){
        return "["+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))+"]";
    }
    
    public static String getTime(long duration) {
        String minutes = String.valueOf((duration/1000)/60);
        if(Integer.parseInt(minutes) < 10){
            minutes = "0" + minutes;
        }
        String seconds = String.valueOf((duration/1000)%60);
        if(Integer.parseInt(seconds) < 10){
            seconds = "0"+seconds;
        }
        return minutes + ":" + seconds;
    }

	public Role findRole(Member member, String name) {
        List<Role> roles = member.getRoles();
        return roles.stream()
            .filter(role -> role.getName().equals(name)) // filter by role name
            .findFirst() // take first result
            .orElse(null); // else return null
	}
}
