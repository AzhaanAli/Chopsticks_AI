import java.util.Arrays;
import java.util.Scanner;

public class AI extends Game {

    private final String RED = "\u001b[31m";
    private final String GREEN = "\u001b[32m";
    private final String RESET = "\u001b[0m";

    private final Scanner SCANNER = new Scanner(System.in);


    public int maxDepth = 12;

    public AI () {

        super(false);

    }
    public AI (boolean overflowRules) {

        super(overflowRules);

    }

    public int[/*enemy hand, player hand*/] getBestMove () {

        // Special case:
        // If you only have one hand, do not attack one with 3 unless you have to.
        if(super.hands[2] + super.hands[3] == 1)
        {
            if(super.hands[0] == 3 && super.hands[1] != 3 && super.hands[1] != 0)
                return new int[]{super.hands[2] == 1? 0 : 1, 1};
            if(super.hands[1] == 3 && super.hands[0] != 3 && super.hands[0] != 0)
                return new int[]{super.hands[2] == 1? 0 : 1, 0};
        }

        int max = Integer.MIN_VALUE;
        int maxHand = 0;
        int maxToAttack = 0;

//        System.out.println("AI is thinking.");

        for(int hand = 0; hand < 2; hand++)
        {
            byte fingers = super.hands[hand + 2];

            // Check attack possibilities.
            for (int toAttack = 0; toAttack < 2; toAttack++)
            {
//                System.out.print(".");
                if (fingers != 0 && super.hands[toAttack] != 0) {

                    byte[] prevValue = Arrays.copyOf(hands, 4);

                    // Place a coin at the current column and get that boards evaluation.
                    // Un-place that coin after evaluating the board so not to damage the normal playing board.
                    super.attack(false, hand, toAttack);
                    int evaluation = minimax(
                            false, maxDepth,
                            Integer.MIN_VALUE,
                            Integer.MAX_VALUE
                    );
                    System.arraycopy(prevValue, 0, hands, 0, 4);

                    System.out.println("ai's " + (hand == 0? "first" : "second") + " to attack your " + (toAttack == 0? "first" : "second") + " hand: " + evaluation);


                    if (evaluation > max || (evaluation == max && Math.random() > .5))
                    {
                        max = evaluation;
                        maxHand = hand;
                        maxToAttack = toAttack;
                    }
                }
            }

            // Check split possibilities.
            for(int toSplit = 1; toSplit <= fingers; toSplit++)
                if(
                        !(super.hands[hand + 2] - toSplit == super.hands[hand == 0? 3 : 2]
                        && super.hands[hand == 0? 3 : 2] + toSplit == super.hands[hand + 2])
                )
                {
//                    System.out.print(".");

                    byte[] prevValue = Arrays.copyOf(hands, 4);

                    // Place a coin at the current column and get that boards evaluation.
                    // Un-place that coin after evaluating the board so not to damage the normal playing board.
                    super.send(false, hand, toSplit);
                    int evaluation = minimax(
                            false, maxDepth,
                            Integer.MIN_VALUE,
                            Integer.MAX_VALUE
                    );
                    System.arraycopy(prevValue, 0, hands, 0, 4);

                    System.out.println("ai's " + (hand == 0? "first" : "second") + " to send " + toSplit + " fingers to the other: " + evaluation);


                    if (evaluation > max || (evaluation == max && Math.random() > .5))
                    {
                        max = evaluation;
                        maxHand = hand;
                        maxToAttack = -toSplit;
                    }
                }
        }
        System.out.println();

        return new int[]{maxHand, maxToAttack};

    }

    public int minimax (boolean aiTurn, int countDown, int alpha, int beta) {

        // If the game ends in a terminal-state OR max recursive depth is reached, evaluate the board.
        if (super.gameOver() || countDown == 0) return this.evaluateBoard(!aiTurn, countDown);

        // Start minMax at a minimum or maximum value depending on whether it is the minimizing or maximizing turn.
        int minMax = aiTurn? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        // Loop though every available column and update minMax accordingly to new evaluations.
        for(int hand = 0; hand < 2; hand++)
        {
            byte fingers = super.hands[hand + (aiTurn? 2 : 0)];
            if (fingers != 0) {
                // Check attack possibilities.
                for (int toAttack = 0; toAttack < 2; toAttack++) {
                    if (super.hands[toAttack + (aiTurn ? 0 : 2)] != 0) {
                        byte[] prevValue = Arrays.copyOf(hands, 4);

                        // Place a coin at the current column and get that boards evaluation.
                        // Un-place that coin after evaluating the board so not to damage the normal playing board.
                        super.attack(!aiTurn, hand, toAttack);
                        int evaluation = minimax(
                                !aiTurn,
                                countDown - 1,
                                alpha, beta
                        );
                        System.arraycopy(prevValue, 0, hands, 0, 4);

                        // Update minMax depending on whether it's the minimizing or maximizing turn.
                        minMax = aiTurn ?
                                Math.max(minMax, evaluation) :
                                Math.min(minMax, evaluation);

                        // Alpha-Beta pruning.
                        // If we know there's already a better option somewhere in the tree, there is no reason to take this one.
                        // This lets us avoid many unnecessary calculations.
                        if (aiTurn) alpha = Math.max(alpha, evaluation);
                        else beta = Math.min(beta, evaluation);
                        if (beta <= alpha) break;
                    }
                }

                // Check split possibilities.
                for(int toSplit = 1; toSplit <= fingers; toSplit++)
                    if(
                            !(super.hands[hand + 2] - toSplit == super.hands[hand == 0? 3 : 2]
                            && super.hands[hand == 0? 3 : 2] + toSplit == super.hands[hand + 2])
                    )
                    {
                        byte[] prevValue = Arrays.copyOf(hands, 4);

                        // Place a coin at the current column and get that boards evaluation.
                        // Un-place that coin after evaluating the board so not to damage the normal playing board.
                        super.send(!aiTurn, hand, toSplit);
                        int evaluation = minimax(
                                !aiTurn,
                                countDown - 1,
                                alpha, beta
                        );
                        System.arraycopy(prevValue, 0, hands, 0, 4);

                        // Update minMax depending on whether it's the minimizing or maximizing turn.
                        minMax = aiTurn ?
                                Math.max(minMax, evaluation) :
                                Math.min(minMax, evaluation);

                        // Alpha-Beta pruning.
                        // If we know there's already a better option somewhere in the tree, there is no reason to take this one.
                        // This lets us avoid many unnecessary calculations.
                        if (aiTurn) alpha = Math.max(alpha, evaluation);
                        else beta = Math.min(beta, evaluation);
                        if (beta <= alpha) break;
                    }
            }
        }

        return minMax;

    }

    public int evaluateBoard(boolean aiTurn, int recursiveDepth) {

        int score = -recursiveDepth;
        if (aiTurn)
        {
            if (hands[0] == 0 && hands[1] == 0) score += 100;
            if (hands[2] == 0 && hands[3] == 0) score -= 100;

            if (hands[0] == 0 || hands[1] == 0) score += 20;
            if (hands[2] == 0 || hands[3] == 0) score -= 20;

            if (hands[0] == 3 || hands[1] == 3) score -= 10;
            if (hands[2] == 3 || hands[3] == 3) score += 10;


//            score +=  10 * (hands[0] + hands[1]);
//            score += -10 * (hands[2] + hands[3]);
        }
        else
        {
            if (hands[0] == 0 && hands[1] == 0) score -= 100;
            if (hands[2] == 0 && hands[3] == 0) score += 100;

            if (hands[0] == 0 || hands[1] == 0) score -= 20;
            if (hands[2] == 0 || hands[3] == 0) score += 20;

            if (hands[0] == 3 || hands[1] == 3) score += 10;
            if (hands[2] == 3 || hands[3] == 3) score -= 10;

//            score += -10 * (hands[0] + hands[1]);
//            score +=  10 * (hands[2] + hands[3]);
        }
        return aiTurn? score : -score;


    }

    public void gameLoop () {

        boolean playerTurn = Math.random() > .5;

        while(!gameOver())
        {
            print();

            if(playerTurn)
            {
                // Ask the user to select a valid hand.
                System.out.print("Select one of your hands (1 or 2): ");
                int hand = this.prompt1or2() - 1;
                int fingers = super.hands[hand];
                // If the hand is not valid, then keep asking the user.
                while(fingers == 0)
                {
                    System.out.print("You have to select a hand with fingers on it, idiot: ");
                    hand = this.prompt1or2() - 1;
                    fingers = super.hands[hand];
                }

                System.out.print("Attack or send? (a or s): ");
                char choice = SCANNER.nextLine().toUpperCase().charAt(0);
                while(choice == 'S' && super.hands[0] + super.hands[1] == 1)
                {
                    System.out.println("Sending 1 finger to an empty hand is an illegal move.");
                    System.out.print("Attack or send? (a or s): ");
                    choice = SCANNER.nextLine().toUpperCase().charAt(0);
                }

                if(choice != 'S')
                {

                    System.out.print("Which enemy hand do you want to attack? (1 or 2): ");
                    int enemyHand = this.prompt1or2() - 1;
                    int enemyFingers = super.hands[enemyHand + 2];
                    // If the hand is not valid, then keep asking the user.
                    while(enemyFingers == 0)
                    {
                        System.out.print("You have to select an enemy hand with fingers on it, idiot: ");
                        enemyHand = this.prompt1or2() - 1;
                        enemyFingers = super.hands[enemyHand + 2];
                    }
                    attack(true, hand, enemyHand);
                }
                else
                {
                    System.out.print("How many fingers do you want to send to your other hand?: ");
                    int toSend = this.promptInt();
                    while(
                            toSend <= 0 || toSend > fingers ||
                            (super.hands[hand] - toSend == super.hands[hand == 0? 1 : 0]
                            && super.hands[hand == 0? 1 : 0] + toSend == super.hands[hand])
                    )
                    {
                        if (toSend <= 0 || toSend > fingers)
                            System.out.print("You cannot send that many fingers, idiot: ");
                        else
                            System.out.print("Sending this amount will result in the same game, and is an illegal move: ");

                        toSend = this.promptInt();
                    }
                    send(true, hand, toSend);
                }

            }
            else
            {
                System.out.print(this.GREEN);
                int[] move = getBestMove();
                if(move[1] < 0)
                {
                    int hand = move[0];
                    int toSplit = -move[1];
                    send(false, hand, toSplit);
                    System.out.println(
                            "AI sent " + toSplit + " fingers from its " +
                            (hand == 0? "first" : "second") + " hand to its " +
                            (hand == 0? "second" : "first")
                    );
                }
                else
                {
                    int hand = move[0];
                    int toAttack = move[1];
                    attack(false, hand, toAttack);
                    System.out.println(
                            "AI attacked your " + (toAttack == 0? "first" : "second") +
                            " hand with its " + (hand == 0? "first" : "second")
                    );
                }
                System.out.print(this.RESET);
            }

            System.out.println("\n");

            playerTurn = !playerTurn;
        }

        print();

        System.out.println("You " + (playerTurn? "lost" : "won") + "!");

    }

    private int prompt1or2(){

        int n = this.promptInt();
        while(n < 1 || n > 2)
        {
            n = this.promptInt();
            System.out.println("Number must be either 1 or 2.");
        }
        return n;

    }
    private int promptInt(){

        int n = -1;
        while(n <= 0)
        {
            try {
                n = Integer.parseInt(this.SCANNER.nextLine());
            }catch(Exception e){
                System.out.println("Enter an integer.");
            }
        }
        return n;

    }


}
