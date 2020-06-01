
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception{
        initVar();
        System.out.println("欢迎您登录游戏！，输入< exit >退出");
        new Reserver(socket).start();
        scheduler();
        Reserver.flag=false;
        writer.close();
        socket.close();
    }

    /* 按规则对card进行排序 */
    private static void sortCard(ArrayList<String> card) {
        List tmp= (List) card.clone();
        card.clear();
        for (int i = rule.length-1; i >= 0; i--) {
            String key=rule[i];
            while (tmp.contains(key)){
                card.add(key);
                tmp.remove(key);
            }
        }
    }
    private static void scheduler() throws IOException {
        String text;
        while (!(text=sc.nextLine()).equals("exit")){
            switch (gameStatus){
                case WAITING:waiting(text);break;
                case PREPARATION:preparation(text);break;
                case SELECTING:selecting(text);break;
                case RUNNING:running(text);break;
                case FINISH:finish(text);break;
            }
        }
    }

    private static void waiting(String text) throws IOException {
        sendMessage(text);
    }

    private static void finish(String text) throws IOException {
        waiting(text);
    }

    private static void running(String text) throws IOException {
        if (turn!=myturn){
            System.out.println(SYS_INFO+"当前不轮到您出牌！");
            return;
        }
        if (text.equals("pass")){
            if (myturn!=lastPlayerTurn) sendMessage(text);
            else System.out.println(SYS_INFO+"您必须打出一张牌！");
            return;
        }
        Rule.Type type = Rule.isCardCorrect(text);
        if (type == Rule.Type.Error) {
            System.out.println(SYS_INFO+"您输入的语法有错误，请重新输入！");
            return;
        }
        boolean b = isNotOutOfBound((ArrayList<String>) myCard, text);
        if(!b){
            System.out.println(SYS_INFO+"您输入的牌多于，您的所拥有手牌，请重新输入！");
            return;
        }
        sendMessage(text);  //检测无误，提交服务器
    }

    private static boolean isNotOutOfBound(ArrayList<String> allCards, String cards) {
        ArrayList<String> clone = (ArrayList) allCards.clone();
        String[] split = cards.split("");
        boolean flag;
        for (String s : split) {
            flag=false;
            for (String card : clone) {
                if (card.equals(s)){
                    clone.remove(card);
                    flag=true;  //找到了卡片，检查下一张
                    break;
                }
            }
            if (!flag) return false;
        }
        return true;
    }

    private static void deleteCard(ArrayList<String> allCards, String cards) {
        for (String s : cards.split("")) {
            allCards.remove(s);
        }
    }

    private static void selecting(String text) throws IOException {
        if (!text.equals("yes")&&!text.equals("no")){
            System.out.println(SYS_INFO+"您输入的内容无效，请输入< yes >或< no >");
        }
        else if (turn==myturn){
            sendMessage(text);
        }else System.out.println(SYS_INFO+"当前不轮到您选择...");
    }

    private static void preparation(String text) throws IOException {
        if (!text.equals("yes")){
            System.out.println(SYS_INFO+"您输入的内容无效，请输入< yes >确认");
        }
        sendMessage(text);
    }

    private static void sendMessage(String text) throws IOException {
        writer.write(text);
        writer.newLine();
        writer.flush();
    }

    /*初始化变量*/
    private static void initVar() throws IOException {
        myCard=new ArrayList<>();
        leftNum=new int[3];
        history=new StringBuilder();
        gameStatus=GameStatus.WAITING;
        socket=new Socket(InetAddress.getByName("localhost"), 6666);
        writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        sc = new Scanner(System.in);
        rule="3456789XJQKA2-+".split("");//数组下标及卡牌大小
    }

    /**
     * 以卡片形式打印卡牌
     * @param cards 要打印的卡牌
     */
    public static void printCard(List<String> cards) {
        System.out.print("\n您的卡牌为：\t\t手牌剩余数量 | ");
        for (int i = 0; i < Client.leftNum.length; i++) {
            if (i==Client.myturn) {
                System.out.print(" 您"+i+"号："+Client.leftNum[i]+"张\t |");
            }
            else if (i==Client.lander) System.out.print(" 地主"+i+"号："+Client.leftNum[i]+"张\t |");
            else System.out.print(" 玩家"+i+"号："+Client.leftNum[i]+"张\t |");
        }
        System.out.println();
        sortCard((ArrayList<String>) cards);
        for (int i = 0; i < cards.size(); i++) {
            System.out.print(" ___");
        }
        System.out.println();
        for (String item:cards){
            System.out.print("| "+item+" ");
        }
        System.out.println("|");
    }
    /******************成员变量******************/
    public static List<String> myCard;
    private static BufferedWriter writer;
    public static Socket socket;
    public static GameStatus gameStatus;
    public static Scanner sc;
    public static final String SYS_INFO="系统提示： ";
    public static int myturn;
    public static int turn;
    public static int lander;
    public static int lastPlayerTurn;
    public static StringBuilder history;
    public static int[] leftNum;
    private static String[] rule; //每张牌的大小规则
    public static final String SYN_STATUS = "dfd4fa1cs1f6a5s4e8w";//设置状态同步序列码
    public static final String SYN_TURN = "syn_turn";//设置顺序同步序列码
    public static final String SYN_SYSTURN = "syn_systurn";//设置系统出牌顺序同步序列码
    public static final String SYN_CARD = "card";//设置系统出牌顺序同步序列码
    public static final String SYN_LANDER = "lander";//设置系统出牌顺序同步序列码
    public static final String SYN_lastTurn = "last_turn";//设置最后一次出牌者序号同步序列码
    public static final String SYN_HISTORY ="历史记录：";  //记录出牌历史
    public static final String SYN_LEFTCARDNUM ="left";  //玩家剩余手牌同步信号
}

