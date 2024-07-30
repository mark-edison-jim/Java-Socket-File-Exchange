import java.util.LinkedHashMap;
import java.util.Map;

public class DM {
    private String userA;
    private String userB;
    private int roomID;
    private LinkedHashMap<String, String> messageLog = new LinkedHashMap<>();

    public DM(String userA, String userB, int roomID) {
        this.userA = userA;
        this.userB = userB;
        this.roomID = roomID;
    }

    public String getUserA() {
        return userA;
    }

    public String getUserB() {
        return userB;
    }

    public int getRoomID() {
        return roomID;
    }

    public void addMessage(String user, String message) {
        messageLog.put(user, message);
    }

    public String getMessages() {
        StringBuilder sb = new StringBuilder();
        messageLog.forEach((user, message) -> sb.append(user).append(": ").append(message).append("\n"));
        return sb.toString();
    }
    

    public LinkedHashMap<String, String> getMessageLog() {
        return messageLog;
    }
}