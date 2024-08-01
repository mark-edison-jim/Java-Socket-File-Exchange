import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.swing.JTextArea;


public class DMThread implements Runnable{

    private DataInputStream reader; //reads chatroom messages for each client
    private DataOutputStream writer;
    private boolean exit;
    String handle;
    String otherUser;
    private JTextArea outputArea;
    private Thread t;
    public DMThread(DataInputStream reader, DataOutputStream writer, String handle, String otherUser, JTextArea outputArea){
        this.reader = reader;
        this.writer = writer;
        this.handle = handle;
        this.otherUser = otherUser;
        this.outputArea = outputArea;
        exit = false;
        t = new Thread(this);
        t.start();
    }

    

    @Override
    public void run() {
        try {
            while (!exit) {
               Thread.sleep(200);
                String msg = reader.readUTF();
                String message[] = msg.split(" ", 2);
                if(!message[0].equals("/skip")){
                    if(!msg.equals("/dc")){
                        outputArea.append(msg+"\n"); //removes current line and adds new lines to simulate new incoming messages

                        
                    }else{
                        stop();
                    }
                }
                else{
                        outputArea.append(message[1]+"\n"); //removes current line and adds new lines to simulate new incoming messages
                }

            }
        } catch (Exception e) {
            System.out.print("");
        }
    }

    public void stop() throws InterruptedException{
        exit = true;
    }

    public void join() throws InterruptedException{
        t.join();
    }



}