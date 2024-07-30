import java.io.DataInputStream;

public class ChatroomThread implements Runnable{

    private DataInputStream reader; //reads chatroom messages for each client
    private boolean exit;
    String handle;
    private Thread t;
    private String message;
    public ChatroomThread(DataInputStream reader, String handle){
        this.reader = reader;
        this.handle = handle;
        this.message = null;
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
                this.message = msg;
                if(!msg.equals("/dc")){
                    System.out.println("\r" + msg); //removes current line and adds new lines to simulate new incoming messages
                    System.out.print("From ChatroomThread - " + handle + ": ");
                    
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

    public String getMessage(){
        return this.message;
    }


}
