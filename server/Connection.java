
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Connection extends Thread {

    private Socket s;
    HashMap<Socket, String> handles = new HashMap<>();
    ArrayList<Socket> chatRoom = new ArrayList<>();
    DMRooms dmRooms = new DMRooms();

    Socket directChat;

    public Connection(Socket s, HashMap<Socket, String> handles, ArrayList<Socket> chatRoom) {
        this.s = s;
        this.handles = handles;
        this.chatRoom = chatRoom;
    }

    @Override
    public void run() {
        try {
            String msg;
            DataInputStream reader = new DataInputStream(s.getInputStream()); //receiving data from client
            DataOutputStream writer = new DataOutputStream(s.getOutputStream()); //sending data to client
            String newString[] = null;
            String dmMessage = null;
            String handle = null;
            String otherUser = null;
            String otherMessage = null;
            Socket handleSocket = null;
            Socket otherUserSocket = null;
            DataOutputStream otherUserStream = null;
            DataOutputStream handleStream = null;

            // This checks whether the string that was sent from
            // the client side is the terminal "END" else we
            // send the string back to the client
            while (!(msg = reader.readUTF()).equals("END")) {
                String clientCommands[] = msg.split(" ", 2);
                switch (clientCommands[0]) {
                    case "/register": //receives register request from client and checks if handle already exists
                        if (handles.containsValue(clientCommands[1])) {
                            writer.writeUTF("False");
                        } else {
                            handles.put(s, clientCommands[1]);
                            writer.writeUTF("True");
                        }
                        break;
                    case "/store": //receives store request from client
                        String fileName = reader.readUTF();

                        if (fileName.length() > 0) { //checks if filename is not empty
                            int fileContentLength = reader.readInt(); //checks if filecontent length is not 0
                            if (fileContentLength > 0) {
                                byte[] fileContentBytes = new byte[fileContentLength];
                                reader.readFully(fileContentBytes, 0, fileContentLength); //reads file content from client

                                File file = new File("./files/" + fileName); //creates new file in server files directory
                                FileOutputStream fos = new FileOutputStream(file); //writes file to server files directory
                                fos.write(fileContentBytes);
                                fos.close();
                            }
                        }
                        break;
                    case "/dir":
                        // Creating a File object for directory
                        File directoryPath = new File("./files");
                        // List of all files and directories
                        String contents[] = directoryPath.list();
                        System.out.println("List of files and directories in the specified directory:");
                        writer.writeInt(contents.length);
                        for (String name : contents) {
                            writer.writeUTF(name);
                        }
                        break;

                    case "/get": //receives get request from client
                        String path = "./files/" + clientCommands[1]; //gets file path in server
                        File file = new File(path);
                        if (file.exists() && !file.isDirectory()) {
                            writer.writeBoolean(true); //sends true if file exists
                            sendFile(s, clientCommands[1], file);
                        } else {
                            writer.writeBoolean(false);
                        }

                        break;
                    case "/joinCR": //receives join chatroom request
                        chatRoom.add(s); //adds client to chatroom
                        for (Socket socket : chatRoom) { //notifies all clients in chatroom that a new client has joined
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("--" + handles.get(s) + " has joined the chat room--");
                            }
                        }
                        break;
                    case "/cr": //receives message from client in chatroom
                        for (Socket socket : chatRoom) { //sends message to all clients in chatroom
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("" + handles.get(s) + ": " + clientCommands[1]);
                            }
                        }
                        System.out.println();
                        break;
                    case "/dcCR":
                        for (Socket socket : chatRoom) {
                            if (!socket.equals(s)) {
                                DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
                                sender.writeUTF("--" + handles.get(s) + " has left the chat room--");
                            }
                        }
                        chatRoom.remove(s);
                        writer.writeUTF("/dc");
                        break;
                    case "/joinDM": // leave dm room
                        newString = clientCommands[1].split("~", 3);
                        handle = newString[0];  // Sender's handle
                        otherUser = newString[1]; // Recipient's handle
                        handleSocket = getKeyByValue(handles, handle);
                        DM curRoom = dmRooms.getOrCreateRoom(handle, otherUser);

                        handleStream = new DataOutputStream(handleSocket.getOutputStream());
                        System.out.println(handle + " : " + curRoom.getMessages());
                        handleStream.writeUTF(curRoom.getMessages());
                        break;
                    case "/dm": //handles dms
                        newString = clientCommands[1].split("~", 3);
                        dmMessage = newString[0]; // The direct message
                        handle = newString[1];  // Sender's handle
                        otherUser = newString[2]; // Recipient's handle
                        handleSocket = getKeyByValue(handles, handle);
                        otherUserSocket = getKeyByValue(handles, otherUser);

                        DM currentRoom = dmRooms.getOrCreateRoom(handle, otherUser);
                        if (!(dmMessage.length() == 0 || dmMessage.contains(" has left the chat--") || dmMessage.contains("/dc"))) {
                            currentRoom.addMessage(handle, dmMessage.trim());
                        }

                        if (handleSocket == null || otherUserSocket == null) {
                            System.out.println("One or both users not found.");
                            break;
                        }

                        // Send the direct message to the other user
                        otherUserStream = new DataOutputStream(otherUserSocket.getOutputStream());
                        otherUserStream.writeUTF(handle + ": " + dmMessage);

                        break;
                    case "/log":
                        newString = clientCommands[1].split("~", 3);
                        handle = newString[0];  // Sender's handle
                        otherUser = newString[1]; // Recipient's handle
                        otherMessage = newString[2];
                        DM currRoom = dmRooms.getOrCreateRoom(handle, otherUser);
                        if (!(otherMessage.length() == 0 || otherMessage.contains(" has left the chat--") || otherMessage.contains("/dc"))) {
                            currRoom.addMessage(otherMessage.trim());                     
                        }

                        break;
                    case "/dcDM": // leave dm room
                        newString = clientCommands[1].split("~", 2);
                        handle = newString[0];  // Sender's handle
                        otherUser = newString[1]; // Recipient's handle
                        otherUserSocket = getKeyByValue(handles, otherUser);
                        otherUserStream = new DataOutputStream(otherUserSocket.getOutputStream());
                        otherUserStream.writeUTF("--" + handle + " has left the chat--");

                        writer.writeUTF("/dc");
                        break;
                    default:
                        System.out.println("Error: Command not found.");
                        break;
                }
            }
            handles.remove(s);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server: Client " + s.getRemoteSocketAddress() + " has disconnected");
        }
    }

    public static <K, V> K getKeyByValue(HashMap<K, V> map, V value) {
        for (HashMap.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null; // Return null if value is not found
    }

    static void sendFile(Socket endpoint, String fileName, File file) {
        try {

            FileInputStream fis = new FileInputStream(file.getAbsolutePath()); //gets file path in server folder
            DataOutputStream dos = new DataOutputStream(endpoint.getOutputStream()); //sends file to client

            byte[] fileContentBytes = new byte[(int) file.length()]; //gets file size and data

            fis.read(fileContentBytes);  //reads file data and stores in byte array

            dos.writeUTF(fileName); //sends file name

            dos.writeInt(fileContentBytes.length); //sends file size
            dos.write(fileContentBytes); //sends file data

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
