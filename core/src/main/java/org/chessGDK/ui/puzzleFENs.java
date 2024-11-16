package org.chessGDK.ui;

import java.util.Random;

//Store all fen strings here
//choosing randomly
public class puzzleFENs {
    private String[] fenArray;
    private Random random;

    public puzzleFENs(){
        fenArray = new String[]{
                "b1B3Q1/5K2/5NP1/n7/2p2k1P/3pN2R/1B1P4/4qn2",
                "6Q1/1Nn5/2p1rp2/2p5/2r1k2P/2PN3K/4PP2/1B2R3",
                "1n3NR1/2B1R3/2pK1p2/2N2p2/5kbp/1r1p4/3ppr2/4b1QB"
        };

        random = new Random();
    }

    public String getRandomPuzzle(){
        int index = random.nextInt(fenArray.length);
        return fenArray[index];
    }

}
