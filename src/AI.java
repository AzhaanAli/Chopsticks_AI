import java.util.Arrays;
import java.util.Scanner;

public class AI extends Game {

    /* This class contains all the methods used to start and play a game of chopsticks against an AI. */

    // ------------------------------------------- //
    // Instance variables.

    // Constants for coloring text.
    private final String RED = "\u001b[31m";
    private final String GREEN = "\u001b[32m";
    private final String RESET = "\u001b[0m";

    // Used for getting player choices from the user.
    private final Scanner SCANNER = new Scanner(System.in);

    // Determines the max amount of turns foreseen by the minimax method.
    // It seems 12 is the sweet-spot, as any higher value slows down computation and any less is unnecessarily quick.
    public int maxDepth = 14;


    // ------------------------------------------- //
    // Constructors.

    public AI () {

        super(false);

    }
    public AI (boolean overflowRules) {

        super(overflowRules);

    }


    // ------------------------------------------- //
    // Decision making methods.

    // Attempts to retrieve the move most likely to lead to victory for the current state of the game.
    private int[/*enemy hand, player hand*/] getBestMove () {

        int[/*max, maxHand, MaxToAttack*/] maximaArray = new int[]{
                Integer.MIN_VALUE, 0, 0
        };

        System.out.print("AI is thinking.");


        for(byte hand = 0; hand < 2; hand++)
        {
            byte fingers = super.hands[hand + 2];

            // Check attack possibilities.
            updateMaxima_AttackComponent(maximaArray, hand, fingers);

            // Check split possibilities.
            updateMaxima_SendComponent(maximaArray, hand, fingers);

        }
        System.out.println();

        return new int[]{
                maximaArray[1],
                maximaArray[2]
        };

    }

    // These two methods attempt to find the best attack and send moves.
    private void updateMaxima_AttackComponent (int[] maximaArray, int hand, byte fingers) {

        // For every hand that is available to attack, check the worth of each possible attack.
        for (byte toAttack = 0; toAttack < 2; toAttack++)
        {
            System.out.print(".");
            if (fingers != 0 && super.hands[toAttack] != 0) {

                // Alter the board to simulate the attack, grab the evaluation, then fix any alterations.
                byte last = super.hands[toAttack];
                super.attack(false, hand, toAttack);
                int evaluation = minimax(
                        false, maxDepth,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                );
                super.hands[toAttack] = last;

                // If the evaluation score is better than anything seen before, replace all variables.
                // If it is equivalent, give it a chance to appear as the best move for some variety.
                if (evaluation > maximaArray[0] || (evaluation == maximaArray[0] && Math.random() > .5))
                {
                    maximaArray[0] = evaluation;
                    maximaArray[1] = hand;
                    maximaArray[2] = toAttack;
                }
            }
        }

    }
    private void updateMaxima_SendComponent (int[] maximaArray, int hand, byte fingers) {

        // Offset hand by its index in the hands array.
        hand += 2;
        // Store the index of the hand that wasn't passed for convenience.
        int otherHand = hand == 2? 3 : 2;

        // For each hand, check the worth of each valid send move.
        for(byte toSplit = 1; toSplit <= fingers; toSplit++)
        {
            System.out.print(".");
            if(
                // This if statement checks whether a move is illegal or not.
                // An illegal move is one that stalls and just reflects a players fingers.
                // An example of this is having |||  || and sending a finger, so you are left with ||  |||.
                    !( super.hands[otherHand] == super.hands[hand] - toSplit
                    && super.hands[otherHand] + toSplit == super.hands[hand])
            )
            {

                // Alter the board to simulate the send action, grab the evaluation, then fix any alterations.
                byte lastSend = super.hands[hand];
                byte lastReceive = super.hands[otherHand];
                super.send(false, hand - 2, toSplit);
                int evaluation = minimax(
                        false, maxDepth,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                );
                super.hands[hand] = lastSend;
                super.hands[otherHand] = lastReceive;

                // If the evaluation score is better than anything seen before, replace all variables.
                // If it is equivalent, give it a chance to appear as the best move for some variety.
                if (evaluation > maximaArray[0] || (evaluation == maximaArray[0] && Math.random() > .5))
                {
                    maximaArray[0] = evaluation;
                    maximaArray[1] = hand - 2;
                    maximaArray[2] = -toSplit;
                }
            }
        }



    }


    // ------------------------------------------- //
    // Lower level decision making methods.

    private int minimax(boolean aiTurn, int countDown, int alpha, int beta) {

        // If the game ends in a terminal-state OR max recursive depth is reached, evaluate the board.
        if (super.gameOver() || countDown == 0) return this.evaluateBoard(!aiTurn, countDown);

        // Start minMax at a minimum or maximum value depending on whether it is the minimizing or maximizing turn.
        int minMax = aiTurn? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;
        // Loop though every available column and update minMax accordingly to new evaluations.
        for(int hand = 0; hand < 2; hand++)
        {
            int otherHand = hand == 0? 1 : 0;
            if(aiTurn) otherHand += 2;

            byte fingers = super.hands[hand + (aiTurn? 2 : 0)];
            if (fingers != 0) {
                // Check attack possibilities.
                for (int toAttack = 0; toAttack < 2; toAttack++) {
                    if (super.hands[toAttack + (aiTurn ? 0 : 2)] != 0) {

                        byte last = super.hands[toAttack + (aiTurn ? 0 : 2)];
                        super.attack(!aiTurn, hand, toAttack);
                        int loss = minimax(
                                !aiTurn,
                                countDown - 1,
                                alpha, beta
                        );
                        super.hands[toAttack + (aiTurn ? 0 : 2)] = last;

                        // Update minMax depending on whether it's the minimizing or maximizing turn.
                        minMax = aiTurn?
                                Math.max(minMax, loss) :
                                Math.min(minMax, loss) ;

                        // Alpha-Beta pruning.
                        // If we know there's already a better option somewhere in the tree, there
                        // is no reason to take this one. This lets us avoid many unnecessary calculations.
                        if (aiTurn) alpha = Math.max(alpha, loss);
                        else beta = Math.min(beta, loss);
                        if (beta <= alpha) break;
                    }
                }

                // Check split possibilities.
                for(int toSplit = 1; toSplit <= fingers; toSplit++)
                    if(
                            !( super.hands[hand + (aiTurn ? 2 : 0)] - toSplit == super.hands[otherHand]
                            && super.hands[otherHand] + toSplit == super.hands[hand + (aiTurn ? 2 : 0)])
                    )
                    {
                        byte[] prevValue = Arrays.copyOf(hands, 4);

                        super.send(!aiTurn, hand, toSplit);
                        int loss = minimax(
                                !aiTurn,
                                countDown - 1,
                                alpha, beta
                        );
                        System.arraycopy(prevValue, 0, hands, 0, 4);

                        // Update minMax depending on whether it's the minimizing or maximizing turn.
                        minMax = aiTurn?
                                Math.max(minMax, loss):
                                Math.min(minMax, loss);

                        // Alpha-Beta pruning.
                        // If we know there's already a better option somewhere in the tree, there is no reason to take this one.
                        // This lets us avoid many unnecessary calculations.
                        if (aiTurn) alpha = Math.max(alpha, loss);
                        else beta = Math.min(beta, loss);
                        if (beta <= alpha) break;
                    }
            }
        }

        return minMax;

    }

    // Determines the AI's priorities.
    // This method determines what futures the AI deems worth striving for, and altering this
    // has the single greatest effect on the AI's game strategy and behavior as a whole.
    private int evaluateBoard (boolean aiTurn, int recursiveDepth) {

        int score = maxDepth - recursiveDepth;
        if (aiTurn) score *= -1;

        if (hands[0] == 0 && hands[1] == 0) score += 100;
        if (hands[2] == 0 && hands[3] == 0) score -= 100;

        if (hands[0] == 0 || hands[1] == 0) score += 20;
        if (hands[2] == 0 || hands[3] == 0) score -= 20;

        if (hands[0] == 3 || hands[1] == 3) score -= 10;
        if (hands[2] == 3 || hands[3] == 3) score += 10;

        return score;


    }


    // ------------------------------------------- //
    // Game-loop methods.

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

    private int prompt1or2 () {

        int n = this.promptInt();
        while(n < 1 || n > 2)
        {
            n = this.promptInt();
            System.out.println("Number must be either 1 or 2.");
        }
        return n;

    }
    private int promptInt () {

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
