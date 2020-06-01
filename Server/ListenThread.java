
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ListenThread extends Thread{
    private ServerSocket socket;
    private List<Socket> socketList ;
    private StringBuffer[] sendBuffer;
    public ListenThread(ServerSocket socket, List<Socket> socketList, StringBuffer[] sendBuffer) {
        this.socket = socket;
        this.socketList = socketList;
        this.sendBuffer = sendBuffer;
    }

    @Override
    public void run() {
        try {
            while (true){
                Socket accept = socket.accept(); /* 阻塞监听，若成功则创建套接字 */
                socketList.add(accept);         /* 为每一个客户端创建一个接收消息的线程 */
                String name=null;
                for (int i = 0; i < 3; i++) {
                    if (!Server.sendBufferStatus.get(sendBuffer[i])){
                        name="玩家"+i;
                        new SendThread(accept,sendBuffer[i]).start();  //创建发送线程
                        Server.sendBufferStatus.put(sendBuffer[i],true);
                        break;
                    }
                }
                new ReserveThread(accept,socketList,name).start();
                Server.reserveBuffer.append(Server.SYS_INFO).append(name).append("加入群聊      （当前人数：").append(socketList.size()).append(")");
                synchronized (Server.reserveBuffer){
                    Server.reserveBuffer.notify();
                }
                if (socketList.size()==3){      //玩家到齐，进入就绪状态
                    Server.gameStatus=GameStatus.PREPARATION;
/*                    synchronized ("wait"){
                        "wait".notify();
                    }*/
                    synchronized (this){
                        wait();     // 设置三个线程上限，达到就阻塞
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}