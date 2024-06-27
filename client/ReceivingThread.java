import java.io.DataInputStream;

public class ReceivingThread implements Runnable{

    private DataInputStream reader;
    private boolean exit;
    String handle;
    private Thread t;
    public ReceivingThread(DataInputStream reader, String handle){
        this.reader = reader;
        this.handle = handle;
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
                    System.out.println("\r" + msg);
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
