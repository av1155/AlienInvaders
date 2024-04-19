# AlienInvaders

AlienInvaders is a fast-paced arcade-style game developed in Java, inspired by the classic Space Invaders space shooting game. The game features a variety of alien enemies, a player-controlled spaceship, and challenging gameplay mechanics, including UFOs, multiple alien types, and dynamic scoring.

## Getting Started

### Prerequisites

-   Java SE Development Kit (JDK) - Version 8 or above.
-   Any Java IDE like IntelliJ IDEA, Eclipse, or NetBeans.

### Installation

1. Install the JAR file from the releases section, and run it!

    OR

1. Clone the repository or download the source code:

    ```
    git clone https://github.com/yourusername/AlienInvaders.git

    ```

1. Open the project in your Java IDE.
1. Build and run the `GamePanel.java` file to start the game.

## Features

-   Multiple types of aliens with different behaviors and scoring values.
-   Player-controlled spaceship capable of moving left, right, and shooting.
-   Explosions that graphically represent the destruction of aliens and the player's ship.
-   A UFO that randomly crosses the screen, providing bonus opportunities when hit.
-   High score tracking with local persistence.
-   Replay functionality to restart the game after a game over.

## Controls

-   **Left Arrow Key/A**: Move the spaceship left.
-   **Right Arrow Key/D**: Move the spaceship right.
-   **Space Bar**: Shoot bullets from the spaceship.

## How to Play

-   Destroy all aliens before they can reach the bottom of the screen.
-   Avoid or shoot down bullets from alien ships.
-   Score points by destroying aliens and hitting the UFO.
-   Keep playing to try and beat your high score!

## Scoring

-   Small Alien: 30 points
-   Medium Alien: 20 points
-   Large Alien: 10 points
-   UFO: Randomly between 50 and 300 points

## Development

This game is developed using Java Swing for the GUI components and graphics handling. The main gameplay mechanics are handled in the `GamePanel` class, with helper classes for managing game state, alien movements, and utility functions.

## Contributions

Contributions are welcome. Please fork the project and submit a pull request with your enhancements.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

-   Special thanks to the creators of classic arcade games for the inspiration.
-   Thanks to [javazoom](http://www.javazoom.net/) for providing the MP3 support used in the game's music playback.
