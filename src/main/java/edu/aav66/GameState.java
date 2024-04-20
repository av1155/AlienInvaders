package edu.aav66;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.Timer;

/**
 * This class represents the game state of the game. It contains methods for starting the game, initializing timers,
 * handling game over and game won states, rendering UI elements, and managing the replay button.
 */
class GameState
{
    static final int DELAY = 16;
    static int ALIEN_MOVEMENT_DELAY = 80;
    static int ALIEN_MOVEMENT_RESET_DELAY = 80;
    static int ALIEN_MOVEMENT_GAMEWON_DELAY = ALIEN_MOVEMENT_RESET_DELAY - ( GamePanel.difficultyMultiplier * 20 );

    // Font Constants
    private static final Font UI_FONT = new Font( "Futura", Font.PLAIN, 20 );    // Font for UI text
    private static final Font LARGE_FONT = new Font( "Futura", Font.BOLD, 75 );  // Font for large text
    private static final Font MEDIUM_FONT = new Font( "Futura", Font.BOLD, 40 ); // Font for medium text

    // Processes
    static Timer timer;
    static Timer alienTimer;
    static Timer ufoTimer;
    static JButton replayButton;

    /**
     * Starts the game by enabling movements, setting visibility, and starting the
     * timer.
     */
    static void startGame( GamePanel panel )
    {
        replayButton.setEnabled( false );
        replayButton.setVisible( false );

        GamePanel.shipMoving = true;
        GamePanel.aliensMoving = true;
        GamePanel.alienShooting = true;

        timer = new Timer( DELAY, panel );
        timer.start();
        alienTimer.start();
    }

    /**
     * Sets the game over state and performs necessary cleanup actions.
     * Stops all timers, updates the high score, and sets up the replay button.
     */
    static void gameOver()
    {
        // Set the game over state
        GamePanel.isGameOver = true;
        GamePanel.difficultyMultiplier = 0;
        ALIEN_MOVEMENT_GAMEWON_DELAY = ALIEN_MOVEMENT_RESET_DELAY - ( GamePanel.difficultyMultiplier * 20 );

        ALIEN_MOVEMENT_DELAY = ALIEN_MOVEMENT_RESET_DELAY;

        ufoTimer.stop();
        GamePanel.ufoActive = false;
        alienTimer.stop();
        timer.stop();

        // Update the high score
        GamePanel.highScore = Math.max( GamePanel.highScore, GamePanel.score );
        if ( GamePanel.score > GamePanel.highScore )
            Helpers.writeHighScore( GamePanel.highScore );

        setupReplayButton();
    }

    /**
     * Resets the game state variables and timers when the player wins the game.
     * Increases the difficulty by reducing the alien delay.
     *
     * @param panel the GamePanel object representing the game panel
     */
    static void gameWon( GamePanel panel )
    {
        GamePanel.difficultyMultiplier++;
        ALIEN_MOVEMENT_GAMEWON_DELAY = ALIEN_MOVEMENT_RESET_DELAY - ( GamePanel.difficultyMultiplier * 20 );

        // Alien delay is reduced to increase difficulty
        ALIEN_MOVEMENT_DELAY = Math.max( 20, ALIEN_MOVEMENT_GAMEWON_DELAY );

        // Reset game state variables
        GamePanel.shipDirection = ' ';
        GamePanel.directionQueue.clear();
        GamePanel.shipMoving = true;
        GamePanel.alienShooting = true;
        GamePanel.shipShooting = true;

        alienTimer.stop();
        ufoTimer.restart();
        GamePanel.ufoActive = false;
        timer.stop();

        // Reset bullets
        GamePanel.shipBullet.clear();
        GamePanel.alienBullet.clear();
        panel.resetAlienShootCooldown();

        // Reinitialize alien positions
        initAliens();
        initAlienTimer( panel );

        // Reset the ship's position
        GamePanel.xOfShip[0] = ( GamePanel.SCREEN_WIDTH / 2 ) - ( GamePanel.UNIT_SIZE / 2 );

        initShelters();

        // Update the high score
        GamePanel.highScore = Math.max( GamePanel.highScore, GamePanel.score );
        Helpers.writeHighScore( GamePanel.highScore );

        // Start or restart the game timers
        if ( timer != null )
            timer.stop();

        timer = new Timer( DELAY, panel );
        timer.start();
        panel.repaint();
    }

    /**
     * Restarts the game by resetting all game state variables and clearing lists of alien positions and bullets.
     *
     * @param panel the GamePanel object representing the game panel
     */
    static void restartGame( GamePanel panel )
    {
        GamePanel.isGameOver = false;
        GamePanel.lives = 3;
        GamePanel.score = 0;

        // Clear the alien positions lists
        GamePanel.xOfAliens.clear();
        GamePanel.yOfAliens.clear();

        // Reset bullets
        GamePanel.shipBullet.clear();
        GamePanel.alienBullet.clear();
        panel.resetAlienShootCooldown();

        // Reset game state variables
        GamePanel.shipDirection = ' ';
        GamePanel.directionQueue.clear();
        GamePanel.shipMoving = true;
        GamePanel.alienShooting = true;

        // Reinitialize alien positions
        initAliens();
        initAlienTimer( panel );

        ufoTimer.restart();
        GamePanel.ufoActive = false;

        // Reset the ship's position
        GamePanel.xOfShip[0] = ( GamePanel.SCREEN_WIDTH / 2 ) - ( GamePanel.UNIT_SIZE / 2 );

        initShelters();

        // Disable the replay button until the game is over
        replayButton.setEnabled( false );
        replayButton.setVisible( false );

        // Start or restart the game timers
        if ( timer != null )
            timer.stop();

        timer = new Timer( DELAY, panel );
        timer.start();

        panel.repaint();
    }

    /**
     * Initializes a timer for moving aliens in the game panel.
     *
     * @param panel the GamePanel object where the aliens are to be moved
     * @throws NullPointerException if the panel parameter is null
     */
    static void initAlienTimer( GamePanel panel )
    {
        if ( panel == null )
            throw new NullPointerException( "GamePanel object cannot be null" );

        alienTimer = new Timer( ALIEN_MOVEMENT_DELAY, new ActionListener() {
            @Override public void actionPerformed( ActionEvent e )
            {
                GamePanel.moveAliens();
                panel.repaint();
            }
        } );

        alienTimer.start();
    }

    /**
     * Initializes the positions of the aliens on the game panel.
     *
     * This method calculates the starting positions of the aliens on the game panel based on the screen width, unit
     * size, and spacing between aliens. The aliens are arranged in a grid pattern with 11 columns and 5 rows.
     *
     * @param None
     * @return None
     */
    static void initAliens()
    {
        double alienSpacing = GamePanel.UNIT_SIZE / 1.5; // Space between aliens
        int startX = (int)( ( GamePanel.SCREEN_WIDTH - ( 11 * GamePanel.UNIT_SIZE ) - ( 10 * alienSpacing ) ) / 2 );

        int startY = 4 * GamePanel.UNIT_SIZE; // Adjust this multiplier to change the vertical start position

        for ( int row = 0; row < 5; row++ )
        {
            for ( int col = 0; col < 11; col++ )
            {
                GamePanel.xOfAliens.add( (int)( startX + col * ( GamePanel.UNIT_SIZE + alienSpacing ) ) );
                GamePanel.yOfAliens.add( (int)( startY + row * ( GamePanel.UNIT_SIZE + alienSpacing ) ) );
            }
        }
    }

    /**
     * Initializes a timer for the UFO in the game panel.
     *
     * This method creates a new Timer object with a specified interval for the UFO to appear on the game panel.
     * When the timer triggers, it sets the UFO's X position to 0 and marks the UFO as active.
     * It then calls the repaint method on the provided GamePanel instance to update the display.
     *
     * @param panel The GamePanel instance on which the UFO will appear and be updated.
     * @throws NullPointerException if the panel parameter is null.
     */
    static void initUfoTimer( GamePanel panel )
    {
        if ( panel == null )
            throw new NullPointerException( "The panel parameter cannot be null." );

        ufoTimer = new Timer( GamePanel.UFO_INTERVAL, e -> {
            GamePanel.ufoX = 0;
            GamePanel.ufoActive = true;
            panel.repaint(); // Call repaint on the passed instance
        } );
        ufoTimer.start();
    }

    /**
     * This method is responsible for rendering the UI elements on the screen, such as lives, score, and high score.
     *
     * @param g The Graphics object used for rendering the UI elements.
     * @param panel The GamePanel object that contains information about the game state.
     */
    static void UIelements( Graphics g, GamePanel panel )
    {
        // Set the font and color for the UI text
        g.setFont( UI_FONT );
        g.setColor( GamePanel.SCORE_COLOR );

        // Draw lives on the top left
        g.drawString( "Lives: " + GamePanel.lives, 10, 30 );

        // Draw score on the top center
        FontMetrics metrics = panel.getFontMetrics( UI_FONT );
        String scoreText = "Score: " + GamePanel.score;
        g.drawString( scoreText, ( GamePanel.SCREEN_WIDTH - metrics.stringWidth( scoreText ) ) / 2, 30 );

        // Draw high score on the top right
        String highScoreText = "High Score: " + GamePanel.highScore;
        g.drawString( highScoreText, GamePanel.SCREEN_WIDTH - metrics.stringWidth( highScoreText ) - 10, 30 );
    }

    /**
     * Draws the game over screen with the game over text and scores.
     *
     * @param g the Graphics object used for drawing
     * @param panel the GamePanel object containing game information
     */
    static void drawGameOverScreen( Graphics g, GamePanel panel )
    {
        // Display game over text and scores
        drawCenteredText( g, "Game Over", LARGE_FONT, GamePanel.SCREEN_HEIGHT / 3, false, panel );
        drawCenteredText( g, "High Score: " + GamePanel.highScore, MEDIUM_FONT,
                          GamePanel.SCREEN_HEIGHT / 3 + LARGE_FONT.getSize(), true, panel );
        drawCenteredText( g, "Score: " + GamePanel.score, MEDIUM_FONT,
                          GamePanel.SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() + MEDIUM_FONT.getSize() + 20, true,
                          panel );
    }

    /**
     * Draws centered text on the screen using the specified graphics object, font, y position, and game panel.
     *
     * @param g the graphics object to draw the text on
     * @param text the text to be drawn
     * @param font the font to use for the text
     * @param yPos the y position of the text on the screen
     * @param panel the game panel to get font metrics from
     */
    private static void drawCenteredText( Graphics g, String text, Font font, int yPos, boolean isScore,
                                          GamePanel panel )
    {
        if ( isScore )
        {
            g.setFont( font );
            g.setColor( GamePanel.SCORE_COLOR );
            FontMetrics metrics = panel.getFontMetrics( font );
            int x = ( GamePanel.SCREEN_WIDTH - metrics.stringWidth( text ) ) / 2;
            g.drawString( text, x, yPos );
        }
        else
        {
            g.setFont( font );
            g.setColor( GamePanel.GAME_OVER_COLOR );
            FontMetrics metrics = panel.getFontMetrics( font );
            int x = ( GamePanel.SCREEN_WIDTH - metrics.stringWidth( text ) ) / 2;
            g.drawString( text, x, yPos );
        }
    }

    /**
     * Sets up the replay button on the game panel.
     * The replay button is centered horizontally at the bottom of the screen.
     *
     * @param buttonWidth the width of the replay button
     * @param buttonHeight the height of the replay button
     * @param buttonX the x-coordinate of the replay button
     * @param buttonY the y-coordinate of the replay button
     */
    static void setupReplayButton()
    {
        int buttonWidth = 150;
        int buttonHeight = 50;
        int buttonX = ( GamePanel.SCREEN_WIDTH - buttonWidth ) / 2;
        int buttonY = GamePanel.SCREEN_HEIGHT - 120;

        replayButton.setBounds( buttonX, buttonY, buttonWidth, buttonHeight );
        replayButton.setEnabled( true );
        replayButton.setVisible( true );
    }

    static void initShelters()
    {
        // Clear the current shelters list
        GamePanel.shelters.clear();

        // Create new shelters and add them to the list
        int numberOfShelters = 4; // Number of shelters
        int firstShelterX = ( GamePanel.SCREEN_WIDTH - ( numberOfShelters * Shelter.SHELTER_WIDTH +
                                                         ( numberOfShelters - 1 ) * Shelter.SHELTER_PADDING ) ) /
                            2;
        int shelterY = GamePanel.SCREEN_HEIGHT - 3 * GamePanel.UNIT_SIZE; // Position the shelters vertically

        for ( int i = 0; i < numberOfShelters; i++ )
        {
            GamePanel.shelters.add(
                new Shelter( firstShelterX + i * ( Shelter.SHELTER_WIDTH + Shelter.SHELTER_PADDING ), shelterY ) );
        }
    }
}
