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
    public DMThread(DataInputStream reader, DataOutputStream writer, String handle, String otherUse, JTextArea outputArea){
        this.reader = reader;
        this.writer = writer;
        this.handle = handle;
        this.outputArea = outputArea;
        this.otherUser = otherUser;
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
                writer.writeUTF("/log " + handle +"~"+ otherUser +"~"+ msg+"\n");
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
