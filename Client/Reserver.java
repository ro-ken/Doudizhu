
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Reserver extends Thread{
    Socket socket;
    static boolean flag=true;
    public Reserver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String s = null;
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (flag){
                while ((s = reader.readLine())!=null) {//是否为阻塞读？是
                    if (s.length()==Client.SYN_STATUS.length()+1){ //同步序列码
                        if (s.substring(0,s.length()-1).equals(Client.SYN_STATUS))
                        {
                            Client.gameStatus=GameStatus.values()[s.charAt(s.length()-1)-48];
                            break;
                        }
                    }
                    switch (Client.gameStatus){
                        case SELECTING:selecting(s);break;
                        case RUNNING:running(s);break;
                        default: System.out.println(s);;break;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("系统提示： 您已下线！");
        }
    }

    private void finish(String s) {

    }

    private void running(String s) {
        String subMsg=s.substring(0,s.length()-1);
        int turn = s.charAt(s.length() - 1) - 48;
        switch (subMsg){
            case Client.SYN_SYSTURN:Client.turn=turn;return;
            case Client.SYN_lastTurn:Client.lastPlayerTurn=turn;return;
            default:break;
        }
        if (Client.SYN_CARD.equals(s.substring(0,Client.SYN_CARD.length()))){ //更新手牌
            Client.myCard=new ArrayList<>();
            Client.myCard.addAll(Arrays.asList(s.substring(Client.SYN_CARD.length(),s.length()).split("")));
            Client.printCard(Client.myCard);
            return;
        }
        if (Client.SYN_HISTORY.equals(s.substring(0,Client.SYN_HISTORY.length()))){ //历史记录
            System.out.println(s.substring(Client.SYN_HISTORY.length(),s.length()));
            Client.history.append(s).append("\n");  //加一条历史记录
            return;
        }
        if (Client.SYN_LEFTCARDNUM.equals(s.substring(0,Client.SYN_LEFTCARDNUM.length()))){ //更新手牌数量
            for (int i = 0; i < Client.leftNum.length; i++) {
                String nums=s.substring(Client.SYN_LEFTCARDNUM.length(),s.length());
                Client.leftNum[i]=Integer.parseInt((nums).split(",")[i]);
            }
            System.out.println(Client.history);////
            Client.printCard(Client.myCard);
            return;
        }
        System.out.println(s);//只是一条普通信息
    }

    private void selecting(String s) {
        String subMsg=s.substring(0,s.length()-1);
        int turn = s.charAt(s.length() - 1) - 48;
        switch (subMsg){
            case Client.SYN_SYSTURN:Client.turn=turn;return;
            case Client.SYN_TURN:Client.myturn=turn;return;
            case Client.SYN_LANDER:Client.lander=turn;return;
            default:break;
        }
        if (Client.SYN_CARD.equals(s.substring(0,Client.SYN_CARD.length()))){ //发牌阶段，接收卡牌
            Client.myCard=new ArrayList<>();
            Client.myCard.addAll(Arrays.asList(s.substring(Client.SYN_CARD.length(),s.length()).split("")));
            Client.printCard(Client.myCard);
            return;
        }
        if (s.charAt(0)=='['&&s.charAt(s.length()-1)==']'){
            System.out.println(Client.SYS_INFO+"底三张为"+s);
            if (Client.lander==Client.myturn){
                String tmp=s.substring(1,s.length()-1);    //把列表的中括号去掉
                String[] split = tmp.split(", ");
                Client.myCard.addAll(Arrays.asList(split));
                Client.printCard(Client.myCard);
                return;
            }
            return;
        }
        System.out.println(s);//只是一条普通信息
    }
}
