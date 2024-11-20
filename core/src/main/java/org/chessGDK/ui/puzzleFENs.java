package org.chessGDK.ui;

import java.util.Random;

//Store all fen strings here
//choosing randomly
public class puzzleFENs {
    private String[] fenArray;
    private Random random;

    public puzzleFENs(){
        fenArray = new String[]{
                "r6k/pp2r2p/4Rp1Q/3p4/8/1N1P2R1/PqP2bPP/7K b - - 0 24",
                "5rk1/1p3ppp/pq3b2/8/8/1P1Q1N2/P4PPP/3R2K1 w - - 2 27"
        };
    }

    public String getRandomPuzzle(){
        random = new Random();
        int index = random.nextInt(fenArray.length);
        return fenArray[index];
    }

}
