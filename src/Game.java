public class Game {

    /* This class contains all the methods used to score and make moves for a game of chopsticks. */

    // ------------------------------------------- //
    // Instance variables.

    // Determines the rule-set of the game.
    // If overflow is off, hands will die as soon as their fingers exceed 4.
    // If overflow is on, hands will die once their fingers equal exactly 5, while any additional damage wraps around.
    private boolean overflowRules;

    // This array encodes the information of all players hands and their respective finger counts.
    // I decided to use a byte array rather than an integer array because no hand will ever have more than 4 fingers.
    // Each hand starts with 1 finger.
    public byte[] hands = new byte[] {
            // Player 1
            // [0] --> player 1 left
            // [1] --> player 1 right
            1, 1,

            // Player 2
            // [2] --> player 2 left
            // [3] --> player 2 right
            1, 1
    };


    // ------------------------------------------- //
    // Constructor.

    public Game (boolean overflowRules) {

        this.overflowRules = overflowRules;

    }


    // ------------------------------------------- //
    // Methods.

    // Returns whether a game has been won or not.
    public boolean gameOver(){

        return (hands[0] == 0 && hands[1] == 0) || (hands[2] == 0 && hands[3] == 0);

    }

    // Updates the hands array to simulate an attack.
    public void attack(boolean playerTurn, int hand, int toAttack){

        // Add 2 to the index of the AI's hand, as it is offset by 2 in the hands array.
        if(playerTurn) toAttack += 2;
        else hand += 2;

        // Store total fingers to manipulate accordingly to game rules.
        int totalFingers = hands[toAttack] + hands[hand];

        // Assign the new amount of fingers to the hand receiving the damage.
        hands[toAttack] = this.limitToGameRule(totalFingers);

    }

    // Updates the hands array to simulate a split or send.
    public void send(boolean playerTurn, int hand, int amountToSend){

        // If the sending hand is the first hand, the receiving hand must be the second and vice versa.
        int sendingTo = hand == 0? 1 : 0;

        // If the AI is splitting or sending, add 2 to both hand variables to match the offset of the hands array..
        if(!playerTurn)
        {
            hand += 2;
            sendingTo += 2;
        }

        // Remove the amount sent from the sending hand and give it to the receiving hand.
        hands[hand] -= amountToSend;
        hands[sendingTo] += amountToSend;

        // In case the receiving hand has accumulated 5 or more fingers from the action, limit as per game-rule.
        hands[sendingTo] = this.limitToGameRule(hands[sendingTo]);

    }

    // Processes numbers greater than or equal to 5.
    // If overflow is enabled, numbers will wrap around if they do not equal exactly 5.
    // If overflow is not enabled, then hands will be eliminated as soon as they have 5 or more fingers.
    private byte limitToGameRule (int fingers) {

        // If the amount of fingers is less than 5, there is no reason to do anything.
        if (fingers >= 5)
            // If we are playing overflow, taking the modulus of the total fingers will simulate wrapping behavior
            if (overflowRules) fingers %= 5;
            // If we are not playing overflow, simply set fingers to 0 if they exceed 4.
            else fingers = 0;
        return (byte) fingers;

    }

    // Prints the game in a way the user can work with.
    public void print(){

        System.out.println(
                "P2:\t" + "|".repeat(hands[2]) + (hands[2] == 4? "  " : "\t  ") + "|".repeat(hands[3]) + "\n" +
                "P1:\t" + "|".repeat(hands[0]) + (hands[0] == 4? "  " : "\t  ") + "|".repeat(hands[1])
        );

    }

}
