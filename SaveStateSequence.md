# Game Room GDX

```mermaid 
sequenceDiagram
    participant user
    participant PauseScreen
    participant GameManager
    user ->> PauseScreen: show()
    user ->> PauseScreen: save()
    PauseScreen ->> GameManager: save()
    participant FILE
    participant FileWriter
    GameManager ->> FILE: <<create>>
    GameManager ->> GameManager: getFEN()
    GameManager ->> FileWriter: write(fen)
    

```

The above sequence diagram details the process of creating a save state. The user hits
the save button in the puase manu and the game manager will write the current fen to a new file.


