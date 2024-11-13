# Game Room GDX

```mermaid 
classDiagram
    class StockFishAI{
        - STOCKFISHPROCESS: Process
        - INPUTREADER: BufferedReader
        - OUTPUTSTREAM: OutputStream
        - DEPTH: int
        + StockfishAI(depth: int)
        + close() void
        + getBestMove(fen: String) String
    }
    
    class GameManager{
       - TURNLOCK : Object 
       - whiteTurn: boolean 
       - BOARD: Piece[][] 
       - POSSIBILITIES: Blank[][]
       - CASTLINGPIECES: Piece[] 
       - STOCKFISHAI: StockfishAI 
       - DEPTH : int
       - halfMoves : int
       - castlingRights : String
       - enPassantSquare: String
       + GameManager()
       - setupPieces()
       + movePiece(move: String) boolean
       + parseMove(bestMove: String) char[]$
       + promote(rank: char, endRow: int, endCol: int) boolean
       + generateFen() String
       + makeNextMove() void
       + playerTakeTurn() boolean
       + aiTakeTurn() boolean
       + aiTurn() boolean
       + getBestMove(fen: String) String
       + getAI() StockfishAI
       + isWhiteTurn() boolean
       + getBoard() Piece[][]
       + getPossibilities() Blank[][]
       + printBoard() void
       + render(delta: float) void
       + exitGame() void
   
   }
    GameManager  o--  StockFishAI
    
    class ScreenManager{
        - gm: GameManager
        - chessBoardScreen: ChessBoardScreen
        
        + playChess() void
        + render() void
        + dispose() void
    }
    
    class Game{
        
    }
    
    Game <-- ScreenManager: Extends
    
    class PieceAnimation{
        - ANIMATION_DURATION: float$
        + piece: Piece
        + startPosition: Vector2
        + targetPosition: Vector2
        + elapsedTime: float
        
        + PieceAnimation(piece: Piece, startPosition: Vector2 , targetPosition: Vector2)
        + isDone: Boolean
        + update(delta: float)
    }

```