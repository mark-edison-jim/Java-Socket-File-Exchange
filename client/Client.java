import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*; // For Scanner
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Client {

    private JFrame frame;
    private JTextField inputField;
    private JTextArea outputArea;
    private JButton submitButton;
    public boolean hasJoined = false;
    public boolean hasRegistered = false;
    public boolean isChatting = false;
    public boolean isPrivateChatting = false;
    public boolean isGroupChatting = false;
    public String handle ="";
    public Socket endpoint;
    public DataInputStream reader ; //receives data from Server
    public DataOutputStream writer; //sends data to Server
    private String otherUser;

    public Client() {
        frame = new JFrame("Simple Frame\n");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        // Create a panel for input and button
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        // Create the input field
        inputField = new JTextField(30);
        panel.add(inputField);

        // Create the submit button
        submitButton = new JButton("Submit");
        panel.add(submitButton);

        // Create the output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add panel to the frame
        frame.add(panel, BorderLayout.NORTH);

        // Add action listener for the submit button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the input text
                String msg = inputField.getText();

                // Process the command
                intro(msg);

                // Clear the input field after processing
                inputField.setText("");
            }
        });
    }
    public void display() {
        // Set the frame visible
        frame.setVisible(true);
    }

    private void intro(String input){
        String msg = input;
            //Starts to read input from client
            

            String command[] = msg.split(" ");
            if(!isChatting)
            {
            switch (command[0]) {
                case "/?":
                    printHelpCommands();
                    break;
                case "/join":
                //join command need exactly 3 inputs
                    if (command.length != 3) {
                        outputArea.append("Error: Command parameters do not match or is not allowed.\n");
                    } else {
                        try {
                            //gets host and port from command input
                            String host = command[1];
                            int port = Integer.parseInt(command[2]);
                            this.endpoint = new Socket(host, port);

                            outputArea.append("You are connected to the server " + host + ":" + port+"\n");
                            outputArea.append("Type /? to see all commands.\n");
                            hasJoined = true;
                            break;
                        } catch (NumberFormatException e) {
                            outputArea.append(
                                    "Error: Connection to the Server has failed! Please check IP Address and Port Number.\n");
                        } catch (IOException ex) {
                        }
                    }
                    break;
                case "/leave":
                    if(!hasRegistered)
                    {
                        outputArea.append("Error: Disconnection failed. Please connect to the server first.\n");
                    }
                    else
                    {
                        try {
                            writer.writeUTF("END");
                            
                            outputArea.append("Client: has terminated connection\n");
                            
                            endpoint.close();
                            hasRegistered=false;
                        } catch (IOException ex) {
                        }
                    }
                        
                    break;
                case "/register":
                if(command.length == 2)
                {
                    if(!hasRegistered)
                    {
                        if(hasJoined)
                        {
                            try {
                                reader = new DataInputStream(endpoint.getInputStream());
                                writer = new DataOutputStream(endpoint.getOutputStream());
                                writer.writeUTF(msg);
                            
                                // Attempt to read response from server
                                String result = reader.readUTF();
                                if (result.equals("False")) {
                                    outputArea.append("Error: Registration failed. Handle or alias already exists.\n");
                                } else if (result.equals("True")) {
                                    handle = command[1];
                                    outputArea.append("Server: Welcome " + command[1] + "!\n");
                                    hasRegistered = true;
                                }
                            } catch (EOFException eofEx) {
                                outputArea.append("Error: Unexpected end of data stream. Check server response.\n");
                                eofEx.printStackTrace();
                            } catch (IOException ex) {
                                outputArea.append("Error: IO Exception occurred.\n");
                                ex.printStackTrace();
                            }                            
                        }
                        else
                            outputArea.append("Error: Register failed. Please connect to the server first.\n");
                    }
                    else
                        outputArea.append("Error: Client has already registered\n");
                    
                }
                else
                {
                    outputArea.append("Error: Command parameters do not match or is not allowed.\n");  
                }
                break;
                case "/store":
                    if(hasJoined && hasRegistered)
                    {
                        try {
                            //store command needs exactly 2 inputs
                                String storeCommand[] = msg.split(" ",2);
                                //gets file path in client folder
                                String path = "./files/" + storeCommand[1];
                                File file = new File(path);
                                if (file.exists() && !file.isDirectory()) {
                                    writer.writeUTF("/store"); //sends store request to server
                                    sendFile(endpoint, storeCommand[1], file);
                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                                    LocalDateTime now = LocalDateTime.now();
                                    outputArea.append(handle + "<" + dtf.format(now) + ">: Uploaded " + storeCommand[1]+"\n"); //log message that client has stored file to server
                                } else {
                                    outputArea.append("Error: File not found.\n");
                                }
                            
                        } catch (Exception e) {
                            outputArea.append("Error: Connection to the Server has failed! Please check IP Address and Port Number.\n");
                        }
                    }
                    else
                        outputArea.append("Error: Store failed. Please connect, register, or both to the server first.\n");
                    break;
                case "/dir":
                    if(hasJoined && hasRegistered)
                        {
                            try {
                                writer.writeUTF("/dir");
                                outputArea.append("Server Directory:\n");
                                int numFiles = reader.readInt();
                                for (int i = 0; i < numFiles; i++) {
                                    outputArea.append(reader.readUTF()+"\n");
                                }
                            } catch (IOException ex) {
                            }
                        }
                        else
                            outputArea.append("Error: Store failed. Please connect, register, or both to the server first.\n");
                        break;
                case "/chat":
                        outputArea.append("Type '/chathelp' to see the list of chat commands.\n");
                        isChatting = true;
                        break;
                case "/get":
                    if(hasJoined && hasRegistered)
                    {
                        try {
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
                                        outputArea.append("File received from Server: " + fileName+"\n");
                                    }
                                }
                            } else {
                                outputArea.append("Error: File not found in the server.\n");
                            }
                        } catch (IOException ex) {
                        }
                    }
                    else
                        outputArea.append("Error: Store failed. Please connect, register, or both to the server first.\n");
                    break;
                default:
                    if (!(msg.toUpperCase().equals("END"))) {
                        outputArea.append("Error: Command not found.\n");
                    }
                    break;
                }
            }
            else{
                if(isPrivateChatting)
                {
                    if(msg == "/dc")
                    {
                        try {
                            writer.writeUTF("/dcDM " + handle + "~" + otherUser);
                        } catch (IOException ex) {
                        }
                    }
                    else
                    {
                        try {
                            writer.writeUTF("/dm " + msg + "~" + handle + "~" + otherUser); //sends message to server
                            outputArea.append(handle + ": ");
                        } catch (IOException ex) {
                        }
                    }
                }
                else if(isGroupChatting)
                {
                    
                    
                    System.out.println("Type /chathelp for help, /chatleave to leave chats.");
                    if(msg == "/dc")
                    {
                        try {
                            writer.writeUTF("/dcCR");
                            isGroupChatting= false;
                        } catch (IOException ex) {
                        }
                    }
                    else
                    {
                        try {
                            writer.writeUTF("/cr " + msg); //sends message to server
                            System.out.print(handle + ": ");
                        } catch (IOException ex) {
                        }
                    }
                }
                else
                {
                    switch (command[0]) {
                        case "/chathelp":
                            printChatCommands();
                            break;
                        case "/chatroom":
                            outputArea.append("Type /dc to leave the chatroom.\n");
                            try {
                                writer.writeUTF("/joinCR"); //sends join chatroom request
                                new ChatroomThread(reader, handle, outputArea); //starts chatroom thread for client to keep waiting for messages from server
                                outputArea.append(handle + ": ");
                                isGroupChatting = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "/whisper":
                            if(command.length == 2)
                            {
                                outputArea.append("Type /dc to leave the chatroom.\n");
                                otherUser = command[1];
                                try {
                                    writer.writeUTF("/joinDM " + handle + "~" + otherUser);
                                        System.out.println("Chatting with : " + otherUser+"\n");
                                        new DMThread(reader, writer, handle, otherUser, outputArea); //starts chatroom thread for client to keep waiting for messages from server
                                        isPrivateChatting = true;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                            }
                            break;
                        case "/chatleave":
                        {
                            isChatting = false;
                            break;
                        }
                        default:
                            if (!(msg.equals("/chatleave"))) {
                                System.out.println("Error: Invalid Command");
                            }
                            break;
                    }
                }
            }
            
        }

    private void printChatCommands() {
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Description                                  | InputSyntax                         | Sample Input Script     |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Join the chatroom                            | /chatroom                           | /chatroom               |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Chat with another user                       | /chat <username>                    | /chat User1             |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
     }
 
     private void printHelpCommands() {
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Description                                  | InputSyntax                         | Sample Input Script     |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Connect to the server application            | /join <server_ip_add> <port>        | /join 192.168.1.1 12345 |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Disconnect to the server application         | /leave                              | /leave                  |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Register a unique handle or alias            | /register <handle>                  | /register User1         |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Send file to server                          | /store <filename>                   | /store Hello.txt        |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Request directory file list from a server    | /dir                                | /dir                    |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Fetch a file from a server                   | /get <filename>                     | /get Hello.txt          |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Join a chatroom or chat with another user    | /chat                               | /chat                   |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                 "| Request command help to output all Input     | /?                                  | /?                      |\n");
        outputArea.append(
                 "| Syntax commands for references               |                                     |                         |\n");
        outputArea.append(
                 "+----------------------------------------------+-------------------------------------+-------------------------+\n");
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
    public static void main(String[] args) {
        // Create and display the SimpleFrame
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client().display();
            }
        });
    }
}
    // static void clientJoinedFunc(String host, int port, Scanner sc) {
    //     String msg;
        
    //     String handle = "";
    //     try {
    //         Socket endpoint = new Socket(host, port);

    //         System.out.println("Client: Has connected to server " + host + ":" + port);

    //         reader = new DataInputStream(endpoint.getInputStream()); //receives data from Server
    //         writer = new DataOutputStream(endpoint.getOutputStream()); //sends data to Server
    //         System.out.print("> ");
    //         // Starts receiving input from client after joining Server and stops upon receiving leave command
    //         while (!(msg = sc.nextLine()).equals("/leave")) {
    //             String command[] = msg.split(" ", 2);
    //             if (command[0].equals("/?")) {
    //                 printHelpCommands();
    //             } else if (command[0].equals("/register") && handle.equals("")) { //checks if client is registered before allowing other commands
    //                 if (command.length != 2)
    //                     System.out.println("Error: Command parameters do not match or is not allowed.");
    //                 else {
    //                     //sends register request to server
    //                     writer.writeUTF("/register " + command[1]);
    //                     String result = reader.readUTF();
    //                     if (result.equals("False")) {
    //                         System.out.println("Error: Registration failed. Handle or alias already exists.");
    //                     } else if (result.equals("True")) {
    //                         handle = command[1];
    //                         System.out.println("Server: Welcome " + command[1] + "!");
    //                     }
    //                 }
    //             } else if (command[0].equals("/register")) { //user already registered
    //                 System.out.println("Error: Registration failed. User already registered as " + handle + ".");
    //             } else if (handle.equals("")) { //dismisses other commands if user is not registered
    //                 if (command[0].equals("/store")
    //                 || command[0].equals("/get")
    //                 || command[0].equals("/dir")
    //                 || command[0].equals("/chat"))
    //                     System.out.println("Error: User must register first.");
    //                 else if (command[0].equals("/join"))
    //                     System.out.println("Error: User already joined.");
    //                 else
    //                     System.out.println("Error: Command not found.");
    //             } else {//user is registered and can use other commands
    //                 switch (command[0]) {
    //                     case "/store":
    //                         storeFile(command, endpoint, writer, handle);
    //                         break;
    //                     case "/dir":
    //                         writer.writeUTF("/dir");
    //                         System.out.println("Server Directory:");
    //                         int numFiles = reader.readInt();
    //                         for (int i = 0; i < numFiles; i++) {
    //                             System.out.println(reader.readUTF());
    //                         }
    //                         break;
    //                     case "/get":
    //                         getFile(msg, command, endpoint, writer, reader, handle);
    //                         break;
    //                     case "/chat":
    //                         chatFunction(sc, handle, writer, reader);
    //                         break;
    //                     default:
    //                         System.out.println("Error: Command parameters do not match or is not allowed.");
    //                         break;
    //                 }
    //             }
    //             System.out.print("> ");
    //         }
    //         // Send the terminal String to the Server
    //         writer.writeUTF("END");

    //         System.out.println("Client: has terminated connection");

    //         endpoint.close();
    //     } catch (Exception e) {
    //         System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
    //     }
    // }

    // static void chatFunction(Scanner sc, String handle, DataOutputStream writer, DataInputStream reader) {
    //     System.out.println("Type /chathelp for help, /chatleave to leave chats.");
    //     String msg;
    //     do {
    //         // Starts receiving input from client
    //         System.out.print("> ");
    //         msg = sc.nextLine();

    //         String command[] = msg.split(" ");
    //         switch (command[0]) {
    //             case "/chathelp":
    //                 printChatCommands();
    //                 break;
    //             case "/chatroom":
    //                 System.out.println("Type /dc to leave the chatroom.");
    //                 joinChatRoom(sc, writer, reader, handle);
    //                 break;
    //             case "/whisper":
    //                 if(command.length == 2)
    //                 {
    //                     System.out.println("Type /dc to leave the chatroom.");
    //                     String otherUser = command[1];
    //                     joinDMRoom(sc, writer, reader, handle, otherUser);
    //                 }
    //                 break;
                    
    //             case "/leave":
    //                 System.out.println("Error: Disconnection failed. Please connect to the server first.");
    //                 break;
    //             case "/register":
    //                 System.out.println("Error: Register failed. Please connect to the server first.");
    //                 break;
    //             case "/store":
    //                 System.out.println("Error: Store failed. Please connect to the server first.");
    //                 break;
    //             case "/dir":
    //                 System.out.println("Error: Dir failed. Please connect to the server first.");
    //                 break;
    //             case "/get":
    //                 System.out.println("Error: Get failed. Please connect to the server first.");
    //                 break;
    //             default:
    //                 if (!(msg.equals("/chatleave"))) {
    //                     System.out.println("Error: Command not found.");
    //                 }
    //                 break;
    //         }
    //     } while (!(msg.equals("/chatleave")));

    //     System.out.println("Type /? to see all commands.");
    // }

    // // static void directChat(Scanner sc, String handle, DataOutputStream writer, DataInputStream reader) {
    // //     try {
    // //         writer.writeUTF("/joinDirect");
    // //         new ChatroomThread(reader, handle);
    // //         String msg;
    // //         System.out.print(handle + ": ");
    // //         while (!(msg = sc.nextLine()).equals("/dc")) {
    // //             writer.writeUTF("/cr " + msg);
    // //             System.out.print(handle + ": ");
    // //         }
    // //         writer.writeUTF("/dcCR");
    // //         System.out.println("Type /chathelp for help, /chatleave to leave chats.");
    // //     } catch (Exception e) {
    // //         e.printStackTrace();
    // //     }
    // // }

    // static void joinChatRoom(Scanner sc, DataOutputStream writer, DataInputStream reader, String handle) {
    //     try {
    //         writer.writeUTF("/joinCR"); //sends join chatroom request
    //         new ChatroomThread(reader, handle); //starts chatroom thread for client to keep waiting for messages from server
    //         String msg;
    //         System.out.print(handle + ": ");
    //         while (!(msg = sc.nextLine()).equals("/dc")) {
    //             writer.writeUTF("/cr " + msg); //sends message to server
    //             System.out.print(handle + ": ");
    //         }
    //         writer.writeUTF("/dcCR");
            
    //         System.out.println("Type /chathelp for help, /chatleave to leave chats.");
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // static void getFile(String msg, String[] command, Socket endpoint, DataOutputStream writer, DataInputStream reader,
    //         String handle) {
    //     try {
    //         if (command.length != 2)
    //             System.out.println("Error: Command parameters do not match or is not allowed.");
    //         else {
    //             writer.writeUTF(msg);
    //             if (reader.readBoolean()) {
    //                 String fileName = reader.readUTF(); //read file name from server

    //                 if (fileName.length() > 0) {
    //                     int fileContentLength = reader.readInt(); //read file content length from server
    //                     if (fileContentLength > 0) {
    //                         byte[] fileContentBytes = new byte[fileContentLength];
    //                         reader.readFully(fileContentBytes, 0, fileContentLength); //read file content from server

    //                         File file = new File("./receivedFiles/" + fileName); //create file in receivedFiles folder in client folder
    //                         FileOutputStream fos = new FileOutputStream(file);
    //                         fos.write(fileContentBytes); //write file content to file
    //                         fos.close();
    //                         System.out.println("File received from Server: " + fileName);
    //                     }
    //                 }
    //             } else {
    //                 System.out.println("Error: File not found in the server.");
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
    //     }
    // }

    // static void storeFile(String[] command, Socket endpoint, DataOutputStream writer, String handle) {
    //     try {
    //         //store command needs exactly 2 inputs
    //         if (command.length != 2)
    //             System.out.println("Error: Command parameters do not match or is not allowed.");
    //         else {
    //             //gets file path in client folder
    //             String path = "./files/" + command[1];
    //             File file = new File(path);
    //             if (file.exists() && !file.isDirectory()) {
    //                 writer.writeUTF("/store"); //sends store request to server
    //                 sendFile(endpoint, command[1], file);
    //                 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    //                 LocalDateTime now = LocalDateTime.now();
    //                 System.out.println(handle + "<" + dtf.format(now) + ">: Uploaded " + command[1]); //log message that client has stored file to server
    //             } else {
    //                 System.out.println("Error: File not found.");
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
    //     }
    // }