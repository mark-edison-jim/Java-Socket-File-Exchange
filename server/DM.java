import java.util.ArrayList;

public class DM {
    private String userA;
    private String userB;
    private int roomID;
    private ArrayList<String> messageLog = new ArrayList<>();


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
        StringBuilder sb = new StringBuilder();
        sb.append("From DM class ").append(user).append(": ").append(message).append(" \n");
        messageLog.add(sb.toString());
    }

    public String getMessages() {
        StringBuilder sb = new StringBuilder();
        for (String message : messageLog){
            sb.append(message);
        }

        return sb.toString();
    }
    

    public ArrayList<String> getMessageLog() {
        return messageLog;
    }
}