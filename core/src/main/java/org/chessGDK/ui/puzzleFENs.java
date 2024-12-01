package org.chessGDK.ui;

import java.util.Random;

//Store all fen strings here
//choosing randomly
public class puzzleFENs {
    private String[] fenArray;
    private Random random;

    // puzzles sourced from Lichess
    public puzzleFENs(){
        fenArray = new String[]{
                "5rk1/1p3ppp/pq3b2/8/8/1P1Q1N2/P4PPP/3R2K1 w - - 2 27",
                "8/4R3/1p2P3/p4r2/P6p/1P3Pk1/4K3/8 w - - 1 64",
                "4r3/1k6/pp3r2/1b2P2p/3R1p2/P1R2P2/1P4PP/6K1 w - - 0 35",
                "5r1k/5rp1/p7/1b2B2p/1P1P1Pq1/2R1Q3/P3p1P1/2R3K1 w - - 0 41",
                "br4k1/p1p2pp1/4p3/4P1p1/qr2P1P1/1PQB1R1P/2P5/1K1R4 w - - 1 26"

        };
    }

    public String getRandomPuzzle(){
        random = new Random();
        int index = random.nextInt(fenArray.length);
        return fenArray[index];
    }

}
