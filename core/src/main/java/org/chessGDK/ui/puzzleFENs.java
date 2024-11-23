package org.chessGDK.ui;

import java.util.Random;

//Store all fen strings here
//choosing randomly
public class puzzleFENs {
    private String[] fenArray;
    private Random random;

    public puzzleFENs(){
        fenArray = new String[]{
                //"r6k/pp2r2p/4Rp1Q/3p4/8/1N1P2R1/PqP2bPP/7K b - - 0 24\tf2g3 e6e7 b2b1 b3c1 b1c1 h6c1\n",
                //"5rk1/1p3ppp/pq3b2/8/8/1P1Q1N2/P4PPP/3R2K1 w - - 2 27\td3d6 f8d8 d6d8 f6d8\n",
                //"8/4R3/1p2P3/p4r2/P6p/1P3Pk1/4K3/8 w - - 1 64\te7f7 f5e5 e2f1 e5e6\n",
            "r2qr1k1/b1p2ppp/pp4n1/P1P1p3/4P1n1/B2P2Pb/3NBP1P/RN1QR1K1 b - - 1 16\tb6c5 e2g4 h3g4 d1g4\n"
        };
    }

    public String getRandomPuzzle(){
        random = new Random();
        int index = random.nextInt(fenArray.length);
        return fenArray[index];
    }

}
