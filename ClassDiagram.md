# Game Room GDX

```mermaid 
classDiagram
    class StockFishAI{
        - Process stockfishProcess
        - BufferedReader inputReader
        - OutputStream outputStream
        - int depth
        + StockfishAI(int depth)
        + close() void
        + getBestMove(String fen) String
    }


```