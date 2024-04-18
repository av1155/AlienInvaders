package edu.aav66;

import java.awt.*;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
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
    static final int UNIT_SIZE = 26;      // This size allows for a grid of 28 x 32
    static final int GAME_UNITS = ( SCREEN_WIDTH * SCREEN_HEIGHT ) / UNIT_SIZE;
    static final int DELAY = 75;
    static int ALIEN_DELAY = 500;
    static int RESET_DELAY = 500;

    static final Color BACKGROUND_COLOR = Color.black;
    static final Color SCORE_COLOR = Color.white;
    static final Color SHIP_COLOR = Color.white;
    static final Color SHIP_BULLET_COLOR = Color.gray;

    static final int SHIP_HEIGHT = 30;
    static final int SHIP_WIDTH = 60;
    static final int BULLET_HEIGHT = 12;
    static final int BULLET_WIDTH = 6;

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
    public final int[] xOfShipBullet = new int[GAME_UNITS];
    public final int[] yOfShipBullet = new int[GAME_UNITS];

    // x and y coordinates of ship bullets shot
    public final int[] xOfAlienBullet = new int[GAME_UNITS];
    public final int[] yOfAlienBullet = new int[GAME_UNITS];

    // x and y coordinates of aliens
    List<Integer> xOfAliens = new ArrayList<>();
    List<Integer> yOfAliens = new ArrayList<>();

    // DIRECTION QUEUE
    private Deque<Character> directionQueue = new ArrayDeque<>();

    // Using a HashSet to track which keys are currently pressed
    private Set<Integer> pressedKeys = new HashSet<>();

    // BULLET QUEUES
    Deque<Integer> shipBullet = new ArrayDeque<>();
    Deque<Integer> alienBullet = new ArrayDeque<>();

    class Explosion
    {
        Point location;
        int timer;

        Explosion( Point location, int timer )
        {
            this.location = location;
            this.timer = timer;
        }
    }

    // List to hold explosion details
    List<Explosion> explosions = new ArrayList<>();
    int explosionDuration = 10; // frames

    // GAME STATE VARIABLES
    int lives = 3;
    int score = 0;
    int highScore = 0;
    int aliensKilled = 0;
    boolean isGameOver = false;

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
    Timer alienTimer;
    JButton replayButton;

    // UFO attributes
    private int ufoX;
    private int ufoY = 50;    // Vertical position set just below the top of the screen
    private int ufoSpeed = 2; // Speed can be adjusted for difficulty
    private boolean ufoActive = false;
    private Timer ufoTimer;
    private final int UFO_INTERVAL = 20000; // 20 seconds between appearances
    // private final Color UFO_COLOR = new Color( 128, 0, 128 ); // Purple color

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
        this.setLayout( null );

        // Initialize key listener setup
        this.addKeyListener( new KeyAdapter() {
            @Override public void keyPressed( KeyEvent e )
            {
                pressedKeys.add( e.getKeyCode() );
                updateMovementAndShooting(); // Update based on current keys pressed
            }

            @Override public void keyReleased( KeyEvent e )
            {
                pressedKeys.remove( e.getKeyCode() );
                updateMovementAndShooting(); // Update on key release too
            }
        } );

        // Center the ship horizontally
        xOfShip[0] = ( SCREEN_WIDTH / 2 ) - ( UNIT_SIZE / 2 );

        // Initialize the replay button
        replayButton = new JButton( "Replay" );
        replayButton.setFont( new Font( "Futura", Font.BOLD, 20 ) );
        replayButton.addActionListener( e -> restartGame() );
        int buttonWidth = 150;
        int buttonHeight = 50;
        int buttonX = ( SCREEN_WIDTH - buttonWidth ) / 2;
        int buttonY = SCREEN_HEIGHT - 120;
        replayButton.setBounds( buttonX, buttonY, buttonWidth, buttonHeight );
        replayButton.setEnabled( false );
        replayButton.setFocusable( true );
        this.add( replayButton );

        initAliens();
        initAlienTimer();
        initUfoTimer();
        highScore = Helpers.initializeHighScore( highScore );
        startGame();
        Helpers.playMusic();
    }

    private void updateMovementAndShooting()
    {
        shipMoving = pressedKeys.contains( KeyEvent.VK_LEFT ) || pressedKeys.contains( KeyEvent.VK_RIGHT );
        shipShooting = pressedKeys.contains( KeyEvent.VK_SPACE );

        if ( pressedKeys.contains( KeyEvent.VK_LEFT ) )
        {
            shipDirection = 'L';
        }
        else if ( pressedKeys.contains( KeyEvent.VK_RIGHT ) )
        {
            shipDirection = 'R';
        }
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

    private void initAliens()
    {
        int alienSpacing = UNIT_SIZE; // Space between aliens
        int startX = ( SCREEN_WIDTH - ( 11 * UNIT_SIZE ) - ( 10 * alienSpacing ) ) / 2;
        int startY = 3 * UNIT_SIZE;

        for ( int row = 0; row < 5; row++ )
        {
            for ( int col = 0; col < 11; col++ )
            {
                xOfAliens.add( startX + col * ( UNIT_SIZE + alienSpacing ) );
                yOfAliens.add( startY + row * ( UNIT_SIZE + alienSpacing ) );
            }
        }
    }

    private void initUfoTimer()
    {
        ufoTimer = new Timer( UFO_INTERVAL, e -> {
            ufoX = 0;         // Start from the left edge
            ufoActive = true; // Activate the UFO
        } );
        ufoTimer.start();
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

        if ( isGameOver )
            drawGameOverScreen( g );
    }

    /**
     * Draws the game elements on the screen, such as the ship, aliens, and bullets.
     *
     * @param g The graphics context used for drawing.
     */
    void draw( Graphics g )
    {
        // Draw the player's ship
        BufferedImage playerShipImage = Helpers.getPlayerShip();
        if ( playerShipImage != null )
        {
            g.drawImage( playerShipImage, xOfShip[0], SCREEN_HEIGHT - playerShipImage.getHeight(), this );
        }

        // Draw aliens
        for ( int i = 0; i < xOfAliens.size(); i++ )
        {
            BufferedImage alienImage;
            int row = i / 11;
            if ( row == 0 )
            {
                alienImage = Helpers.getYellowAlien(); // Small aliens
            }
            else if ( row < 3 )
            {
                alienImage = Helpers.getGreenAlien(); // Medium aliens
            }
            else
            {
                alienImage = Helpers.getRedAlien(); // Large aliens
            }

            if ( alienImage != null )
            {
                g.drawImage( alienImage, xOfAliens.get( i ), yOfAliens.get( i ), this );
            }
        }

        // Draw UFO
        BufferedImage ufoImage = Helpers.getUfo();
        if ( ufoImage != null && ufoActive )
        {
            g.drawImage( ufoImage, ufoX, ufoY, this );
        }

        // Draw ship bullets
        g.setColor( SHIP_BULLET_COLOR );
        shipBullet.forEach(
            index -> g.fillRect( xOfShipBullet[index], yOfShipBullet[index], BULLET_WIDTH, BULLET_HEIGHT ) );

        // Draw alien bullets
        g.setColor( ALIEN_BULLET_COLOR );
        alienBullet.forEach(
            index -> g.fillRect( xOfAlienBullet[index], yOfAlienBullet[index], BULLET_WIDTH, BULLET_HEIGHT ) );

        // Draw explosions
        for ( Explosion exp : explosions )
        {
            if ( exp.timer > 0 )
            {
                g.setColor( Color.orange );
                g.fillOval( exp.location.x, exp.location.y, UNIT_SIZE, UNIT_SIZE ); // Simple explosion effect
            }
        }
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
        boolean changeDirection = false;

        for ( int i = 0; i < xOfAliens.size(); i++ )
        {
            if ( aliensDirection == 'R' )
            {
                xOfAliens.set( i, xOfAliens.get( i ) + UNIT_SIZE / 2 ); // Move right
                // Check if any alien touches the right boundary
                if ( xOfAliens.get( i ) > SCREEN_WIDTH - UNIT_SIZE - UNIT_SIZE ) // Decreased by one UNIT_SIZE
                {
                    changeDirection = true;
                }
            }
            else if ( aliensDirection == 'L' )
            {
                xOfAliens.set( i, xOfAliens.get( i ) - UNIT_SIZE / 2 ); // Move left
                // Check if any alien touches the left boundary
                if ( xOfAliens.get( i ) < UNIT_SIZE ) // No change needed here
                {
                    changeDirection = true;
                }
            }
        }

        if ( changeDirection )
        {
            aliensDirection = ( aliensDirection == 'R' ) ? 'L' : 'R';
            for ( int i = 0; i < yOfAliens.size(); i++ )
            {
                // Move aliens down by UNIT_SIZE / 4 as originally planned or adjust as needed
                yOfAliens.set( i, yOfAliens.get( i ) + UNIT_SIZE / 4 );
            }
        }
    }

    /**
     * Allows the ship to fire bullets upwards towards the aliens.
     */

    void bulletsFromShip()
    {
        if ( shipShooting && shipBullet.isEmpty() )
        {
            int bulletIndex = 0; // Only one bullet on screen at a time
            // Center bullet on ship horizontally
            xOfShipBullet[bulletIndex] = xOfShip[0] + SHIP_WIDTH / 2 - BULLET_WIDTH / 2;
            // Start bullet just above the ship vertically
            yOfShipBullet[bulletIndex] = SCREEN_HEIGHT - SHIP_HEIGHT - BULLET_HEIGHT;
            shipBullet.addLast( bulletIndex );
        }
    }

    /**
     * Allows aliens to fire bullets downwards towards the ship.
     */

    void bulletsFromAliens()
    {
        if ( alienShooting && alienBullet.isEmpty() )
        {
            int[] bottomAliens = new int[11];
            Arrays.fill( bottomAliens, -1 );

            for ( int i = 0; i < xOfAliens.size(); i++ )
            {
                int col = i % 11;
                if ( yOfAliens.get( i ) >
                     ( bottomAliens[col] == -1 ? Integer.MIN_VALUE : yOfAliens.get( bottomAliens[col] ) ) )
                {
                    bottomAliens[col] = i;
                }
            }

            List<Integer> shooters =
                Arrays.stream( bottomAliens ).filter( index -> index != -1 ).boxed().collect( Collectors.toList() );
            if ( !shooters.isEmpty() )
            {
                int shooterIndex = shooters.get( random.nextInt( shooters.size() ) );
                int bulletIndex = shooterIndex;
                xOfAlienBullet[bulletIndex] = xOfAliens.get( shooterIndex ) + UNIT_SIZE / 2 - 2;
                yOfAlienBullet[bulletIndex] = yOfAliens.get( shooterIndex ) + UNIT_SIZE;
                alienBullet.addLast( bulletIndex );
            }
        }
    }

    void moveBullets()
    {
        // Move ship bullets
        Iterator<Integer> shipIterator = shipBullet.iterator();
        while ( shipIterator.hasNext() )
        {
            int index = shipIterator.next();
            yOfShipBullet[index] -= UNIT_SIZE; // Move ship bullet up
            if ( yOfShipBullet[index] < 0 )
            {
                shipIterator.remove(); // Remove ship bullet if it goes off screen
            }
        }

        // Move alien bullets
        Iterator<Integer> alienIterator = alienBullet.iterator();
        while ( alienIterator.hasNext() )
        {
            int index = alienIterator.next();
            yOfAlienBullet[index] += UNIT_SIZE; // Move alien bullet down
            if ( yOfAlienBullet[index] > SCREEN_HEIGHT )
            {
                alienIterator.remove(); // Remove alien bullet if it goes off screen
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
        // Total aliens are 55
        // Check collisions of ship bullets with aliens
        // Counter to check if all aliens are shot
        Iterator<Integer> shipBulletIterator = shipBullet.iterator();
        while ( shipBulletIterator.hasNext() )
        {
            int index = shipBulletIterator.next();
            Rectangle bulletRect = new Rectangle( xOfShipBullet[index], yOfShipBullet[index], 5, 10 );

            for ( int i = 0; i < xOfAliens.size(); i++ )
            {
                Rectangle alienRect = new Rectangle( xOfAliens.get( i ), yOfAliens.get( i ), UNIT_SIZE, UNIT_SIZE );
                if ( bulletRect.intersects( alienRect ) )
                {
                    // Trigger explosion
                    explosions.add(
                        new Explosion( new Point( xOfAliens.get( i ), yOfAliens.get( i ) ), explosionDuration ) );

                    // Remove the alien from the list
                    xOfAliens.remove( i );
                    yOfAliens.remove( i );
                    shipBulletIterator.remove(); // Remove the bullet

                    aliensKilled++;
                    if ( aliensKilled == 55 )
                        gameWon();

                    // Score the shot based on alien's type before removal
                    if ( i < 11 )
                    {
                        score += 30; // Assume small alien score
                    }
                    else if ( i < 33 )
                    {
                        score += 20; // Medium alien
                    }
                    else
                    {
                        score += 10; // Large alien
                    }
                    break; // Break since bullet can only hit one alien
                }
            }

            Rectangle ufoRect = new Rectangle( ufoX, ufoY, UNIT_SIZE * 2, UNIT_SIZE );
            shipBulletIterator = shipBullet.iterator();
            while ( shipBulletIterator.hasNext() )
            {
                index = shipBulletIterator.next();
                bulletRect = new Rectangle( xOfShipBullet[index], yOfShipBullet[index], 5, 10 );

                if ( ufoActive && bulletRect.intersects( ufoRect ) )
                {
                    // Trigger explosion
                    explosions.add( new Explosion( new Point( ufoX + UNIT_SIZE, ufoY ), explosionDuration ) );

                    // UFO is hit
                    int[] possibleScores = { 50, 100, 150, 200, 300 };
                    score += possibleScores[random.nextInt( possibleScores.length )];
                    ufoActive = false;           // Deactivate UFO
                    shipBulletIterator.remove(); // Remove the bullet
                }
            }
        }

        // Check collisions of alien bullets with the ship
        Iterator<Integer> alienBulletIterator = alienBullet.iterator();
        while ( alienBulletIterator.hasNext() )
        {
            int index = alienBulletIterator.next();
            Rectangle bulletRect = new Rectangle( xOfAlienBullet[index], yOfAlienBullet[index], 5, 10 );
            Rectangle shipRect = new Rectangle( xOfShip[0], SCREEN_HEIGHT - UNIT_SIZE, UNIT_SIZE, UNIT_SIZE );

            if ( bulletRect.intersects( shipRect ) )
            {
                // Trigger explosion for ship hit
                explosions.add(
                    new Explosion( new Point( xOfShip[0], SCREEN_HEIGHT - UNIT_SIZE ), explosionDuration ) );

                // Collision detected, remove the bullet and subtract a life
                alienBulletIterator.remove();
                lives--;
                shipShot = true;

                if ( lives <= 0 )
                {
                    gameOver();
                }
                break; // Break since one bullet can only hit the ship once
            }
        }

        // Reset shot flags
        smallAlienShot = false;
        mediumAlienShot = false;
        bigAlienShot = false;

        // If aliens reach the ship or the bottom of the screen, it's game over
        for ( int i = 0; i < xOfAliens.size(); i++ )
        {
            Rectangle alienRect = new Rectangle( xOfAliens.get( i ), yOfAliens.get( i ), UNIT_SIZE, UNIT_SIZE );
            Rectangle shipRect = new Rectangle( xOfShip[0], SCREEN_HEIGHT - UNIT_SIZE, UNIT_SIZE, UNIT_SIZE );

            // Check for collision with the ship
            boolean isCollision = alienRect.intersects( shipRect );
            boolean isBottom = yOfAliens.get( i ) >= SCREEN_HEIGHT - UNIT_SIZE;
            if ( isCollision || isBottom )
            {
                if ( isCollision )
                {
                    explosions.add(
                        new Explosion( new Point( xOfAliens.get( i ), yOfAliens.get( i ) ), explosionDuration ) );
                }

                // If aliens reach the ship or the bottom of the screen, it's game over
                lives = 0;
                gameOver();
                break;
            }
        }
    }

    /**
     * Updates the score based on which aliens have been shot.
     */
    void score()
    {
        if ( smallAlienShot )
            score += 30;

        else if ( mediumAlienShot )
            score += 20;

        else if ( bigAlienShot )
            score += 10;

        // Update the high score
        highScore = Math.max( highScore, score );
        if ( score > highScore )
            Helpers.writeHighScore( highScore );
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
    void gameOver()
    {
        // Set the game over state
        isGameOver = true;

        ALIEN_DELAY = RESET_DELAY;

        ufoTimer.stop();
        ufoActive = false;
        alienTimer.stop();
        timer.stop();

        // Update the high score
        highScore = Math.max( highScore, score );
        if ( score > highScore )
            Helpers.writeHighScore( highScore );

        setupReplayButton();
    }

    private void drawGameOverScreen( Graphics g )
    {
        // Display game over text and scores
        drawCenteredText( g, "Game Over", LARGE_FONT, SCREEN_HEIGHT / 3 );
        drawCenteredText( g, "High Score: " + highScore, MEDIUM_FONT, SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() );
        drawCenteredText( g, "Score: " + score, MEDIUM_FONT,
                          SCREEN_HEIGHT / 3 + LARGE_FONT.getSize() + MEDIUM_FONT.getSize() + 20 );
    }

    /**
     * Handles game over logic, including displaying scores and high scores.
     *
     * @param g The graphics context used for drawing.
     */
    void gameWon()
    {
        // Alien delay is reduced to increase difficulty
        ALIEN_DELAY = Math.max( 100, ALIEN_DELAY - 50 );

        // Reset game state variables
        aliensKilled = 0;
        shipDirection = ' ';
        directionQueue.clear();
        shipMoving = true;
        alienShooting = true;
        shipShooting = true;

        alienTimer.stop();
        ufoTimer.restart();
        ufoActive = false;
        timer.stop();

        // Reset bullets
        shipBullet.clear();
        alienBullet.clear();

        // Reinitialize alien positions
        initAliens();
        initAlienTimer();

        // Reset the ship's position
        xOfShip[0] = ( SCREEN_WIDTH / 2 ) - ( UNIT_SIZE / 2 );

        // Update the high score
        highScore = Math.max( highScore, score );
        Helpers.writeHighScore( highScore );

        // Start or restart the game timers
        if ( timer != null )
            timer.stop();

        timer = new Timer( DELAY, this );
        timer.start();
        repaint();
    }

    /**
     * Handles action events during gameplay such as moving the ship or firing
     * bullets.
     *
     * @param e The action event to be processed.
     */
    @Override public void actionPerformed( ActionEvent e )
    {
        // Handle UFO movement
        if ( ufoActive )
        {
            ufoX += ufoSpeed;
            if ( ufoX > SCREEN_WIDTH )
            { // UFO disappears after crossing the screen
                ufoActive = false;
            }
        }

        // Ship movement and bullet firing
        if ( shipMoving )
        {
            moveShip();
        }
        if ( shipShooting && shipBullet.isEmpty() )
        {
            bulletsFromShip();
        }

        // Alien movement and bullet firing
        moveBullets();
        bulletsFromAliens();
        checkCollisions();

        // Update explosion timers
        Iterator<Explosion> expIterator = explosions.iterator();
        while ( expIterator.hasNext() )
        {
            Explosion exp = expIterator.next();
            exp.timer--;
            if ( exp.timer <= 0 )
            {
                expIterator.remove(); // Remove explosion once finished
            }
        }

        repaint();
    }

    /**
     * Resets the game to its initial state, ready to start anew.
     */
    void restartGame()
    {
        isGameOver = false;
        lives = 3;
        score = 0;
        aliensKilled = 0;

        // Clear the alien positions lists
        xOfAliens.clear();
        yOfAliens.clear();

        // Reset bullets
        shipBullet.clear();
        alienBullet.clear();

        // Reset game state variables
        shipDirection = ' ';
        directionQueue.clear();
        shipMoving = true;
        alienShooting = true;

        // Reinitialize alien positions
        initAliens();
        initAlienTimer();

        ufoTimer.restart();
        ufoActive = false;

        // Reset the ship's position
        xOfShip[0] = ( SCREEN_WIDTH / 2 ) - ( UNIT_SIZE / 2 );

        // Disable the replay button until the game is over
        replayButton.setEnabled( false );
        replayButton.setVisible( false );

        // Start or restart the game timers
        if ( timer != null )
            timer.stop();

        timer = new Timer( DELAY, this );
        timer.start();

        repaint();
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
}
