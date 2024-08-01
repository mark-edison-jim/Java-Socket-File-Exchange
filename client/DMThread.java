import java.io.DataInputStream;
import javax.swing.JTextArea;


public class DMThread implements Runnable{

    private DataInputStream reader; //reads chatroom messages for each client
    private boolean exit;
    private JTextArea outputArea;
    private Thread t;
    public DMThread(DataInputStream reader, JTextArea outputArea){
        this.reader = reader;
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
                        outputArea.append(msg+"\n"); //removes current line and adds new lines to simulate new incoming messages
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