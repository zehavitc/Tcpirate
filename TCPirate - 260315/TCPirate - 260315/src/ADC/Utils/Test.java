package ADC.Utils;

/**
 * Created by Ludaa on 3/10/14.
 */
public class Test {

    public static void main(String[] args) {
        String text = "Lorem ipsum dolor sit amet";
        String pattern = "sss";
        BoyerMoore bm = new BoyerMoore(pattern);

        int first_occur_position = bm.search(text);
        System.out.println("The text '" + pattern + "' is first found after the "
                + first_occur_position + " position.");
    }
}