
import java.io.*;
import java.net.Socket;
import java.util.List;
class ReserveThread extends Thread{
    Socket socket;
    private List<Socket> socketList;
    String name;
    boolean ack;
    int turn;

    public ReserveThread(Socket socket, List<Socket> socketList, String name) {
        this.socket = socket;
        this.socketList = socketList;
        this.name =name;
        turn=name.charAt(2)-48;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true){
                String s=null;
                while ((s=reader.readLine())!=null){
                    switch (Server.gameStatus){
                        case WAITING: waiting(s);break ;
                        case PREPARATION: preparation(s);break ;
                        case SELECTING: selecting(s); break ;
                        case RUNNING: running(s);break ;
                        case FINISH: finish(s);break ;
                    }
                }
            }
        } catch (Exception ignored) {

        }finally {
            socketList.remove(socket);
            Server.reserveBuffer.append(Server.SYS_INFO).append(name).append("已下线      （当前人数：").append(socketList.size()).append(")");
        }
    }

    private void selecting(String s) {
        if (Server.turn==turn){
            if ("yes".equals(s) || "no".equals(s))
                Server.reserveBuffer.append(s);
        }
    }

    private void finish(String s) throws IOException {
        waiting(s); //功能和waiting阶段差不多
    }

    private void running(String s) {
        if (Server.turn==turn){
            Server.reserveBuffer.append(s);
        }
    }

    private void preparation(String s) {
        if ("yes".equals(s)&&!ack){
            Server.reserveBuffer.append(name).append(": ").append("yes");
            synchronized (Server.reserveBuffer){
                Server.reserveBuffer.notify();
            }
            ack=true;
        }
    }

    private void waiting(String s) throws IOException {
        Server.reserveBuffer.append(name).append(": ").append(s);
        synchronized (Server.reserveBuffer){
            Server.reserveBuffer.notify();
        }
    }
}