package edu.aav66;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The main game panel for the Alien Invaders game.
 * This class handles game initialization, input handling, game logic, and
 * rendering.
 * It extends JPanel and implements ActionListener to handle game events and
 * actions.
 */
public class GamePanel extends JPanel implements ActionListener
{
    // CONSTANTS
    static final int SCREEN_WIDTH = 672;  // 224 * 3
    static final int SCREEN_HEIGHT = 768; // 256 * 3
    static final int UNIT_SIZE = 24;      // This size allows for a grid of 28 x 32
    static final int GAME_UNITS = ( SCREEN_WIDTH * SCREEN_HEIGHT ) / UNIT_SIZE;
    static final int DELAY = 100;
    static final int ALIEN_DELAY = 1000;

    static final Color BACKGROUND_COLOR = Color.black;
    static final Color SCORE_COLOR = Color.white;
    static final Color SHIP_COLOR = Color.white;
    static final Color SHIP_BULLET_COLOR = Color.gray;

    // Olive green
    static final Color LARGE_ALIEN_COLOR = new Color( 106, 168, 79 );
    // Dark golden rod
    static final Color MEDIUM_ALIEN_COLOR = new Color( 184, 134, 11 );
    // Red-orange
    static final Color SMALL_ALIEN_COLOR = new Color( 255, 69, 0 );
    // Medium violet red
    static final Color ALIEN_BULLET_COLOR = new Color( 199, 21, 133 );

    // COORDINATE ARRAYS
    // x coordinates of ship
    public final int[] xOfShip = new int[GAME_UNITS];

    // x and y coordinates of ship bullets shot
    public final int[] xOfBullet = new int[GAME_UNITS];
    public final int[] yOfBullet = new int[GAME_UNITS];

    // x and y coordinates of aliens
    public final int[] xOfAliens = new int[GAME_UNITS];
    public final int[] yOfAliens = new int[GAME_UNITS];

    // DIRECTION QUEUE
    private Deque<Character> directionQueue = new ArrayDeque<>();

    // BULLET QUEUES
    Deque<Integer> shipBullet = new ArrayDeque<>();
    Deque<Integer> alienBullet = new ArrayDeque<>();

    // GAME STATE VARIABLES
    int lives = 3;
    int score = 0;
    int highScore = 0;

    // LOGIC VARIABLES
    // Ship:
    char shipDirection = ' ';
    boolean shipMoving = false;
    boolean shipShooting = false;
    boolean shipShot = false;

    // Aliens:
    char aliensDirection = 'R';
    boolean aliensMoving = true;
    boolean alienShooting = false;
    boolean smallAlienShot = false;
    boolean mediumAlienShot = false;
    boolean bigAlienShot = false;

    // Processes
    Random random;
    Timer timer;
    JButton replayButton;

    Timer alienTimer;

    // Constants for font sizes
    private static final Font UI_FONT = new Font( "Futura", Font.PLAIN, 20 );
    private static final Font LARGE_FONT = new Font( "Futura", Font.BOLD, 75 );
    private static final Font MEDIUM_FONT = new Font( "Futura", Font.BOLD, 40 );

    /**
     * Constructor for GamePanel. Initializes the game environment, including UI
     * components, and starts the game.
     */
    public GamePanel()
    {
        random = new Random();
        this.setPreferredSize( new Dimension( SCREEN_WIDTH, SCREEN_HEIGHT ) );
        this.setBackground( BACKGROUND_COLOR );
        this.setDoubleBuffered( true );
        this.setFocusable( true );
        this.addKeyListener( new MyKeyAdapter() );

        // Center the ship horizontally
        xOfShip[0] = ( SCREEN_WIDTH / 2 ) - ( UNIT_SIZE / 2 );
        initializeAliens();

        initUI();
        initAlienTimer();
        highScore = Helpers.initializeHighScore( highScore );
        startGame();
        Helpers.playMusic();
    }

    /**
     * Initializes the UI components such as the replay button.
     */
    private void initUI()
    {
        replayButton = new JButton( "Replay" );
        replayButton.setFont( MEDIUM_FONT );
        replayButton.addActionListener( e -> restartGame() );
        replayButton.setBounds( SCREEN_WIDTH / 2 - 50, SCREEN_HEIGHT - 60, 100, 40 );
        replayButton.setEnabled( false );
        replayButton.setFocusable( false );
        this.add( replayButton );
    }

    private void initAlienTimer()
    {
        alienTimer = new Timer( ALIEN_DELAY, new ActionListener() {
            @Override public void actionPerformed( ActionEvent e )
            {
                moveAliens();
                repaint();
            }
        } );
        alienTimer.start();
    }

    private void initializeAliens()
    {
        int alienSpacing = UNIT_SIZE; // Space between aliens
        int startX =
            ( SCREEN_WIDTH - ( 11 * UNIT_SIZE ) - ( 10 * alienSpacing ) ) / 2; // Center the aliens horizontally
        int startY = 3 * UNIT_SIZE; // Starting height from top, adjust as needed

        for ( int row = 0; row < 5; row++ )
        {
            for ( int col = 0; col < 11; col++ )
            {
                int index = row * 11 + col;
                xOfAliens[index] = startX + col * ( UNIT_SIZE + alienSpacing );
                yOfAliens[index] = startY + row * ( UNIT_SIZE + alienSpacing );
            }
        }
    }

    /**
     * Starts the game by enabling movements, setting visibility, and starting the
     * timer.
     */
    void startGame()
    {
        replayButton.setEnabled( false );
        replayButton.setVisible( false );
        shipMoving = true;
        aliensMoving = true;
        alienShooting = true;
        shipShooting = true;
        timer = new Timer( DELAY, this );
        timer.start();
        alienTimer.start();
    }

    /**
     * Custom rendering of the game's graphical elements like the ship, aliens, and
     * bullets.
     *
     * @param g The graphics context used for drawing.
     */
    @Override public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        draw( g );
        UIelements( g );
    }

    /**
     * Draws the game elements on the screen, such as the ship, aliens, and bullets.
     *
     * @param g The graphics context used for drawing.
     */
    void draw( Graphics g )
    {
        // Draw ship
        g.setColor( SHIP_COLOR );
        g.fillRect( xOfShip[0], SCREEN_HEIGHT - UNIT_SIZE, UNIT_SIZE, UNIT_SIZE );

        // Draw aliens
        for ( int i = 0; i < 55; i++ )
        {
            Color alienColor = LARGE_ALIEN_COLOR; // Default to large
            if ( i < 11 )
            {
                alienColor = SMALL_ALIEN_COLOR; // Top row - small aliens
            }
            else if ( i < 33 )
            {
                alienColor = MEDIUM_ALIEN_COLOR; // Middle rows - medium aliens
            }
            g.setColor( alienColor );
            g.fillRect( xOfAliens[i], yOfAliens[i], UNIT_SIZE, UNIT_SIZE );
        }

        // Draw ship bullets
        g.setColor( SHIP_BULLET_COLOR );
        shipBullet.forEach( index -> g.fillRect( xOfBullet[index], yOfBullet[index], 5, 10 ) );
    }

    /**
     * Handles movement of the ship based on the current direction.
     */
    void moveShip()
    {
        if ( shipMoving )
        {
            switch ( shipDirection )
            {
            case 'L':
                xOfShip[0] -= UNIT_SIZE;
                if ( xOfShip[0] < 0 )
                {
                    xOfShip[0] = 0;
                }
                break;
            case 'R':
                xOfShip[0] += UNIT_SIZE;
                if ( xOfShip[0] > SCREEN_WIDTH - UNIT_SIZE )
                {
                    xOfShip[0] = SCREEN_WIDTH - UNIT_SIZE;
                }
                break;
            }
        }
    }

    /**
     * Handles the movement of aliens across the screen and down towards the ship.
     */
    void moveAliens()
    {
        boolean edgeReached = false;
        int horizontalMove = UNIT_SIZE;
        if ( aliensDirection == 'R' )
        {
            for ( int i = 0; i < xOfAliens.length; i++ )
            {
                xOfAliens[i] += horizontalMove;
                if ( xOfAliens[i] >= SCREEN_WIDTH - UNIT_SIZE )
                {
                    edgeReached = true;
                }
            }
        }
        else
        {
            for ( int i = 0; i < xOfAliens.length; i++ )
            {
                xOfAliens[i] -= horizontalMove;
                if ( xOfAliens[i] <= 0 )
                {
                    edgeReached = true;
                }
            }
        }

        if ( edgeReached )
        {
            for ( int i = 0; i < yOfAliens.length; i++ )
            {
                yOfAliens[i] += UNIT_SIZE; // Move down by one unit
            }
            aliensDirection = ( aliensDirection == 'R' ? 'L' : 'R' ); // Reverse direction
        }
    }

    /**
     * Allows the ship to fire bullets upwards towards the aliens.
     */
    void bulletsFromShip()
    {
        if ( shipShooting && shipBullet.isEmpty() )
        {
            int bulletIndex = 0;                                     // Only one bullet on screen at a time
            xOfBullet[bulletIndex] = xOfShip[0] + UNIT_SIZE / 2 - 2; // Center of ship
            yOfBullet[bulletIndex] = SCREEN_HEIGHT - 2 * UNIT_SIZE;  // Start just above the ship
            shipBullet.addLast( bulletIndex );
        }
    }

    /**
     * Allows aliens to fire bullets downwards towards the ship.
     */
    void bulletsFromAliens() {}

    void moveBullets()
    {
        Iterator<Integer> iterator = shipBullet.iterator();
        while ( iterator.hasNext() )
        {
            int index = iterator.next();
            yOfBullet[index] -= UNIT_SIZE; // Move bullet up
            if ( yOfBullet[index] < 0 )
            {
                iterator.remove(); // Remove bullet if it goes off screen
            }
        }
    }

    /**
     * Checks for collisions between different game elements including ship bullets
     * and aliens, as well as alien bullets and the ship. Upon detecting a collision
     * between a ship bullet and an alien, the relevant alien is marked as shot,
     * which
     * triggers its removal from the game and an increase in the player's score.
     * Similarly, collisions between alien bullets and the ship decrease the
     * player's
     * lives, potentially leading to a game over scenario.
     */
    void checkCollisions()
    {
        // Implementation details:
        // 1. Iterate through each bullet in the shipBullet queue to check if any bullet
        // intersects with any alien's
        // bounding box.
        // 2. Iterate through each bullet in the alienBullet queue to check if any
        // bullet intersects with the ship's
        // bounding box.
        // 3. Update game state variables such as shipShot, alienShot, and lives based
        // on collision detection.
        // 4. Remove bullets and aliens as necessary based on the results of collision
        // detection.
        //
        //
        // check wether an alien was shot; eliminated:
        // if (bulletX&Y coordinates equal alienX&Y coordinates)
        // then alienShot = true;
        // else
        // alienShot = false;
        //
        //
        // check wether the ship was hit; lives--:
        //
        // if (alientBulletX&Y coordinates equal shipX&Y coordinates)
        // shipShot = true;
        // lives--;
        //
        // if (lives == 0)
        // gameOver();
        //
        // else
        // shipShot = false;
    }

    /**
     * Updates the score based on which aliens have been shot.
     */
    void score()
    {
        // if (bigAlienShot)
        // then score += 10;
        // else if (mediumAlienShot)
        // then score += 20;
        // else if (smallAlienShot)
        // then score += 30;
    }

    /**
     * Displays UI elements such as score, lives, and high score.
     */
    void UIelements( Graphics g )
    {
        // Set the font and color for the UI text
        g.setFont( UI_FONT );
        g.setColor( SCORE_COLOR );

        // Draw lives on the top left
        g.drawString( "Lives: " + lives, 10, 30 );

        // Draw score on the top center
        FontMetrics metrics = getFontMetrics( UI_FONT );
        String scoreText = "Score: " + score;
        g.drawString( scoreText, ( SCREEN_WIDTH - metrics.stringWidth( scoreText ) ) / 2, 30 );

        // Draw high score on the top right
        String highScoreText = "High Score: " + highScore;
        g.drawString( highScoreText, SCREEN_WIDTH - metrics.stringWidth( highScoreText ) - 10, 30 );
    }

    /**
     * Handles game over logic, including displaying scores and high scores.
     *
     * @param g The graphics context used for drawing.
     */
    void gameOver( Graphics g )
    {

        alienTimer.stop();
        // Show score, high score, and
        highScore = Math.max( highScore, score );
        Helpers.writeHighScore( highScore );

        drawCenteredText( g, "Game Over", LARGE_FONT, SCREEN_HEIGHT / 3 );
        drawCenteredText( g, "High Score: " + highScore, MEDIUM_FONT, SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() );
        drawCenteredText( g, "Score: " + score, MEDIUM_FONT,
                          SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() + MEDIUM_FONT.getSize() + 20 );

        // Enable the color toggle button and replay button
        setupReplayButton();
    }

    /**
     * Handles action events during gameplay such as moving the ship or firing
     * bullets.
     *
     * @param e The action event to be processed.
     */
    @Override public void actionPerformed( ActionEvent e )
    {
        synchronized ( directionQueue )
        {
            if ( !directionQueue.isEmpty() )
            {
                shipDirection = directionQueue.poll();
                shipMoving = true; // Ensure the ship moves when there's direction queued
            }
        }

        if ( shipMoving )
        {
            moveShip();
        }

        bulletsFromAliens();
        moveBullets();
        checkCollisions();
        repaint();
    }

    /**
     * Draws centered text on the screen.
     *
     * @param g    The graphics context used for drawing.
     * @param text The text to be drawn.
     * @param font The font used for the text.
     * @param yPos The vertical position for the text.
     */
    private void drawCenteredText( Graphics g, String text, Font font, int yPos )
    {
        g.setFont( font );
        g.setColor( SCORE_COLOR );
        FontMetrics metrics = getFontMetrics( font );
        int x = ( SCREEN_WIDTH - metrics.stringWidth( text ) ) / 2;
        g.drawString( text, x, yPos );
    }

    /**
     * Configures and displays the replay button after a game over.
     */
    private void setupReplayButton()
    {
        int buttonWidth = 150;
        int buttonHeight = 50;
        int buttonX = ( SCREEN_WIDTH - buttonWidth ) / 2;
        int buttonY = SCREEN_HEIGHT - 120;

        replayButton.setBounds( buttonX, buttonY, buttonWidth, buttonHeight );
        replayButton.setEnabled( true );
        replayButton.setVisible( true );
    }

    /**
     * Resets the game to its initial state, ready to start anew.
     */
    void restartGame()
    {
        // Reset the lives counter
        lives = 3;

        // x[] = center of screen; // Reset x position of ship to the center of the
        // screen

        // Reset game state variables
        score = 0;
        shipBullet.clear();
        alienBullet.clear();

        // LOGIC VARIABLES
        // Ship:
        shipDirection = ' ';
        shipMoving = false;
        shipShooting = false;
        shipShot = false;

        // Aliens:
        aliensDirection = 'R';
        aliensMoving = true;
        alienShooting = false;
        smallAlienShot = false;
        mediumAlienShot = false;
        bigAlienShot = false;

        // Disable the replay button until the game is over
        replayButton.setEnabled( false );
        replayButton.setVisible( false );

        timer.stop();                     // Stop the current timer
        timer = new Timer( DELAY, this ); // Reinitialize the timer
        timer.start();                    // Restart the timer

        repaint(); // Repaint the game panel to refresh the screen
    }

    /**
     * Handles key presses to control the ship's movement and firing.
     */
    public class MyKeyAdapter extends KeyAdapter
    {
        @Override public void keyPressed( KeyEvent e )
        {
            synchronized ( directionQueue )
            {
                char newDirection = ' ';
                switch ( e.getKeyCode() )
                {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    newDirection = 'L';
                    break;

                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    newDirection = 'R';
                    break;

                case KeyEvent.VK_SPACE:
                    if ( shipBullet.isEmpty() )
                    {
                        shipShooting = true;
                        bulletsFromShip();
                    }
                    return; // Skip direction queueing for shooting
                }
                if ( newDirection != ' ' && ( directionQueue.isEmpty() || directionQueue.getLast() != newDirection ) )
                {
                    directionQueue.add( newDirection );
                }
            }
        }

        @Override public void keyReleased( KeyEvent e )
        {
            synchronized ( directionQueue )
            {
                // Clear the direction queue when the key is released
                directionQueue.clear();
            }
            shipMoving = false;
            shipShooting = false; // Ensure we stop shooting when space is released
        }
    }
}
