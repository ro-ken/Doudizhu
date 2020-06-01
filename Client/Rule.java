
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Rule {
    /**
     * �����಻��ʵ����
     */
    private Rule() {
    }

    public static final String rule="3456789XJQKA2-+";//����

    /**
     * ��鿨Ƭ�Ƿ���ȷ
     * @param card  ����鿨Ƭ
     * @return  �ɹ����
     */
    public static Type isCardCorrect(String card) {
        String[] cards = card.split("");
        for (String s : cards)
            if (!rule.contains(s)) return Type.Error;//�зǷ��ַ�
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
     * ����5���ƣ����ơ����ӣ��ɻ�������������
     * @param card ����鿨Ƭ
     * @return  �Ƿ�ɹ�
     */
    private static Type defaultInspection(String card) {
        if (isContinuousCard(card)) return Type.Continuous;//����

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
        if (map.containsValue(4)) return Type.Error;//�涨�Ʋ�����ը

        if (isContinuousPairCard(card,map,tmp)) return Type.PairContinuous;//����
        return isAirplaneCard(card,map,tmp);
    }

    /**
     * �ж��Ƿ�Ϊ�ɻ�
     * @param card  �ַ�����Ƭ
     * @param map   ������ʽ��Ƭ
     * @param span  �����С���
     * @return  �Ƿ�ɹ�
     */
    private static Type isAirplaneCard(String card, Map<Character, Integer> map, int span) {
        //������333444555
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
        //��һ�ŵ���333444555678
        if (card.length()==(max-min+1)*4) return Type.AirplaneByOne;
        //��һ����333444555667788
        return card.length() == (max - min + 1) * 5 && !map.containsValue(1)? Type.AirplaneByPair: Type.Error;
    }

    /**
     * �ж��Ƿ�Ϊ����
     * @param card  �ַ�����Ƭ
     * @param map   ������ʽ��Ƭ
     * @param span  �����С���
     * @return  �Ƿ�ɹ�
     */
    private static boolean isContinuousPairCard(String card,Map<Character, Integer> map,int span) {
        if (card.length()%2!=0) return false;//����ż��
        //�⼸���Ʋ����������
        if (card.contains("2")||card.contains("-")||card.contains("+"))
            return false;
        //���ȵ���������һ�룬�Ҳ�����3��4����ͬ���ƣ��������ƾ�Ϊһ�� �磺33445566
        return card.length()/2==span && !map.containsValue(3);
    }

    /**
     * �����ſ�Ƭ���м��
     * @param card  ����鿨Ƭ
     * @return  �Ƿ�ɹ�
     */
    private static Type fiveCardsInspect(String card) {
        if (isContinuousCard(card)) return Type.Continuous;//����
        Map<Character,Integer> map=new HashMap<>();
        for (int i = 0; i < card.length(); i++) {
            if (map.containsKey(card.charAt(i)))
                map.put(card.charAt(i),map.get(card.charAt(i))+1);
            else map.put(card.charAt(i),1);
        }
        //�Ƿ�Ϊ����һ��
        return  map.size()==2&&map.containsValue(3)? Type.TripleByPair: Type.Error;
    }

    /**
     * �ж��Ƿ�Ϊ˳��
     * @param card  ����鿨Ƭ
     * @return  �Ƿ�ɹ�
     */
    private static boolean isContinuousCard(String card) {
        int min,max,tmp;
        //�⼸���Ʋ����������
        if (card.contains("2")||card.contains("-")||card.contains("+"))
            return false;
        min=max=rule.indexOf(card.charAt(0));
        for (int i = 1; i < card.length(); i++) {
            tmp=rule.indexOf(card.charAt(i));
            if (tmp<min) min=tmp;
            if (tmp>max) max=tmp;
        }
        return card.length()==max-min+1;//���ֵ����Сֵ�Ƿ���ڳ���
    }

    /**
     * �������ƽ��м��
     * @param card ����鿨Ƭ
     * @return �Ƿ�ɹ�
     */
    private static Type fourCardsInspect(String[] card) {
        //��������������ͬ
        if (card[0].equals(card[1])||card[2].equals(card[3])){
            //��������������ͬ,����һ��ը��
            if(card[1].equals(card[2])||card[0].equals(card[3])){
                return card[0].equals(card[3])? Type.Bomb: Type.TripleByOne;
            }
        }
        return Type.Error;
    }

    /**
     * �������ƽ��м��
     * @param card ����鿨Ƭ
     * @return �Ƿ�ɹ�
     */
    private static Type threeCardsInspect(String card) {
        //if (card.equals("+++")||card.equals("---")) return false;
        //�Ƿ�Ϊ����һ������
        return card.charAt(0)==card.charAt(1)&&card.charAt(0)==card.charAt(2)? Type.TripleByNull: Type.Error;
    }
    /**
     * �������ƽ��м��
     * @param card ����鿨Ƭ
     * @return �Ƿ�ɹ�
     */
    private static Type twoCardsInspect(String card) {
        //if (card.equals("++")||card.equals("--")) return false;//������ڱ𴦽����ж�
        //�Ƿ�Ϊ��С�������
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
        //�ҳ��ؼ�������
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
            if (src.contains(rule.substring(i,i+1))) return false;//С�ڵ���
            if (des.contains(rule.substring(i,i+1))) return true;//����
        }
        return false;
    }

    private static boolean bombCompare(String src, String des) {
        //src������ը����des���ܲ���ը��
        if (src.length()==2) return true;//��ը
        if (des.equals("-+")||des.equals("+-")) return false;//desΪ��ը
        if (des.length()==4){
                //�ж��Ƿ�Ϊը��
            if (des.charAt(0)==des.charAt(1) && des.charAt(0)==des.charAt(2) && des.charAt(0)==des.charAt(3)){
                return rule.indexOf(src.charAt(0))>rule.indexOf(des.charAt(0));
            }
        }
        return true;//des����ը������src��
    }

    private static boolean tripleCompare(String src, String des) {
        //������
        if (src.length()==3) return rule.indexOf(src.charAt(0))>rule.indexOf(des.charAt(0));
        char s,d;
        //������
        if (src.length()==4){
            //�ҳ������ؼ��ƱȽ�
            s=src.charAt(0)==src.charAt(1)?src.charAt(0):src.charAt(2);
            d=des.charAt(0)==des.charAt(1)?des.charAt(0):des.charAt(2);
            return rule.indexOf(s)>rule.indexOf(d);
        }
        //����һ��
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
        //�Ƚϵ�һ�ž���
        return rule.indexOf(src.charAt(0))>rule.indexOf(des.charAt(0));
    }

    private static boolean singleCompare(String src, String des) {
        return rule.indexOf(src)>rule.indexOf(des);
    }

    public enum Type{
        Error,     //����
        Single,    //����
        Pair,    //һ��
        TripleByNull,   //������
        TripleByOne,    //����һ
        TripleByPair,   //����һ��
        Bomb,   //ը��
        Continuous, //����
        PairContinuous, //����
        AirplaneByNull,//�ɻ�����
        AirplaneByOne,//�ɻ�����
        AirplaneByPair,//�ɻ���һ��
    }
}
