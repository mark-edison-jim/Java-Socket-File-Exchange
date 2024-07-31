import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*; // For Scanner

public class Client {

    public static void main(String[] args) {

        Scanner outerSc = new Scanner(System.in);
        String msg = "";

        System.out.println("Type /? to see all commands.");
        do {
            //Starts to read input from client
            System.out.print("> ");
            msg = outerSc.nextLine();

            String command[] = msg.split(" ");
            switch (command[0]) {
                case "/?":
                    printHelpCommands();
                    break;
                case "/join":
                //join command need exactly 3 inputs
                    if (command.length != 3) {
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                    } else {
                        try {
                            //gets host and port from command input
                            String host = command[1];
                            int port = Integer.parseInt(command[2]);
                            clientJoinedFunc(host, port, outerSc);
                        } catch (NumberFormatException e) {
                            System.out.println(
                                    "Error: Connection to the Server has failed! Please check IP Address and Port Number.");
                        }
                    }
                    break;
                case "/leave":
                    System.out.println("Error: Disconnection failed. Please connect to the server first.");
                    break;
                case "/register":
                    System.out.println("Error: Register failed. Please connect to the server first.");
                    break;
                case "/store":
                    System.out.println("Error: Store failed. Please connect to the server first.");
                    break;
                case "/dir":
                    System.out.println("Error: Dir failed. Please connect to the server first.");
                    break;
                case "/get":
                    System.out.println("Error: Get failed. Please connect to the server first.");
                    break;
                default:
                    if (!(msg.toUpperCase().equals("END"))) {
                        System.out.println("Error: Command not found.");
                    }
                    break;
            }
        } while (!(msg.toUpperCase().equals("END")));

        outerSc.close();
    }

    static void clientJoinedFunc(String host, int port, Scanner sc) {
        String msg;
        String handle = "";
        try {
            Socket endpoint = new Socket(host, port);

            System.out.println("Client: Has connected to server " + host + ":" + port);

            DataInputStream reader = new DataInputStream(endpoint.getInputStream()); //receives data from Server
            DataOutputStream writer = new DataOutputStream(endpoint.getOutputStream()); //sends data to Server
            System.out.print("> ");
            // Starts receiving input from client after joining Server and stops upon receiving leave command
            while (!(msg = sc.nextLine()).equals("/leave")) {
                String command[] = msg.split(" ", 2);
                if (command[0].equals("/?")) {
                    printHelpCommands();
                } else if (command[0].equals("/register") && handle.equals("")) { //checks if client is registered before allowing other commands
                    if (command.length != 2)
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                    else {
                        //sends register request to server
                        writer.writeUTF("/register " + command[1]);
                        String result = reader.readUTF();
                        if (result.equals("False")) {
                            System.out.println("Error: Registration failed. Handle or alias already exists.");
                        } else if (result.equals("True")) {
                            handle = command[1];
                            System.out.println("Server: Welcome " + command[1] + "!");
                        }
                    }
                } else if (command[0].equals("/register")) { //user already registered
                    System.out.println("Error: Registration failed. User already registered as " + handle + ".");
                } else if (handle.equals("")) { //dismisses other commands if user is not registered
                    if (command[0].equals("/store")
                    || command[0].equals("/get")
                    || command[0].equals("/dir")
                    || command[0].equals("/chat"))
                        System.out.println("Error: User must register first.");
                    else if (command[0].equals("/join"))
                        System.out.println("Error: User already joined.");
                    else
                        System.out.println("Error: Command not found.");
                } else {//user is registered and can use other commands
                    switch (command[0]) {
                        case "/store":
                            storeFile(command, endpoint, writer, handle);
                            break;
                        case "/dir":
                            writer.writeUTF("/dir");
                            System.out.println("Server Directory:");
                            int numFiles = reader.readInt();
                            for (int i = 0; i < numFiles; i++) {
                                System.out.println(reader.readUTF());
                            }
                            break;
                        case "/get":
                            getFile(msg, command, endpoint, writer, reader, handle);
                            break;
                        case "/chat":
                            chatFunction(sc, handle, writer, reader);
                            break;
                        default:
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                            break;
                    }
                }
                System.out.print("> ");
            }
            // Send the terminal String to the Server
            writer.writeUTF("END");

            System.out.println("Client: has terminated connection");

            endpoint.close();
        } catch (Exception e) {
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
        }
    }

    static void chatFunction(Scanner sc, String handle, DataOutputStream writer, DataInputStream reader) {
        System.out.println("Type /chathelp for help, /chatleave to leave chats.");
        String msg;
        do {
            // Starts receiving input from client
            System.out.print("> ");
            msg = sc.nextLine();

            String command[] = msg.split(" ",2);
            switch (command[0]) {
                case "/chathelp":
                    printChatCommands();
                    break;
                case "/chatroom":
                    System.out.println("Type /dc to leave the chatroom.");
                    joinChatRoom(sc, writer, reader, handle);
                    break;
                case "/whisper":
                    if(command.length == 2)
                    {
                        System.out.println("Type /dc to leave the chatroom.");
                        String otherUser = command[1];
                            joinDMRoom(sc, writer, reader, handle, otherUser);
                    }
                    break;
                default:
                    if (!(msg.equals("/chatleave"))) {
                        System.out.println("Error: Invalid Command");
                    }
                    break;
            }
        } while (!(msg.equals("/chatleave")));

        System.out.println("Type /? to see all commands.");
    }

    static void joinDMRoom(Scanner sc, DataOutputStream writer, DataInputStream reader, String handle, String otherUser) {
        try {
           writer.writeUTF("/joinDM " + handle + "~" + otherUser);
            System.out.println("Chatting with : " + otherUser);
            new DMThread(reader, writer, handle, otherUser); //starts chatroom thread for client to keep waiting for messages from server
            String msg;
            while (!(msg = sc.nextLine()).equals("/dc")) {
                writer.writeUTF("/dm " + msg + "~" + handle + "~" + otherUser); //sends message to server
                System.out.print(handle + ": ");
            }
            writer.writeUTF("/dcDM " + handle + "~" + otherUser);
    
            System.out.println("Type /chathelp for help, /chatleave to leave chats.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void joinChatRoom(Scanner sc, DataOutputStream writer, DataInputStream reader, String handle) {
        try {
            writer.writeUTF("/joinCR"); //sends join chatroom request
            new ChatroomThread(reader, handle); //starts chatroom thread for client to keep waiting for messages from server
            String msg;
            System.out.print(handle + ": ");
            while (!(msg = sc.nextLine()).equals("/dc")) {
                writer.writeUTF("/cr " + msg); //sends message to server
                System.out.print(handle + ": ");
            }
            writer.writeUTF("/dcCR");
            
            System.out.println("Type /chathelp for help, /chatleave to leave chats.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void getFile(String msg, String[] command, Socket endpoint, DataOutputStream writer, DataInputStream reader,
            String handle) {
        try {
            if (command.length != 2)
                System.out.println("Error: Command parameters do not match or is not allowed.");
            else {
                writer.writeUTF(msg);
                if (reader.readBoolean()) {
                    String fileName = reader.readUTF(); //read file name from server

                    if (fileName.length() > 0) {
                        int fileContentLength = reader.readInt(); //read file content length from server
                        if (fileContentLength > 0) {
                            byte[] fileContentBytes = new byte[fileContentLength];
                            reader.readFully(fileContentBytes, 0, fileContentLength); //read file content from server

                            File file = new File("./receivedFiles/" + fileName); //create file in receivedFiles folder in client folder
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(fileContentBytes); //write file content to file
                            fos.close();
                            System.out.println("File received from Server: " + fileName);
                        }
                    }
                } else {
                    System.out.println("Error: File not found in the server.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
        }
    }

    static void storeFile(String[] command, Socket endpoint, DataOutputStream writer, String handle) {
        try {
            //store command needs exactly 2 inputs
            if (command.length != 2)
                System.out.println("Error: Command parameters do not match or is not allowed.");
            else {
                //gets file path in client folder
                String path = "./files/" + command[1];
                File file = new File(path);
                if (file.exists() && !file.isDirectory()) {
                    writer.writeUTF("/store"); //sends store request to server
                    sendFile(endpoint, command[1], file);
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    System.out.println(handle + "<" + dtf.format(now) + ">: Uploaded " + command[1]); //log message that client has stored file to server
                } else {
                    System.out.println("Error: File not found.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
        }
    }

    static void sendFile(Socket endpoint, String fileName, File file) {
        try {
            FileInputStream fis = new FileInputStream(file.getAbsolutePath()); //gets file path in client folder
            DataOutputStream dos = new DataOutputStream(endpoint.getOutputStream()); //sends file to server

            byte[] fileContentBytes = new byte[(int) file.length()]; //gets file size and data

            fis.read(fileContentBytes); //reads file data and stores in byte array

            dos.writeUTF(fileName); //sends file name

            dos.writeInt(fileContentBytes.length); //sends file size
            dos.write(fileContentBytes); //sends file data

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printChatCommands() {
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Description                                  | InputSyntax                         | Sample Input Script     |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Join the chatroom                            | /chatroom                           | /chatroom               |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Chat with another user                       | /whisper <username>                 | /whisper User1          |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
    }

    static void printHelpCommands() {
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Description                                  | InputSyntax                         | Sample Input Script     |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Connect to the server application            | /join <server_ip_add> <port>        | /join 192.168.1.1 12345 |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Disconnect to the server application         | /leave                              | /leave                  |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Register a unique handle or alias            | /register <handle>                  | /register User1         |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Send file to server                          | /store <filename>                   | /store Hello.txt        |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Request directory file list from a server    | /dir                                | /dir                    |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Fetch a file from a server                   | /get <filename>                     | /get Hello.txt          |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Join a chatroom or chat with another user    | /chat                               | /chat                   |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
        System.out.println(
                "| Request command help to output all Input     | /?                                  | /?                      |");
        System.out.println(
                "| Syntax commands for references               |                                     |                         |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
    }

}