import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]); // Can be changed 
        HashMap<Socket, String> handles = new HashMap<>();
        ArrayList<Socket> chatRoom = new ArrayList<>();
        ArrayList<DM> DMList = new ArrayList<>();
        try {
            ServerSocket ss = new ServerSocket(port);

            System.out.println("Sever: Listening to port " + port);

            while (true) {
                // Waits for a client to connect
                Socket endpoint = ss.accept();

                System.out.println("Server: Client at " + endpoint.getRemoteSocketAddress() + " has connected");

                // Make the Thread Object
                Connection connect = new Connection(endpoint, handles, chatRoom, DMList);
                // Start the thread
                connect.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}