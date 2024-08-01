import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public String handle = "";
    public Socket endpoint;
    public DataInputStream reader; // receives data from Server
    public DataOutputStream writer; // sends data to Server
    private String otherUser;

    public Client() {
        frame = new JFrame("Client\n");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
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
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Use a monospaced font for better alignment
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add panel to the frame
        frame.add(panel, BorderLayout.NORTH);

        // Add action listener for the submit button
        ActionListener submitListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the input text
                String msg = inputField.getText();

                // Process the command
                intro(msg);

                // Clear the input field after processing
                inputField.setText("");
            }
        };
        submitButton.addActionListener(submitListener);
        inputField.addActionListener(submitListener);
    }

    public void display() {
        // Set the frame visible
        frame.setVisible(true);
    }

    private void intro(String input) {
        String msg = input;
        // Starts to read input from client

        String command[] = msg.split(" ");
        if (!isChatting) {
            switch (command[0]) {
                case "/?":
                    printHelpCommands();
                    break;
                case "/join":
                    if (hasJoined) {
                        outputArea.append("Error: User already joined.\n");
                    } else {
                        // join command need exactly 3 inputs
                        if (command.length != 3) {
                            outputArea.append("Error: Command parameters do not match or is not allowed.\n");
                        } else {
                            try {
                                // gets host and port from command input
                                String host = command[1];
                                int port = Integer.parseInt(command[2]);
                                this.endpoint = new Socket(host, port);
                                this.reader = new DataInputStream(endpoint.getInputStream());
                                this.writer = new DataOutputStream(endpoint.getOutputStream());
                                outputArea.append("Connection to the File Exchange. Server is successful!" + host + ":"
                                        + port + "\n");
                                outputArea.append("Type /? to see all commands.\n");
                                hasJoined = true;
                                break;
                            } catch (Exception ex) {
                                outputArea.append(
                                        "Error: Connection to the Server has failed! Please check IP Address and Port Number.\n");
                            }
                        }
                    }
                    break;
                case "/leave":
                    if (!hasJoined) {
                        outputArea.append("Error: Disconnection failed. Please connect to the server first.\n");
                    } else {
                        try {
                            this.writer.writeUTF("END");

                            outputArea.append("Connection closed. Thank you!\n");

                            endpoint.close();
                            hasJoined = false;
                            hasRegistered = false;
                        } catch (IOException ex) {
                        }
                    }

                    break;
                case "/register":
                    if (command.length == 2) {
                        if (!hasRegistered) {
                            if (hasJoined) {
                                try {
                                    writer.writeUTF(msg);

                                    // Attempt to read response from server
                                    String result = reader.readUTF();
                                    if (result.equals("False")) {
                                        outputArea.append(
                                                "Error: Registration failed. Handle or alias already exists.\n");
                                    } else if (result.equals("True")) {
                                        this.handle = command[1];
                                        outputArea.append("Server: Welcome " + command[1] + "!\n");
                                        frame.setTitle("Client: " + handle);
                                        hasRegistered = true;
                                    }
                                } catch (EOFException eofEx) {
                                    outputArea.append("Error: Unexpected end of data stream. Check server response.\n");
                                    eofEx.printStackTrace();
                                } catch (IOException ex) {
                                    outputArea.append("Error: IO Exception occurred.\n");
                                    ex.printStackTrace();
                                }
                            } else
                                outputArea.append("Error: Register failed. Please connect to the server first.\n");
                        } else
                            outputArea.append("Error: Client has already registered\n");

                    } else {
                        outputArea.append("Error: Command parameters do not match or is not allowed.\n");
                    }
                    break;
                case "/store":
                    if (hasJoined && hasRegistered) {
                        try {
                            if (command.length != 2) {
                                outputArea.append("Error: Command parameters do not match or is not allowed.\n");
                            } else {
                                // store command needs exactly 2 inputs
                                String storeCommand[] = msg.split(" ", 2);
                                // gets file path in client folder
                                String path = "./files/" + storeCommand[1];
                                File file = new File(path);
                                if (file.exists() && !file.isDirectory()) {
                                    writer.writeUTF("/store"); // sends store request to server
                                    sendFile(endpoint, storeCommand[1], file);
                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                                    LocalDateTime now = LocalDateTime.now();
                                    outputArea.append(
                                            this.handle + "<" + dtf.format(now) + ">: Uploaded " + storeCommand[1]
                                                    + "\n"); // log message that client hasstored file to server
                                } else {
                                    outputArea.append("Error: File not found.\n");
                                }
                            }
                        } catch (Exception e) {
                            outputArea.append(
                                    "Error: Connection to the Server has failed! Please check IP Address and Port Number.\n");
                        }
                    } else
                        outputArea.append(
                                "Error: Store failed. Please connect, register, or both to the server first.\n");
                    break;
                case "/dir":
                    if (hasJoined && hasRegistered) {
                        try {
                            writer.writeUTF("/dir");
                            outputArea.append("Server Directory:\n");
                            int numFiles = reader.readInt();
                            for (int i = 0; i < numFiles; i++) {
                                outputArea.append(reader.readUTF() + "\n");
                            }
                        } catch (Exception ex) {
                            outputArea.append(
                                    "Error:  Cannot find directory. Please connect, register, or both to the server first or check if the server is on.\n");
                        }
                    } else
                        outputArea.append(
                                "Error:  Cannot find directory. Please connect, register, or both to the server first.\n");
                    break;
                case "/chat":
                    if (hasRegistered) {
                        outputArea.append("Type '/chathelp' to see the list of chat commands.\n");
                        isChatting = true;
                    } else
                        outputArea
                                .append("Error: Chat failed. Please connect, register, or both to the server first.\n");
                    break;
                case "/get":
                    if (hasJoined && hasRegistered) {
                        try {
                            if (command.length != 2) {
                                outputArea.append("Error: Command parameters do not match or is not allowed.\n");
                            } else {
                                writer.writeUTF(msg);
                                if (reader.readBoolean()) {
                                    String fileName = reader.readUTF(); // read file name from server

                                    if (fileName.length() > 0) {
                                        int fileContentLength = reader.readInt(); // read file content length from
                                                                                  // server
                                        if (fileContentLength > 0) {
                                            byte[] fileContentBytes = new byte[fileContentLength];
                                            reader.readFully(fileContentBytes, 0, fileContentLength); // read file
                                                                                                      // content
                                                                                                      // from server

                                            File file = new File("./receivedFiles/" + fileName); // create file in
                                                                                                 // receivedFiles folder
                                                                                                 // in
                                                                                                 // client folder
                                            FileOutputStream fos = new FileOutputStream(file);
                                            fos.write(fileContentBytes); // write file content to file
                                            fos.close();
                                            outputArea.append("File received from Server: " + fileName + "\n");
                                        }
                                    }
                                } else {
                                    outputArea.append("Error: File not found in the server.\n");
                                }
                            }

                        } catch (IOException ex) {
                        }
                    } else
                        outputArea.append(
                                "Error: Get failed. Please connect, register, or both to the server first.\n");
                    break;
                default:
                    if (!(msg.toUpperCase().equals("END"))) {
                        outputArea.append("Error: Command not found.\n");
                    }
                    break;
            }
        } else {
            if (this.isPrivateChatting) {
                if (input.equals("/dc")) {
                    try {
                        outputArea.append("You left the chatroom.\n");
                        writer.writeUTF("/dcDM " + this.handle + "~" + this.otherUser);
                        isPrivateChatting = false;
                    } catch (IOException ex) {
                    }
                } else {
                    try {
                        writer.writeUTF("/dm " + msg + "~" + this.handle + "~" + this.otherUser); // sends message to
                                                                                                  // server
                        outputArea.append(handle + ": " + msg + "\n");
                        // outputArea.append(this.handle + ": ");
                    } catch (IOException ex) {
                    }
                }
            } else if (this.isGroupChatting) {
                if (input.equals("/dc")) {
                    isGroupChatting = false;
                    try {
                        outputArea.append("You left the chatroom.\n");
                        writer.writeUTF("/dcCR");
                        isGroupChatting = false;
                    } catch (IOException ex) {
                    }
                } else {
                    try {
                        outputArea.append(handle + ": " + msg + "\n");
                        writer.writeUTF("/cr " + msg); // sends message to server
                    } catch (IOException ex) {
                    }
                }
            } else {
                switch (command[0]) {
                    case "/chathelp":
                        printChatCommands();
                        break;
                    case "/listUsers":
                        if (hasJoined && hasRegistered) {
                            try {
                                writer.writeUTF("/listUsers");
                                outputArea.append("Available Users:\n");
                                int numUsers = reader.readInt();
                                for (int i = 0; i < numUsers; i++) {
                                    outputArea.append(reader.readUTF() + "\n");
                                }
                            } catch (Exception ex) {
                                outputArea.append(
                                        "Error:  Cannot find list. Please connect, register, or both to the server first or check if the server is on.\n");
                            }
                        } else
                            outputArea.append(
                                    "Error:  Cannot find list. Please connect, register, or both to the server first.\n");
                        break;
                    case "/chatroom":
                        outputArea.append("Type /dc to leave the chatroom.\n");
                        try {
                            writer.writeUTF("/joinCR"); // sends join chatroom request
                            new ChatroomThread(this.reader, this.handle, this.outputArea); // starts chatroom thread for
                                                                                           // client to keep waiting for
                                                                                           // messages from server
                            // outputArea.append(this.handle + ": ");
                            isGroupChatting = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "/whisper":
                        if (command.length == 2) {
                            this.otherUser = command[1].trim();
                            try {
                                writer.writeUTF("/check " + this.handle + "~" + this.otherUser);
                                writer.flush();

                                String response = reader.readUTF();
                                if ("True".equals(response)) {
                                    this.isPrivateChatting = true;
                                    outputArea.append("Type /dc to leave the chatroom.\n");
                                    new DMThread(reader, outputArea); // starts chatroom thread for client to keep
                                                                      // waiting for messages from server
                                    writer.writeUTF("/joinDM " + this.handle + "~" + this.otherUser);
                                    outputArea.append("Chatting with : " + this.otherUser + "\n");

                                } else if ("False1".equals(response)) {
                                    outputArea.append("Error: Cannot Whisper Yourself!\n");
                                } else {
                                    outputArea.append("Error: User not found!\n");
                                }
                            } catch (IOException e) {
                                outputArea.append("Error: Communication issue.\n");
                                e.printStackTrace();
                            }
                        } else {

                            outputArea.append("Error: Command not found.\n");
                        }
                        break;
                    case "/chatleave": {
                        isChatting = false;
                        outputArea.append("You Have Left Chat. \nType /? to see all commands.\n");
                        break;
                    }
                    default:
                        if (!(msg.equals("/chatleave"))) {
                            outputArea.append("Error: Command not found.\n");
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
                "| Chat with another user                       | /whisper <username>                 | /whisper User1          |\n");
        outputArea.append(
                "+----------------------------------------------+-------------------------------------+-------------------------+\n");
        outputArea.append(
                "| List all available users                     | /listUsers                          | /listUsers              |\n");
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
            FileInputStream fis = new FileInputStream(file.getAbsolutePath()); // gets file path in client folder
            DataOutputStream dos = new DataOutputStream(endpoint.getOutputStream()); // sends file to server

            byte[] fileContentBytes = new byte[(int) file.length()]; // gets file size and data

            fis.read(fileContentBytes); // reads file data and stores in byte array

            dos.writeUTF(fileName); // sends file name

            dos.writeInt(fileContentBytes.length); // sends file size
            dos.write(fileContentBytes); // sends file data

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