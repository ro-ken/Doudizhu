
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception{
        initVar();
        System.out.println("��ӭ����¼��Ϸ��������< exit >�˳�");
        new Reserver(socket).start();
        scheduler();
        Reserver.flag=false;
        writer.close();
        socket.close();
    }

    /* �������card�������� */
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
            System.out.println(SYS_INFO+"��ǰ���ֵ������ƣ�");
            return;
        }
        if (text.equals("pass")){
            if (myturn!=lastPlayerTurn) sendMessage(text);
            else System.out.println(SYS_INFO+"��������һ���ƣ�");
            return;
        }
        Rule.Type type = Rule.isCardCorrect(text);
        if (type == Rule.Type.Error) {
            System.out.println(SYS_INFO+"��������﷨�д������������룡");
            return;
        }
        boolean b = isNotOutOfBound((ArrayList<String>) myCard, text);
        if(!b){
            System.out.println(SYS_INFO+"��������ƶ��ڣ�������ӵ�����ƣ����������룡");
            return;
        }
        sendMessage(text);  //��������ύ������
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
                    flag=true;  //�ҵ��˿�Ƭ�������һ��
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
            System.out.println(SYS_INFO+"�������������Ч��������< yes >��< no >");
        }
        else if (turn==myturn){
            sendMessage(text);
        }else System.out.println(SYS_INFO+"��ǰ���ֵ���ѡ��...");
    }

    private static void preparation(String text) throws IOException {
        if (!text.equals("yes")){
            System.out.println(SYS_INFO+"�������������Ч��������< yes >ȷ��");
        }
        sendMessage(text);
    }

    private static void sendMessage(String text) throws IOException {
        writer.write(text);
        writer.newLine();
        writer.flush();
    }

    /*��ʼ������*/
    private static void initVar() throws IOException {
        myCard=new ArrayList<>();
        leftNum=new int[3];
        history=new StringBuilder();
        gameStatus=GameStatus.WAITING;
        socket=new Socket(InetAddress.getByName("localhost"), 6666);
        writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        sc = new Scanner(System.in);
        rule="3456789XJQKA2-+".split("");//�����±꼰���ƴ�С
    }

    /**
     * �Կ�Ƭ��ʽ��ӡ����
     * @param cards Ҫ��ӡ�Ŀ���
     */
    public static void printCard(List<String> cards) {
        System.out.print("\n���Ŀ���Ϊ��\t\t����ʣ������ | ");
        for (int i = 0; i < Client.leftNum.length; i++) {
            if (i==Client.myturn) {
                System.out.print(" ��"+i+"�ţ�"+Client.leftNum[i]+"��\t |");
            }
            else if (i==Client.lander) System.out.print(" ����"+i+"�ţ�"+Client.leftNum[i]+"��\t |");
            else System.out.print(" ���"+i+"�ţ�"+Client.leftNum[i]+"��\t |");
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
    /******************��Ա����******************/
    public static List<String> myCard;
    private static BufferedWriter writer;
    public static Socket socket;
    public static GameStatus gameStatus;
    public static Scanner sc;
    public static final String SYS_INFO="ϵͳ��ʾ�� ";
    public static int myturn;
    public static int turn;
    public static int lander;
    public static int lastPlayerTurn;
    public static StringBuilder history;
    public static int[] leftNum;
    private static String[] rule; //ÿ���ƵĴ�С����
    public static final String SYN_STATUS = "dfd4fa1cs1f6a5s4e8w";//����״̬ͬ��������
    public static final String SYN_TURN = "syn_turn";//����˳��ͬ��������
    public static final String SYN_SYSTURN = "syn_systurn";//����ϵͳ����˳��ͬ��������
    public static final String SYN_CARD = "card";//����ϵͳ����˳��ͬ��������
    public static final String SYN_LANDER = "lander";//����ϵͳ����˳��ͬ��������
    public static final String SYN_lastTurn = "last_turn";//�������һ�γ��������ͬ��������
    public static final String SYN_HISTORY ="��ʷ��¼��";  //��¼������ʷ
    public static final String SYN_LEFTCARDNUM ="left";  //���ʣ������ͬ���ź�
}

