import java.util.ArrayList;

public class DMRooms {
    private final ArrayList<DM> DMList; // Use synchronizedList for thread safety
    private int roomID;

    public DMRooms(int initialRoomID) {
        this.DMList = new ArrayList<>();
        this.roomID = initialRoomID;
    }


    // Method to get or create a DM room
    public DM getOrCreateRoom(String userA, String userB) {
        // Check if a room already exists between userA and userB
        for (DM room : DMList) {
            if ((room.getUserA().equals(userA) && room.getUserB().equals(userB)) ||
                (room.getUserA().equals(userB) && room.getUserB().equals(userA))) {
                return room; // Room found
            }
        }
        // If no room is found, create a new one
        DM newRoom = new DM(userA, userB, this.roomID);
        DMList.add(newRoom);
        this.roomID++;
        return newRoom;
    }


}