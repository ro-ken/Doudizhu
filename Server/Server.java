
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static void main(String[] args)throws Exception {
        gameStatus=GameStatus.WAITING;
        initVar();
        listenThread.start();
        Scheduler();
    }

    private static void Scheduler() throws Exception {
        waiting();
        preparation();
        selecting();
        running();
        finish();
    }

    private static void finish() throws InterruptedException {
        gameStatus=GameStatus.FINISH;
        sendBoardMessage(SYN_STATUS+4);//发送同步序列码，提示客户端进入结束阶段。
        while (true){
            synchronized (reserveBuffer) {
                reserveBuffer.wait();
            }
            sendBoardMessage(reserveBuffer.toString());
            reserveBuffer.delete(0,reserveBuffer.length());
        }
    }

    private static void running() throws InterruptedException {
        sendBoardMessage(SYN_STATUS+3);//发送同步序列码，提示客户端进入运行阶段。
        String name;
        int lastPlayTurn=turn=lander;//记录上一次谁最后出牌
        StringBuilder lastPlayCard = new StringBuilder();//记录上一次打出的卡片
        while (true){
            sendBoardMessage(SYS_INFO+"");
            sendBoardMessage(SYN_SYSTURN+turn);
            sendBoardMessage(SYN_lastTurn+lastPlayTurn);
            sendBoardMessage(SYN_LEFTCARDNUM+player[0].size()+","+player[1].size()+","+player[2].size());
            name=turn==lander?"地主":"农民";
            sendMessage(sendBuffer[(turn + 1) % 3], SYS_INFO + name + "玩家" + turn +  "正在出牌...");
            sendMessage(sendBuffer[(turn + 2) % 3], SYS_INFO + name + "玩家" + turn + "正在出牌...");
            if (lastPlayTurn==turn){
                sendMessage(sendBuffer[turn], SYS_INFO + "请您出牌...               "+thinkTime+"S倒计时");
                if (waitPlayerPlayCard(turn, name, lastPlayCard, false)){    //未出牌，自动打一张
                    String remove = player[turn].remove(player[turn].size()-1);//从玩家手里删除牌
                    lastPlayCard.delete(0,lastPlayCard.length());
                    lastPlayCard.append(remove);        //更新最后一次出牌
                    sendBoardMessage(SYN_HISTORY + name + "玩家" + turn +  "： "+remove);
                    sendMessage(sendBuffer[turn],player[turn]);
                }
            }
            else {
                sendMessage(sendBuffer[turn], SYS_INFO + "请您出牌,若不出请输入< pass >              "+thinkTime+"S倒计时");
                if (waitPlayerPlayCard(turn, name, lastPlayCard, true)) {
                   sendBoardMessage(SYS_INFO + name + "玩家" + turn + "pass");
                }else lastPlayTurn=turn;    //打出更新序号
            }
            if (player[turn].size()==0){
                sendBoardMessage(SYS_INFO+"恭喜"+name+"获得胜利！！！");
                sendBoardMessage(SYS_INFO+"本场倍率: "+rate+",愿赌服输，自觉转账！！！");
                return;
            }
            turn=(++turn)%3; //进入下一个玩家
        }
    }
    //打出返回false，未打出返回true
    private static boolean waitPlayerPlayCard(int turn, String name, StringBuilder
                            lastPlayCard, boolean needCompare) throws InterruptedException {
        for (int i = 0; i < thinkTime; i++) {
            String content = reserveBuffer.toString();
            reserveBuffer.delete(0,reserveBuffer.length());
            if (content.equals(""))
                Thread.sleep(1000);
            else if (content.equals("pass") && needCompare) return true;   //要不起
            else{
                Rule.Type type = Rule.isCardCorrect(content);
                if (type!= Rule.Type.Error)  //是否有语法错误
                    if (isNotOutOfBound((ArrayList<String>) player[turn],content)){     //是否超出手牌限制
                        if (needCompare){
                            if (!Rule.compare(content,lastPlayCard.toString(),type)){
                                sendMessage(sendBuffer[turn],SYS_INFO+"您出的牌比不够大");
                                break;
                            }
                        }
                        if (type==Rule.Type.Bomb) rate=rate*2;//炸弹翻倍
                        deleteCard((ArrayList<String>) player[turn],content);   //从玩家手里删除牌
                        sendBoardMessage(SYN_HISTORY + name + "玩家" + turn +  "： "+content);
                        sendMessage(sendBuffer[turn],player[turn]);     //更新玩家手牌
                        lastPlayCard.delete(0,lastPlayCard.length());
                        lastPlayCard.append(content);
                        return false;    //玩家出牌
                    }else sendMessage(sendBuffer[turn],SYS_INFO+"您出的牌超过限制");
                else sendMessage(sendBuffer[turn],SYS_INFO+"您出的牌有语法错误");
            }
        }
        return true;   //玩家没有出牌
    }

    //打出的是不是手里有的
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

    private static void selecting() throws InterruptedException {
        sendBoardMessage(SYN_STATUS+2);//发送同步序列码，提示客户端进入抢地主阶段。
        turn=new Random().nextInt(player.length);  //随机一个出牌顺序
        for (int i = 0; i < player.length; i++) {
            sendMessage(sendBuffer[i],SYN_TURN+i);  //告诉每个玩家自己的顺序
        }
        //给每位玩家发牌
        for (int i = 0; i < player.length; i++) {
            sendMessage(sendBuffer[i],player[i]);
        }
        //选地主
        for (int i = 0; i < player.length; i++) {
            ++turn;
            sendBoardMessage(SYN_SYSTURN + (turn %= 3));     //发送当前系统顺序
            sendMessage(sendBuffer[turn], SYS_INFO + "您是否要抢地主,输入 < yes > 或 < no >, 10S倒计时");
            sendMessage(sendBuffer[(turn + 1) % 3], SYS_INFO + "玩家" + turn + "正在选择，请耐心等待...");
            sendMessage(sendBuffer[(turn + 2) % 3], SYS_INFO + "玩家" + turn + "正在选择，请耐心等待...");
            for (int j = 0; j < 10; j++) {
                if ("yes".contentEquals(reserveBuffer)) {
                    lander = turn;
                    sendBoardMessage(SYS_INFO + "地主为玩家" + lander);
                    sendBoardMessage(SYN_LANDER + lander);    //告诉客户端地主编号
                    sendBoardMessage(lastCard.toString());
                    reserveBuffer.delete(0, reserveBuffer.length());
                    player[lander].addAll(lastCard);
                    sortCard((ArrayList<String>) player[lander]);
                    gameStatus = GameStatus.RUNNING;  //  选好地主，进入下一阶段
                    return;
                } else if ("no".contentEquals(reserveBuffer)) {
                    reserveBuffer.delete(0, reserveBuffer.length());
                    break;
                }
                Thread.sleep(1000);//一秒钟检查一次状态
            }
        }
        sendBoardMessage(SYS_INFO+"没有玩家选择地主，重新洗牌！");
        refreshPlayerCard();
        selecting();
    }

    private static void sendMessage(StringBuffer player, List<String> card) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (player){
            player.append(SYN_CARD);
            for (String s:card){
                player.append(s);
            }
            player.notify();
        }
    }
    private static void sendMessage(StringBuffer player, String msg) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (player){
            player.append(msg);
            player.notify();
        }
    }

    private static void preparation() throws InterruptedException {
        sendBoardMessage(SYN_STATUS+1);//发送同步序列码，提示客户端进入准备状态。
        int ack=0;
        while (true) {
            synchronized (reserveBuffer) {
                reserveBuffer.wait();
            }
            sendBoardMessage(reserveBuffer.toString());
            reserveBuffer.delete(0,reserveBuffer.length());
            if (++ack==3) {             //全部就绪，进入抢地主状态
                gameStatus=GameStatus.SELECTING;
                sendBoardMessage(SYS_INFO+"所有玩家已确定，游戏现在开始！");
                return;
            }
        }
    }

    private static void waiting() throws InterruptedException {
        while (true){
            synchronized (reserveBuffer) {
                reserveBuffer.wait();
            }
            sendToOnline(reserveBuffer.toString());
            reserveBuffer.delete(0,reserveBuffer.length());
            if (gameStatus==GameStatus.PREPARATION){
                sendBoardMessage(SYS_INFO+"所有玩家以进入，输入< yes >确认开始游戏");
                return;
            }
        }
    }
    //发送广播消息，三个玩家全部上线
    private static void sendBoardMessage(String msg){
        try {
            Thread.sleep(100);  //睡眠0.2S让服务器有时间发
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (StringBuffer stringBuffer : sendBuffer) {
            synchronized (stringBuffer) {
                stringBuffer.append(msg);
                stringBuffer.notify();
            }
        }
    }
    //发送广播消息，可能少于三人
    private static void sendToOnline(String msg){
        for (StringBuffer stringBuffer : sendBuffer) {
            if (sendBufferStatus.get(stringBuffer)) {
                synchronized (stringBuffer) {
                    stringBuffer.append(msg);
                    stringBuffer.notify();
                }
            }
        }
    }

    /**
     * 初始化成员变量
     * @throws Exception
     */
    private static void initVar() throws Exception {
        System.out.println("该服务器地址为<"+ InetAddress.getByName("localhost").getHostAddress() +">,端口为<"+port+">");
        reserveBuffer=new StringBuffer();
        List<Socket> socketList = new ArrayList<>();
        ServerSocket serverSocket = new ServerSocket(port);/* 开启监听客户端连接的线程 */
        sendBuffer =new StringBuffer[3];
        for (int i = 0; i < sendBuffer.length; i++) {
            sendBuffer[i]=new StringBuffer();
        }
        sendBufferStatus=new HashMap<>();
        for (StringBuffer buffer:sendBuffer)
            sendBufferStatus.put(buffer,false); //先把三个输出口置为false
        listenThread=new ListenThread(serverSocket, socketList,sendBuffer);
        rule="3456789XJQKA2-+".split("");//数组下标及卡牌大小
        initCard();//初始化卡牌
    }

    private static void initCard() {
        allCard=new HashMap<>();
        for (String key:rule)
            if (key.equals("-")||key.equals("+"))
                allCard.put(key,1);     //一张大小王
            else allCard.put(key,4);    //其余卡牌为四张
        player=new List[3];
        refreshPlayerCard();
    }
    //重新洗牌
    private static void refreshPlayerCard() {
        player[0]=new ArrayList<>();
        player[1]=new ArrayList<>();
        player[2]=new ArrayList<>();
        lastCard=new ArrayList<>();
        Map<String,Integer> tmpCard= (Map<String, Integer>) allCard.clone();
        givenCard(player[0],tmpCard,17);
        givenCard(player[1], tmpCard, 17);
        givenCard(player[2], tmpCard, 17);
        //showCards();
    }

    private static void showCards() {
        printCard(player[0]);
        printCard(player[1]);
        printCard(player[2]);
        printCard(lastCard);
    }

    /**
     * 按大小规则顺序打印卡牌
     * @param cards 要打印的卡牌
     */
    private static void printCard(List<String> cards) {
        int size = cards.size();
        for (int i = 0; i < size; i++) {
            System.out.print(" ___");
        }
        System.out.println();
        for (String item:cards){
            System.out.print("| "+item+" ");
        }
        System.out.println("|\n");
    }

    /**
     * 初始化玩家手牌
     * @param player
     * @param tmpCard
     * @param num 初始化手牌数
     */
    private static void givenCard(List<String> player, Map<String, Integer> tmpCard, int num) {
        Random random=new Random();
        for (int i = 0; i < num; i++) {
            Set<String> keySet = tmpCard.keySet();
            int n = random.nextInt(keySet.size());
            Object[] obj = keySet.toArray();
            String key= (String) obj[n];
            player.add(key);
            if (tmpCard.get(key)==1) tmpCard.remove(key);
            else tmpCard.put(key,tmpCard.get(key)-1);
        }
        sortCard((ArrayList<String>) player);   //对list进行排序
        if (tmpCard.size()<=3){   //给三张底牌赋值
            for (String key : tmpCard.keySet()) {
                for (int i = 0; i < tmpCard.get(key); i++) {
                    lastCard.add(key);
                }
            }
        }
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

    /*********************成员变量**********************/
    private static int rate=1;//记录倍率
    public static final int port=6666;
    public static GameStatus gameStatus;
    public static final String SYS_INFO="系统提示： ";
    public static StringBuffer reserveBuffer;
    private static ListenThread listenThread;
    private static HashMap<String,Integer> allCard;//54张扑克牌
    private static int lander=-1;  //地主
    private static List<String>[] player;    //三个玩家
    private static List<String> lastCard;//三张底牌
    private static String[] rule; //每张牌的大小规则
    private static StringBuffer[] sendBuffer;
    public static Map<StringBuffer,Boolean> sendBufferStatus;
    public static int turn;//当前出牌者
    public static final int thinkTime=15;
    private static final String SYN_STATUS = "dfd4fa1cs1f6a5s4e8w";//设置状态同步序列码
    private static final String SYN_TURN = "syn_turn";//设置顺序同步序列码
    private static final String SYN_SYSTURN = "syn_systurn";//设置系统出牌顺序同步序列码
    private static final String SYN_CARD = "card";//设置系统出牌顺序同步序列码
    private static final String SYN_LANDER = "lander";//设置系统出牌顺序同步序列码
    private static final String SYN_lastTurn = "last_turn";//设置最后一次出牌者序号同步序列码
    private static final String SYN_HISTORY ="历史记录：";  //记录出牌历史
    private static final String SYN_LEFTCARDNUM ="left";  //玩家剩余手牌同步信号
}