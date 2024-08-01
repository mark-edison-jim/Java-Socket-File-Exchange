import java.util.ArrayList;

public class DM {
    private final String userA;
    private final String userB;
    private final int roomID;
    private boolean userAHasJoined;
    private boolean userBHasJoined;
    private final ArrayList<String> messageLog = new ArrayList<>();


    public DM(String userA, String userB, int roomID) {
        this.userA = userA;
        this.userB = userB;
        this.roomID = roomID;
        this.userAHasJoined = false; 
        this.userBHasJoined = false; 
    }

    public boolean checkIfUserJoined(String user) {
        if (user.equals(userA)) {
            return this.userAHasJoined;
        } else if (user.equals(userB)) {
            return this.userBHasJoined;
        }
        return false; // User is not part of this DM room
    }

    public void setUserJoinedStatus(String user, boolean hasJoined) {
        if (user.equals(userA)) {
            this.userAHasJoined = hasJoined;
        } else if (user.equals(userB)) {
            this.userBHasJoined = hasJoined;
        }
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
        sb.append(user).append(": ").append(message).append(" \n");
        messageLog.add(sb.toString());
    }
    public void addMessage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(" \n");
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