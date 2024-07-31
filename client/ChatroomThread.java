import java.io.DataInputStream;
import javax.swing.JTextArea;


public class ChatroomThread implements Runnable{

    private DataInputStream reader; //reads chatroom messages for each client
    private boolean exit;
    String handle;
    private Thread t;
    private JTextArea outputArea;
    public ChatroomThread(DataInputStream reader, String handle, JTextArea outputArea){
        this.reader = reader;
        this.handle = handle;
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
                if(!msg.equals("/dc")){
                    outputArea.append("\r" + msg+"\n"); //removes current line and adds new lines to simulate new incoming messages
                    outputArea.append(handle + ": \n");
                    
                }else{
                    stop();
                }
            }
        } catch (Exception e) {
            outputArea.append("");
        }
    }

    public void stop() throws InterruptedException{
        exit = true;
    }

    public void join() throws InterruptedException{
        t.join();
    }



}