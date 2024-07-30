import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DMRooms {
    private ArrayList<DM> DMList = new ArrayList<>();
    private int nextRoomID = 1; // To keep track of the next room ID

    // Method to get or create a DM room
    public DM getOrCreateRoom(String userA, String userB) {
        // Check if a room already exists between userA and userB
        for (DM room : DMList) {
            if ((room.getUserA().equals(userA) && room.getUserB().equals(userB)) ||
                (room.getUserA().equals(userB) && room.getUserB().equals(userA))) {
                return room; // Room found
            }
        }

        // No existing room, create a new one
        DM newRoom = new DM(userA, userB, nextRoomID++);
        DMList.add(newRoom);
        return newRoom;
    }
}