
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;

public class SendThread extends Thread{
    Socket socket;
    StringBuffer message;
    BufferedWriter writer;
    public SendThread(Socket socket, StringBuffer stringBuffer) {
        this.socket = socket;
        this.message = stringBuffer;
    }
    @Override
    public void run() {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true){
                synchronized (message) {
                    writer.write(String.valueOf(message));
                    writer.newLine();
                    writer.flush();
                    message.delete(0,message.length());
                    message.wait();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
