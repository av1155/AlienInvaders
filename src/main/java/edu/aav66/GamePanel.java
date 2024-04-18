package edu.aav66;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The main game panel for the Alien Invaders game.
 * This class handles game initialization, input handling, game logic, and rendering.
 * It extends JPanel and implements ActionListener to handle game events and actions.
 */
public class GamePanel extends JPanel implements ActionListener
{
    static final int SCREEN_WIDTH = 672;  // 224 * 3
    static final int SCREEN_HEIGHT = 768; // 256 * 3
    static final int UNIT_SIZE = 28;
    static final int GAME_UNITS = ( SCREEN_WIDTH * SCREEN_HEIGHT ) / UNIT_SIZE;

    // Color Constants
    static final Color BACKGROUND_COLOR = Color.black;
    static final Color SCORE_COLOR = Color.white;
    static final Color SHIP_COLOR = Color.white;
    static final Color SHIP_BULLET_COLOR = Color.gray;
    static final Color LARGE_ALIEN_COLOR = new Color( 106, 168, 79 );
    static final Color MEDIUM_ALIEN_COLOR = new Color( 184, 134, 11 );
    static final Color SMALL_ALIEN_COLOR = new Color( 255, 69, 0 );
    static final Color ALIEN_BULLET_COLOR = new Color( 199, 21, 133 );

    // Bullet Dimensions
    static final int BULLET_HEIGHT = 12;
    static final int BULLET_WIDTH = 6;

    // Bullet Deques
    static Deque<Integer> shipBullet = new ArrayDeque<>();
    static Deque<Integer> alienBullet = new ArrayDeque<>();

    // Ship and Bullet Coordinates
    public final static int[] xOfShip = new int[GAME_UNITS];

    public final int[] xOfShipBullet = new int[GAME_UNITS];
    public final int[] yOfShipBullet = new int[GAME_UNITS];

    public final int[] xOfAlienBullet = new int[GAME_UNITS];
    public final int[] yOfAlienBullet = new int[GAME_UNITS];

    // Alien Coordinates
    static List<Integer> xOfAliens = new ArrayList<>();
    static List<Integer> yOfAliens = new ArrayList<>();

    // Ship Movement Variables
    static Deque<Character> directionQueue = new ArrayDeque<>();
    private Set<Integer> pressedKeys = new HashSet<>();

    // GAME STATE VARIABLES
    static int lives = 3;
    static int score = 0;
    static int highScore = 0;
    static int aliensKilled = 0;
    static boolean isGameOver = false;

    // Ship, Alien, and UFO Attributes:
    // Ship:
    static char shipDirection = ' ';
    static boolean shipMoving = false;
    static boolean shipShooting = false;
    boolean shipShot = false;

    // Alien:
    static char aliensDirection = 'R';
    static boolean aliensMoving = true;
    static boolean alienShooting = false;
    boolean smallAlienShot = false;
    boolean mediumAlienShot = false;
    boolean bigAlienShot = false;

    // UFO:
    static int ufoX;
    private int ufoY = 50;
    private int ufoSpeed = 2;
    static boolean ufoActive = false;
    final static int UFO_INTERVAL = 20000; // 20 seconds

    // Processes
    Random random;

    // List to hold explosion details
    List<Explosion> explosions = new ArrayList<>();
    int explosionDuration = 10; // frames

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
        GameState.replayButton = new JButton( "Replay" );
        GameState.replayButton.setFont( new Font( "Futura", Font.BOLD, 20 ) );
        GameState.replayButton.addActionListener( e -> GameState.restartGame( this ) );
        int buttonWidth = 150;
        int buttonHeight = 50;
        int buttonX = ( SCREEN_WIDTH - buttonWidth ) / 2;
        int buttonY = SCREEN_HEIGHT - 120;
        GameState.replayButton.setBounds( buttonX, buttonY, buttonWidth, buttonHeight );
        GameState.replayButton.setEnabled( false );
        GameState.replayButton.setFocusable( true );
        this.add( GameState.replayButton );

        GameState.initAliens();
        GameState.initAlienTimer( this );
        GameState.initUfoTimer( this );
        highScore = Helpers.initializeHighScore( highScore );
        GameState.startGame( this );
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
        GameState.UIelements( g, this );

        if ( isGameOver )
            GameState.drawGameOverScreen( g, this );
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
    static void moveAliens()
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
            int bulletIndex = shipBullet.size();
            xOfShipBullet[bulletIndex] = xOfShip[0] + UNIT_SIZE / 2 - 2;
            yOfShipBullet[bulletIndex] = SCREEN_HEIGHT - UNIT_SIZE;
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
                        GameState.gameWon( this );

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
                    GameState.gameOver();
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
                GameState.gameOver();
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
