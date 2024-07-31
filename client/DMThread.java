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
                String message[] = msg.split(" ", 2);
               // System.out.print("MESSAGE [0] : " + message[0]);
                if(!message[0].equals("/skip")){
                    writer.writeUTF("/log " + handle +"~"+ otherUser +"~"+ msg);
                    if(!msg.equals("/dc")){
                        System.out.println("\r" + msg); //removes current line and adds new lines to simulate new incoming messages
                        System.out.print(handle + ": ");
                        
                    }else{
                        stop();
                    }
                }
                else{
                    String trimmedMsg[] = message[1].split(" ", 2);
                  //  System.out.print("TrimmedMsg [0] : " + trimmedMsg[0]);
                    switch(trimmedMsg[0])
                    {
                        case "/joined":
                     //   System.out.print("CASE 1 : " + trimmedMsg[1]);
                        break;
                        case "/curr":
                    //    System.out.print("CASE 2 : " + trimmedMsg[1]);
                        String currMsg[] = trimmedMsg[1].split(" ", 2);
                        
                        if(!trimmedMsg[1].isEmpty()) {
                            writer.writeUTF("/log " + handle +"~"+ otherUser +"~"+ currMsg[1]);
                            System.out.println("\r" + trimmedMsg[1]); //removes current line and adds new lines to simulate new incoming messages
                        }

                        break;
                        case "/left":
                    //    System.out.print("CASE 3 : " + trimmedMsg[1]);
                        break;
                        default:
                        break;
                    }
                   
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
