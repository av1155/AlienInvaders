package edu.aav66;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.Timer;

class GameState
{
    static final int DELAY = 75;
    static int ALIEN_DELAY = 600;
    static int RESET_DELAY = 600;

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

    static void initAlienTimer( GamePanel panel )
    {
        alienTimer = new Timer( ALIEN_DELAY, new ActionListener() {
            @Override public void actionPerformed( ActionEvent e )
            {
                GamePanel.moveAliens();
                panel.repaint();
            }
        } );
        alienTimer.start();
    }

    static void initAliens()
    {
        int alienSpacing = GamePanel.UNIT_SIZE; // Space between aliens
        int startX = ( GamePanel.SCREEN_WIDTH - ( 11 * GamePanel.UNIT_SIZE ) - ( 10 * alienSpacing ) ) / 2;
        int startY = 3 * GamePanel.UNIT_SIZE;

        for ( int row = 0; row < 5; row++ )
        {
            for ( int col = 0; col < 11; col++ )
            {
                GamePanel.xOfAliens.add( startX + col * ( GamePanel.UNIT_SIZE + alienSpacing ) );
                GamePanel.yOfAliens.add( startY + row * ( GamePanel.UNIT_SIZE + alienSpacing ) );
            }
        }
    }

    static void initUfoTimer( GamePanel panel )
    {
        ufoTimer = new Timer( GamePanel.UFO_INTERVAL, e -> {
            GamePanel.ufoX = 0;
            GamePanel.ufoActive = true;
            panel.repaint(); // Call repaint on the passed instance
        } );
        ufoTimer.start();
    }

    /**
     * Displays UI elements such as score, lives, and high score.
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
     * Handles game over logic, including displaying scores and high scores.
     *
     * @param g The graphics context used for drawing.
     */
    static void gameOver()
    {
        // Set the game over state
        GamePanel.isGameOver = true;

        ALIEN_DELAY = RESET_DELAY;

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
     * Handles game over logic, including displaying scores and high scores.
     *
     * @param g The graphics context used for drawing.
     */
    static void gameWon( GamePanel panel )
    {
        // Alien delay is reduced to increase difficulty
        ALIEN_DELAY = Math.max( 100, ALIEN_DELAY - 50 );

        // Reset game state variables
        GamePanel.aliensKilled = 0;
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

        // Reinitialize alien positions
        initAliens();
        initAlienTimer( panel );

        // Reset the ship's position
        GamePanel.xOfShip[0] = ( GamePanel.SCREEN_WIDTH / 2 ) - ( GamePanel.UNIT_SIZE / 2 );

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

    static void drawGameOverScreen( Graphics g, GamePanel panel )
    {
        // Display game over text and scores
        drawCenteredText( g, "Game Over", LARGE_FONT, GamePanel.SCREEN_HEIGHT / 3, panel );
        drawCenteredText( g, "High Score: " + GamePanel.highScore, MEDIUM_FONT,
                          GamePanel.SCREEN_HEIGHT / 3 + LARGE_FONT.getSize(), panel );
        drawCenteredText( g, "Score: " + GamePanel.score, MEDIUM_FONT,
                          GamePanel.SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() + MEDIUM_FONT.getSize() + 20, panel );
    }

    /**
     * Draws centered text on the screen.
     *
     * @param g    The graphics context used for drawing.
     * @param text The text to be drawn.
     * @param font The font used for the text.
     * @param yPos The vertical position for the text.
     */
    private static void drawCenteredText( Graphics g, String text, Font font, int yPos, GamePanel panel )
    {
        g.setFont( font );
        g.setColor( GamePanel.SCORE_COLOR );
        FontMetrics metrics = panel.getFontMetrics( font );
        int x = ( GamePanel.SCREEN_WIDTH - metrics.stringWidth( text ) ) / 2;
        g.drawString( text, x, yPos );
    }

    /**
     * Configures and displays the replay button after a game over.
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

    /**
     * Resets the game to its initial state, ready to start anew.
     */
    static void restartGame( GamePanel panel )
    {
        GamePanel.isGameOver = false;
        GamePanel.lives = 3;
        GamePanel.score = 0;
        GamePanel.aliensKilled = 0;

        // Clear the alien positions lists
        GamePanel.xOfAliens.clear();
        GamePanel.yOfAliens.clear();

        // Reset bullets
        GamePanel.shipBullet.clear();
        GamePanel.alienBullet.clear();

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
}
