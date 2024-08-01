import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Server {
    private JFrame frame;
    private JTextField inputField;
    private static JTextArea outputArea;
    private JButton submitButton;
    private boolean hasPort = false;
    public Server(){
        frame = new JFrame("Server\n");
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
        outputArea.setFont(new Font("Monospaced", Font.BOLD, 15)); // Use a monospaced font for better alignment
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add panel to the frame
        frame.add(panel, BorderLayout.NORTH);

        // Add action listener for the submit button
        ActionListener submitListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               if(!hasPort)
               {
                    // Get the input text
                    String msg = inputField.getText();
                    // Start the server in a new thread to avoid blocking the GUI
                    new Thread(() -> start(msg)).start();
    
                    // Clear the input field after processing
                    inputField.setText("");
               }
               else{
                Server.outputArea.append("Server port already exists.\n");
               }
            }
        };

        submitButton.addActionListener(submitListener);
        inputField.addActionListener(submitListener); // Add listener to the text field
    }
    public void display() {
        // Set the frame visible
        frame.setVisible(true);
    }
    
    public void start(String args) {
        int port = Integer.parseInt(args); // Can be changed 
        HashMap<Socket, String> handles = new HashMap<>();
        ArrayList<Socket> chatRoom = new ArrayList<>();
        DMRooms dmRooms = new DMRooms(0);
        try {
            ServerSocket ss = new ServerSocket(port);

            outputArea.append("Server: Listening to port " + port+"\n");

            while (true) {
                // Waits for a client to connect
                Socket endpoint = ss.accept();

                outputArea.append("Server: Client at " + endpoint.getRemoteSocketAddress() + " has connected\n");

                // Make the Thread Object
                Connection connect = new Connection(endpoint, handles, chatRoom, dmRooms, outputArea);
                // Start the thread
                connect.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        // Create and display the SimpleFrame
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                    new Server().display();
                    Server.outputArea.append("Enter port number.\n");
                
            }
        });
    }
}