import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game {
    public static void main(String[] args) {
        List<Integer> rolls = new ArrayList<>();

        rolls = roll();
        System.out.println(rolls);
        System.out.println(rollScore(rolls));
    }

    static Random ran = new Random();

    public synchronized static List<Integer> roll() {
        List<Integer> rolls = new ArrayList<>();
        int i = 0;
        int roll;

        while (i < 3) {
            roll = ran.nextInt(6) + 1;
            rolls.add(roll);
            i++;
        }
        return rolls;
    }

    public synchronized static int rollScore(List<Integer> list) {
        Collections.sort(list);
        if (list.get(0) == list.get(1)) {
            return list.get(2);
        } else if (list.get(1) == list.get(2)) {
            return list.get(0);
        } else {
            return 0;
        }
    }
}
