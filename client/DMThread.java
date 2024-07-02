import java.io.DataInputStream;

public class DMThread implements Runnable{
    private DataInputStream reader;
    private boolean exit;
    String handle;
    private Thread t;

    public DMThread(DataInputStream reader, String handle){
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
                String msg[] = reader.readUTF().split(" ", 2);
                if(msg[0].equals("/requestDM")){
                    System.out.println("\r" + msg[1] + " wants to chat with you, /acc to accept, /dec to decline...");
                    System.out.print("> ");
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
