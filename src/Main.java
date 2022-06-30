import java.util.Scanner;

public class Main {

    public static void main (String[] args) {

        Scanner s = new Scanner(System.in);
        System.out.print("Do you want to play with overflow enabled? (y/n): ");
        boolean overflow = s.nextLine().toUpperCase().charAt(0) == 'Y';

        System.out.println("\n");

        AI ai = new AI(overflow);
        ai.gameLoop();

    }

}