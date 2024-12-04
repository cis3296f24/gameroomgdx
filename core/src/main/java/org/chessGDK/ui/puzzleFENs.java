package org.chessGDK.ui;

import java.util.Random;

//Store all fen strings here
//choosing randomly
/**
 * Stores all the fen strings used in puzzle mode
 */

public class puzzleFENs {
    /** Array of fen strings and move order solutions.*/
    private String[] fenArray;
    /** random variable for puzzle selection*/
    private Random random;
    /**
     * Constructor for puzzle fens sourced from Lichess which adds fens to fenArray.
     *
     */
    // puzzles sourced from Lichess
    public puzzleFENs(){
        fenArray = new String[]{
                "r6k/pp2r2p/4Rp1Q/3p4/8/1N1P2R1/PqP2bPP/7K b - - 0 24\tf2g3 e6e7 b2b1 b3c1 b1c1 h6c1\n",
                "5rk1/1p3ppp/pq3b2/8/8/1P1Q1N2/P4PPP/3R2K1 w - - 2 27\td3d6 f8d8 d6d8 f6d8\n",
                "2kr3r/pp3p2/4p2p/1N1p2p1/3Q4/1P1P4/2q2PPP/5RK1 b - - 1 20\tb7b6 d4a1 a7a5 f1c1\n",
                "4r1k1/5ppp/r1p5/p1n1RP2/8/2P2N1P/2P3P1/3R2K1 b - - 0 21\te8e5 d1d8 e5e8 d8e8\n",
                "3R4/8/K7/pB2b3/1p6/1P2k3/3p4/8 w - - 4 58\ta6a5 e5c7 a5b4 c7d8\n",
                "4r3/5pk1/1p3np1/3p3p/2qQ4/P4N1P/1P3RP1/7K w - - 6 34\td4b6 f6e4 h1g1 e4f2\n"
        };
    }
    /**
     * Randomly selects a fen and solution from fenArray
     *
     * @return random puzzle from fen array
     */
    public String getRandomPuzzle(){
        random = new Random();
        int index = random.nextInt(fenArray.length);
        return fenArray[index];
    }

}
