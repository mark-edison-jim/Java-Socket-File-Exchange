import java.io.DataInputStream;
import java.io.DataOutputStream;


public class DMThread implements Runnable{

    private DataInputStream reader; //reads chatroom messages for each client
    private DataOutputStream writer;
    private boolean exit;
    String handle;
    String otherUser;
    private Thread t;
    public DMThread(DataInputStream reader, DataOutputStream writer, String handle, String otherUser){
        this.reader = reader;
        this.writer = writer;
        this.handle = handle;
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
                writer.writeUTF("/log " + handle +"~"+ otherUser +"~"+ msg);
                if(!msg.equals("/dc")){
                    System.out.println("\r" + msg); //removes current line and adds new lines to simulate new incoming messages
                    System.out.print(handle + ": ");
                    
                }else{
                    stop();
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
