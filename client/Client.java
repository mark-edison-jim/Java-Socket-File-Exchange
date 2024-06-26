import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*; // For Scanner

public class Client {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String msg;
        boolean leave = false;
        do {
            System.out.print("> ");
            msg = sc.nextLine();
            String command[] = msg.split(" ");
            switch (command[0]) {
                case "/?":
                    printHelpCommands();
                    break;
                case "/join":
                    if (command.length != 3) {
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                    } else {
                        String host = command[1]; // can be changed
                        int port = Integer.parseInt(command[2]);
                        leave = clientJoinedFunc(host, port);
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
                    System.out.println("Error: Command not found.");
                    break;
            }
        } while (!leave && !(msg.toUpperCase().equals("END")));
        sc.close();
    }

    static boolean clientJoinedFunc(String host, int port) {
        Scanner sc = new Scanner(System.in);
        String msg;
        String handle = "";
        try {
            Socket endpoint = new Socket(host, port);

            System.out.println("Client: Has connected to server " + host + ":" + port);

            DataInputStream reader = new DataInputStream(endpoint.getInputStream());
            DataOutputStream writer = new DataOutputStream(endpoint.getOutputStream());

            System.out.print("> ");
            // Let's try inputting a string in the console
            while (!(msg = sc.nextLine()).equals("/leave")) {
                String command[] = msg.split(" ", 2);
                switch (command[0]) {
                    case "/register":
                        if (command.length != 2)
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        else {
                            writer.writeUTF("/register " + command[1]);
                            String result = reader.readUTF();
                            if (result.equals("False")) {
                                System.out.println("Error: Registration failed. Handle or alias already exists.");
                            } else if (result.equals("True")) {
                                handle = command[1];
                                System.out.println("Server: Welcome " + command[1] + "!");
                            }
                        }
                        break;
                    case "/store":
                        if (command.length != 2)
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        else {
                            String path = "./files/" + command[1];
                            File file = new File(path);
                            if (file.exists() && !file.isDirectory()) {
                                writer.writeUTF("/store");
                                sendFile(endpoint, command[1], file);
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                                LocalDateTime now = LocalDateTime.now();
                                System.out.println(handle + "<" + dtf.format(now) + ">: Uploaded " + command[1]);
                            } else {
                                System.out.println("Error: File not found.");
                            }
                        }
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
                        if (command.length != 2)
                            System.out.println("Error: Command parameters do not match or is not allowed.");
                        else {
                            writer.writeUTF(msg);
                            if (reader.readBoolean()) {
                                String fileName = reader.readUTF();

                                if (fileName.length() > 0) {
                                    int fileContentLength = reader.readInt();
                                    if (fileContentLength > 0) {
                                        byte[] fileContentBytes = new byte[fileContentLength];
                                        reader.readFully(fileContentBytes, 0, fileContentLength);

                                        File file = new File("./files/" + fileName);
                                        FileOutputStream fos = new FileOutputStream(file);
                                        fos.write(fileContentBytes);
                                        fos.close();
                                        System.out.println("File received from Server: " + fileName);
                                    }
                                }
                            }else{
                                System.out.println("Error: File not found in the server.");
                            }
                        }
                        break;
                    default:
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                        break;
                }
                // The message will be send to the server
                // The Server will append "Server: " so that
                // we know that the message really was accepted
                // by the server
                System.out.print("> ");
            }
            // Send the terminal String to the Server
            writer.writeUTF("END");

            System.out.println("Client: has terminated connection");

            endpoint.close();
        } catch (Exception e) {
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
            return false;
        }finally {
            sc.close();
        }
        return true;
    }

    static void sendFile(Socket endpoint, String fileName, File file) {
        try {

            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            DataOutputStream dos = new DataOutputStream(endpoint.getOutputStream());

            byte[] fileContentBytes = new byte[(int) file.length()];

            fis.read(fileContentBytes);

            dos.writeUTF(fileName);

            dos.writeInt(fileContentBytes.length);
            dos.write(fileContentBytes);

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                "| Request command help to output all Input     | /?                                  | /?                      |");
        System.out.println(
                "| Syntax commands for references               |                                     |                         |");
        System.out.println(
                "+----------------------------------------------+-------------------------------------+-------------------------+");
    }

}