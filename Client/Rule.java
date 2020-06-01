
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Rule {
    /**
     * 工具类不能实例化
     */
    private Rule() {
    }

    public static final String rule="3456789XJQKA2-+";//规则

    /**
     * 检查卡片是否正确
     * @param card  待检查卡片
     * @return  成功与否
     */
    public static Type isCardCorrect(String card) {
        String[] cards = card.split("");
        for (String s : cards)
            if (!rule.contains(s)) return Type.Error;//有非法字符
        switch (card.length()){
            case 0:return Type.Error;
            case 1:return Type.Single;
            case 2:return twoCardsInspect(card);
            case 3:return threeCardsInspect(card);
            case 4:return fourCardsInspect(cards);
            case 5:return fiveCardsInspect(card);
            default:return defaultInspection(card);
        }
    }


    /**
     * 超过5张牌，连牌、连队，飞机【带，不带】
     * @param card 待检查卡片
     * @return  是否成功
     */
    private static Type defaultInspection(String card) {
        if (isContinuousCard(card)) return Type.Continuous;//连牌

        int min,max,tmp;
        min=max=rule.indexOf(card.charAt(0));
        Map<Character,Integer> map=new HashMap<>();
        for (int i = 0; i < card.length(); i++) {
            if (map.containsKey(card.charAt(i)))
                map.put(card.charAt(i),map.get(card.charAt(i))+1);
            else map.put(card.charAt(i),1);
            tmp=rule.indexOf(card.charAt(i));
            if (tmp<min) min=tmp;
            if (tmp>max) max=tmp;
        }
        tmp=max-min+1;
        if (map.containsValue(4)) return Type.Error;//规定牌不能连炸

        if (isContinuousPairCard(card,map,tmp)) return Type.PairContinuous;//连队
        return isAirplaneCard(card,map,tmp);
    }

    /**
     * 判断是否为飞机
     * @param card  字符串卡片
     * @param map   集合形式卡片
     * @param span  最大最小跨距
     * @return  是否成功
     */
    private static Type isAirplaneCard(String card, Map<Character, Integer> map, int span) {
        //不带牌333444555
        if (card.length()==span*3) return Type.AirplaneByNull;
        int min,max,tmp;
        min=max=tmp=-1;
        Set<Character> set = map.keySet();
        for (Character c : set) {
            if (map.get(c)==3){
                if (tmp==-1) max=min=tmp=rule.indexOf(c);
                else {
                    tmp=rule.indexOf(c);
                    if (tmp<min) min=tmp;
                    else if (tmp>max) max=tmp;
                }
            }
        }
        //带一张单牌333444555678
        if (card.length()==(max-min+1)*4) return Type.AirplaneByOne;
        //带一对牌333444555667788
        return card.length() == (max - min + 1) * 5 && !map.containsValue(1)? Type.AirplaneByPair: Type.Error;
    }

    /**
     * 判断是否为连队
     * @param card  字符串卡片
     * @param map   集合形式卡片
     * @param span  最大最小跨距
     * @return  是否成功
     */
    private static boolean isContinuousPairCard(String card,Map<Character, Integer> map,int span) {
        if (card.length()%2!=0) return false;//不是偶数
        //这几张牌不能组成连牌
        if (card.contains("2")||card.contains("-")||card.contains("+"))
            return false;
        //长度等于数量的一半，且不包含3或4张相同的牌，则所有牌均为一对 如：33445566
        return card.length()/2==span && !map.containsValue(3);
    }

    /**
     * 对五张卡片进行检查
     * @param card  待检查卡片
     * @return  是否成功
     */
    private static Type fiveCardsInspect(String card) {
        if (isContinuousCard(card)) return Type.Continuous;//连牌
        Map<Character,Integer> map=new HashMap<>();
        for (int i = 0; i < card.length(); i++) {
            if (map.containsKey(card.charAt(i)))
                map.put(card.charAt(i),map.get(card.charAt(i))+1);
            else map.put(card.charAt(i),1);
        }
        //是否为三带一对
        return  map.size()==2&&map.containsValue(3)? Type.TripleByPair: Type.Error;
    }

    /**
     * 判断是否为顺子
     * @param card  待检查卡片
     * @return  是否成功
     */
    private static boolean isContinuousCard(String card) {
        int min,max,tmp;
        //这几张牌不能组成连牌
        if (card.contains("2")||card.contains("-")||card.contains("+"))
            return false;
        min=max=rule.indexOf(card.charAt(0));
        for (int i = 1; i < card.length(); i++) {
            tmp=rule.indexOf(card.charAt(i));
            if (tmp<min) min=tmp;
            if (tmp>max) max=tmp;
        }
        return card.length()==max-min+1;//最大值减最小值是否等于长度
    }

    /**
     * 对四张牌进行检查
     * @param card 待检查卡片
     * @return 是否成功
     */
    private static Type fourCardsInspect(String[] card) {
        //至少有两张牌相同
        if (card[0].equals(card[1])||card[2].equals(card[3])){
            //至少有三张牌相同,三带一或炸弹
            if(card[1].equals(card[2])||card[0].equals(card[3])){
                return card[0].equals(card[3])? Type.Bomb: Type.TripleByOne;
            }
        }
        return Type.Error;
    }

    /**
     * 对三张牌进行检查
     * @param card 待检查卡片
     * @return 是否成功
     */
    private static Type threeCardsInspect(String card) {
        //if (card.equals("+++")||card.equals("---")) return false;
        //是否为三张一样的牌
        return card.charAt(0)==card.charAt(1)&&card.charAt(0)==card.charAt(2)? Type.TripleByNull: Type.Error;
    }
    /**
     * 对两张牌进行检查
     * @param card 待检查卡片
     * @return 是否成功
     */
    private static Type twoCardsInspect(String card) {
        //if (card.equals("++")||card.equals("--")) return false;//这个会在别处进行判断
        //是否为大小王或对子
        if (card.equals("-+") || card.equals("+-")) return Type.Bomb;
        return card.charAt(0) == card.charAt(1)? Type.Pair: Type.Error;
    }

    public static boolean compare(String src, String des, Type type) {
        if (src.length()==des.length()){
            switch (type){
                case Single:return singleCompare(src,des);
                case Pair:return pairCompare(src,des);
                case TripleByNull:case TripleByOne:case TripleByPair:
                    return tripleCompare(src,des);
                case Bomb:return bombCompare(src,des);
                case Continuous:
                case PairContinuous:return continuousCompare(src,des);
                case AirplaneByNull:case AirplaneByOne:case AirplaneByPair:
                    return airplaneCompare(src,des);
                case Error:return false;
            }
        }
        else if (type== Type.Bomb){
            return bombCompare(src,des);
        }
        return false;
    }

    private static boolean airplaneCompare(String src, String des) {
        //找出关键字序列
        String s=findAirplaneKey(src);
        String d=findAirplaneKey(des);
        return continuousCompare(s,d);
    }

    private static String findAirplaneKey(String src) {
        StringBuilder s= new StringBuilder();
        HashMap<Character,Integer> map=new HashMap<>();
        for (int i = 0; i < src.length(); i++) {
            if (map.containsKey(src.charAt(i))){
                map.put(src.charAt(i),map.get(src.charAt(i))+1);
            }else map.put(src.charAt(i),1);
        }
        Set<Character> set = map.keySet();
        for (Character c : set) {
            if (map.get(c)==3) s.append(c);
        }
        return s.toString();
    }

    private static boolean continuousCompare(String src, String des) {
        for (int i = 0; i < rule.length(); i++) {
            if (src.contains(rule.substring(i,i+1))) return false;//小于等于
            if (des.contains(rule.substring(i,i+1))) return true;//大于
        }
        return false;
    }

    private static boolean bombCompare(String src, String des) {
        //src可能是炸弹，des可能不是炸弹
        if (src.length()==2) return true;//王炸
        if (des.equals("-+")||des.equals("+-")) return false;//des为王炸
        if (des.length()==4){
                //判断是否为炸弹
            if (des.charAt(0)==des.charAt(1) && des.charAt(0)==des.charAt(2) && des.charAt(0)==des.charAt(3)){
                return rule.indexOf(src.charAt(0))>rule.indexOf(des.charAt(0));
            }
        }
        return true;//des不是炸弹，则src大
    }

    private static boolean tripleCompare(String src, String des) {
        //三不带
        if (src.length()==3) return rule.indexOf(src.charAt(0))>rule.indexOf(des.charAt(0));
        char s,d;
        //三带单
        if (src.length()==4){
            //找出三个关键牌比较
            s=src.charAt(0)==src.charAt(1)?src.charAt(0):src.charAt(2);
            d=des.charAt(0)==des.charAt(1)?des.charAt(0):des.charAt(2);
            return rule.indexOf(s)>rule.indexOf(d);
        }
        //三带一对
        s=findTripleByPairKey(src);
        d=findTripleByPairKey(des);
        return rule.indexOf(s)>rule.indexOf(d);
    }

    private static char findTripleByPairKey(String src) {
        char s;
        char[] sr = src.toCharArray();
        if (sr[0]==sr[1]&&sr[0]==sr[2]) s=sr[0];
        else {
            if (sr[3]==sr[4]) s=sr[3];
            else s=sr[0]==sr[1] ? sr[0]:sr[2];
        }
        return s;
    }

    private static boolean pairCompare(String src, String des) {
        //比较第一张就行
        return rule.indexOf(src.charAt(0))>rule.indexOf(des.charAt(0));
    }

    private static boolean singleCompare(String src, String des) {
        return rule.indexOf(src)>rule.indexOf(des);
    }

    public enum Type{
        Error,     //错误
        Single,    //单张
        Pair,    //一对
        TripleByNull,   //三不带
        TripleByOne,    //三带一
        TripleByPair,   //三带一对
        Bomb,   //炸弹
        Continuous, //连牌
        PairContinuous, //连对
        AirplaneByNull,//飞机不带
        AirplaneByOne,//飞机带单
        AirplaneByPair,//飞机带一对
    }
}
