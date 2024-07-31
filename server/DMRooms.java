import java.util.ArrayList;

public class DMRooms {
    private ArrayList<DM> DMList = new ArrayList<>(); // Use synchronizedList for thread safety

    // Method to get or create a DM room
    public DM getRoom(String userA, String userB) {
        // Check if a room already exists between userA and userB
        for (DM room : DMList) {
            if ((room.getUserA().equals(userA) && room.getUserB().equals(userB)) ||
                (room.getUserA().equals(userB) && room.getUserB().equals(userA))) {
                return room; // Room found
            }
        }
        return null;
    }

    public DM createRoom(String userA, String userB, int ID){
        DM newRoom = new DM(userA, userB, ID);
        DMList.add(newRoom);
        return newRoom;
    }

}
