package edu.aav66;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements ActionListener
{
    static final int SCREEN_WIDTH = 672;  // 224 * 3
    static final int SCREEN_HEIGHT = 614; // 256 * 3 (-20% for score and lives display)
    static final int UNIT_SIZE = 24;
    static final int GAME_UNITS = ( SCREEN_WIDTH * SCREEN_HEIGHT ) / UNIT_SIZE;

    // Color Constants
    static final Color BACKGROUND_COLOR = Color.black;
    static final Color SCORE_COLOR = new Color( 251, 251, 251 );
    static final Color GAME_OVER_COLOR = new Color( 175, 35, 39 );
    static final Color SHIP_COLOR = new Color( 64, 224, 240 );
    static final Color SHIP_BULLET_COLOR = Color.gray;
    static final Color LARGE_ALIEN_COLOR = new Color( 106, 168, 79 );
    static final Color MEDIUM_ALIEN_COLOR = new Color( 184, 134, 11 );
    static final Color SMALL_ALIEN_COLOR = new Color( 255, 69, 0 );
    static final Color ALIEN_BULLET_COLOR = new Color( 199, 21, 133 );

    // Bullet Dimensions
    static final int BULLET_HEIGHT = 12;
    static final int BULLET_WIDTH = 3;

    // Bullet Dimensions
    static final int ALIEN_BULLET_HEIGHT = 14;
    static final int ALIEN_BULLET_WIDTH = 8;

    // Bullet Deques
    static Deque<Integer> shipBullet = new ArrayDeque<>();
    static Deque<Integer> alienBullet = new ArrayDeque<>();

    // Ship and Bullet Coordinates
    public final static int[] xOfShip = new int[GAME_UNITS];

    public final int[] xOfShipBullet = new int[GAME_UNITS];
    public final int[] yOfShipBullet = new int[GAME_UNITS];

    public final int[] xOfAlienBullet = new int[GAME_UNITS];
    public final int[] yOfAlienBullet = new int[GAME_UNITS];

    private long lastAlienShotTime = System.currentTimeMillis();
    static final long MIN_ALIEN_SHOT_STAGGER_TIME = 400;
    static final long MAX_ALIEN_SHOT_STAGGER_TIME = 800;
    private final Random randomShots = new Random();

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
    static boolean isGameOver = false;
    static int difficultyMultiplier = 0;

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
    private int ufoSpeed = 3;
    static boolean ufoActive = false;
    final static int UFO_INTERVAL = 20000; // 20 seconds

    // Processes
    Random random;

    // List to hold explosion details
    List<Explosion> explosions = new ArrayList<>();
    int explosionDuration = 10; // frames

    // List to hold shelters
    static List<Shelter> shelters = new ArrayList<>();

    /**
     * Constructor for the GamePanel class. Initializes the game panel with necessary components.
     * This constructor sets up the game panel with a random object for number generation, sets the preferred size,
     * background color, enables double buffering for smooth rendering, and makes the panel focusable.
     * It also sets up key listeners for handling game controls, centers the ship, initializes game elements like
     * aliens, shelters, and the replay button, and starts the game timers.
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

        GameState.initShelters();

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

    /**
     * Updates the movement and shooting status of the ship based on currently pressed keys.
     * Sets the ship's moving and shooting flags and updates the direction to left or right accordingly.
     */
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
     * This method overrides the paintComponent method from the superclass to paint the game components on the screen.
     * It first calls the superclass's paintComponent method to clear the screen and then proceeds to draw the game
     * elements. It also calls the GameState class to draw any UI elements on the screen. If the game is over, it
     * displays the game over screen using the GameState class.
     *
     * @param g the Graphics object used to draw on the screen
     */
    @Override public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        draw( g );
        drawShelters( g );
        GameState.UIelements( g, this );

        if ( isGameOver )
            GameState.drawGameOverScreen( g, this );
    }

    /**
     * This method is responsible for drawing the game elements on the screen using the provided Graphics object.
     * It draws the player's ship, aliens, UFO, ship bullets, alien bullets, and explosions.
     *
     * @param g The Graphics object used for drawing on the screen
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

            // Determine the type of alien based on the row
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
            index
            -> g.fillRect( xOfAlienBullet[index], yOfAlienBullet[index], ALIEN_BULLET_WIDTH, ALIEN_BULLET_HEIGHT ) );

        // Draw explosions
        for ( Explosion exp : explosions )
        {
            if ( exp.timer > 0 )
            {
                g.setColor( Color.orange );
                g.fillOval( exp.location.x, exp.location.y, UNIT_SIZE, UNIT_SIZE ); // Simple explosion effect
            }
        }

        // Draw shelters
        g.setColor( new Color( 34, 177, 76 ) ); // Shelter color
        for ( Shelter shelter : shelters )
        {
            if ( !shelter.isDestroyed() )
            {
                g.fillRect( shelter.bounds.x, shelter.bounds.y, shelter.bounds.width, shelter.bounds.height );
            }
        }
    }

    /**
     * Draws the shelters on the screen with different colors based on their health status.
     * If a shelter is not destroyed, it will be drawn with a color representing its health status.
     * Additionally, if a shelter has less than 10 hit points, cracks will be drawn on it for visual effect.
     *
     * @param g the Graphics object used for drawing on the screen
     */
    void drawShelters( Graphics g )
    {
        for ( Shelter shelter : shelters )
        {
            if ( !shelter.isDestroyed() )
            {
                Color shelterColor;
                if ( shelter.hitPoints > 6 )
                {
                    shelterColor = new Color( 34, 177, 76 ); // Healthy color
                }
                else if ( shelter.hitPoints > 3 )
                {
                    shelterColor = new Color( 139, 69, 19 ); // Moderate damage color
                }
                else
                {
                    shelterColor = new Color( 105, 105, 105 ); // Severely damaged color
                }

                g.setColor( shelterColor );
                g.fillRect( shelter.bounds.x, shelter.bounds.y, shelter.bounds.width, shelter.bounds.height );

                // Additional visual effects for damage
                if ( shelter.hitPoints < 10 )
                {
                    drawCracks( g, shelter );
                }
            }
        }
    }

    /**
     * Draws cracks on the shelter using the given Graphics object.
     *
     * @param g the Graphics object to use for drawing
     * @param shelter the Shelter object representing the shelter to draw cracks on
     */
    void drawCracks( Graphics g, Shelter shelter )
    {
        g.setColor( Color.DARK_GRAY );
        // Example: Draw simple lines for cracks
        g.drawLine( shelter.bounds.x, shelter.bounds.y, shelter.bounds.x + shelter.bounds.width,
                    shelter.bounds.y + shelter.bounds.height );
        if ( shelter.hitPoints < 7 )
        {
            g.drawLine( shelter.bounds.x, shelter.bounds.y + shelter.bounds.height,
                        shelter.bounds.x + shelter.bounds.width, shelter.bounds.y );
        }
    }

    /**
     * Moves the ship in the specified direction by one unit size.
     *
     * This method checks if the ship is currently moving and then moves the ship in the specified direction by one unit
     * size. If the ship is moving left ('L'), the x-coordinate of the ship is decremented by the unit size. If the
     * resulting x-coordinate is less than 0, it is set to 0 to prevent the ship from moving off the screen to the left.
     * If the ship is moving right ('R'), the x-coordinate of the ship is incremented by the unit size. If the resulting
     * x-coordinate is greater than the screen width minus the unit size, it is set to the screen width minus the unit
     * size to prevent the ship from moving off the screen to the right.
     */
    void moveShip()
    {
        int shipMovementSpeed = UNIT_SIZE / 8; // Reduced speed
        if ( shipMoving )
        {
            if ( shipDirection == 'L' )
            {
                xOfShip[0] -= shipMovementSpeed;
                if ( xOfShip[0] < 0 )
                {
                    xOfShip[0] = 0;
                }
            }
            else if ( shipDirection == 'R' )
            {
                xOfShip[0] += shipMovementSpeed;
                if ( xOfShip[0] > SCREEN_WIDTH - UNIT_SIZE )
                {
                    xOfShip[0] = SCREEN_WIDTH - UNIT_SIZE;
                }
            }
        }
    }

    /**
     * Moves the aliens in the game based on their current direction.
     * If any alien reaches the boundary, changes the direction of all aliens and moves them down.
     *
     * @param xOfAliens a list of x-coordinates of the aliens
     * @param yOfAliens a list of y-coordinates of the aliens
     * @param aliensDirection the current direction of the aliens (either 'R' for right or 'L' for left)
     * @param SCREEN_WIDTH the width of the game screen
     * @param UNIT_SIZE the size of each unit in the game
     */
    static void moveAliens()
    {
        int alienMovementSpeed = UNIT_SIZE / 20; // Smaller step size
        boolean changeDirection = false;

        for ( int i = 0; i < xOfAliens.size(); i++ )
        {
            if ( aliensDirection == 'R' )
            {
                xOfAliens.set( i, xOfAliens.get( i ) + alienMovementSpeed );
                if ( xOfAliens.get( i ) > SCREEN_WIDTH - ( UNIT_SIZE * 2 ) )
                {
                    changeDirection = true;
                }
            }
            else if ( aliensDirection == 'L' )
            {
                xOfAliens.set( i, xOfAliens.get( i ) - alienMovementSpeed );
                if ( xOfAliens.get( i ) < UNIT_SIZE )
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
                yOfAliens.set( i, yOfAliens.get( i ) + UNIT_SIZE );
            }
        }
    }

    /**
     * Generates a bullet from the ship if it is currently shooting and there are no existing bullets.
     * The bullet is created at the center of the ship's x-coordinate and at the bottom of the screen.
     */
    void bulletsFromShip()
    {
        if ( shipShooting && shipBullet.isEmpty() )
        {
            int bulletIndex = shipBullet.size();                         // Get the index of the bullet
            xOfShipBullet[bulletIndex] = xOfShip[0] + UNIT_SIZE / 2 - 2; // Center the bullet
            yOfShipBullet[bulletIndex] = SCREEN_HEIGHT - UNIT_SIZE;      // Bottom of the screen
            shipBullet.addLast( bulletIndex );                           // Add the bullet to the list
        }
    }

    /**
     * Generates bullets from aliens based on a staggered timing mechanism if the alien shooting flag is true.
     * Selects a random alien from the lowest in each column to shoot if fewer than two alien bullets are on screen.
     */
    void bulletsFromAliens()
    {
        // Allow two bullets on the screen, staggered shots handled with time checking
        if ( alienBullet.size() < 2 )
        {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastShot = currentTime - lastAlienShotTime;

            if ( timeSinceLastShot >= MIN_ALIEN_SHOT_STAGGER_TIME )
            {
                createAlienBullet();
                lastAlienShotTime =
                    currentTime + getRandomDelay( MIN_ALIEN_SHOT_STAGGER_TIME, MAX_ALIEN_SHOT_STAGGER_TIME );
            }
        }
    }

    /**
     * Creates a bullet for the alien to shoot.
     *
     * This method determines the lowest alien in each column and selects one of them as the shooter.
     * It then calculates the position of the bullet based on the shooter's position and adds it to the list of alien
     * bullets.
     *
     * @throws IndexOutOfBoundsException if the bullet index exceeds the limit of GAME_UNITS
     */
    private void createAlienBullet()
    {
        int[] bottomAliens = new int[11];
        Arrays.fill( bottomAliens, -1 ); // Initialize all columns with -1

        // Determine the lowest alien in each column
        for ( int i = 0; i < xOfAliens.size(); i++ )
        {
            int col = i % 11;
            int currentY = yOfAliens.get( i );
            if ( currentY > ( bottomAliens[col] != -1 ? yOfAliens.get( bottomAliens[col] ) : -1 ) )
            {
                bottomAliens[col] = i;
            }
        }

        List<Integer> shooters =
            Arrays.stream( bottomAliens ).filter( index -> index != -1 ).boxed().collect( Collectors.toList() );

        if ( !shooters.isEmpty() )
        {
            int shooterIndex = shooters.get( random.nextInt( shooters.size() ) );
            int bulletIndex = alienBullet.isEmpty() ? 0 : Collections.max( alienBullet ) + 1;
            if ( bulletIndex < GAME_UNITS )
            { // Ensure we do not exceed the limit
                xOfAlienBullet[bulletIndex] = xOfAliens.get( shooterIndex ) + UNIT_SIZE / 2 - ALIEN_BULLET_WIDTH / 2;
                yOfAlienBullet[bulletIndex] = yOfAliens.get( shooterIndex ) + UNIT_SIZE;
                alienBullet.add( bulletIndex );
            }
        }
    }

    /**
     * Generates a random delay between the specified minimum and maximum values.
     *
     * @param min the minimum delay value
     * @param max the maximum delay value
     * @return a random delay value between min and max
     */
    private long getRandomDelay( long min, long max )
    {
        // Ensure that this provides a random delay that allows two bullets to coexist
        long delay = randomShots.nextLong( max - min + 1 ) + min;
        return delay;
    }

    /**
     * Resets the cooldown for alien shooting.
     *
     * If you're keeping track of the last shot time to manage shooting frequency,
     * this method updates the lastAlienShotTime to the current system time.
     *
     * If there's a stagger time or delay for alien shots, this method resets it to a default value.
     */
    public void resetAlienShootCooldown()
    {
        this.lastAlienShotTime = System.currentTimeMillis();
        // If there's a stagger time or delay for alien shots, reset that too
        // this.alienShootStaggerTime = someDefaultValue;
    }

    /**
     * Moves the ship and alien bullets on the screen.
     *
     * This method iterates through the shipBullet and alienBullet lists, updating the y-coordinate of each bullet
     * based on its direction of movement. If a bullet goes off the screen, it is removed from the respective list.
     */
    void moveBullets()
    {
        int shipBulletSpeed = UNIT_SIZE / 2;
        int alienBulletSpeed = UNIT_SIZE / 5;

        Iterator<Integer> shipIterator = shipBullet.iterator();
        while ( shipIterator.hasNext() )
        {
            int index = shipIterator.next();
            yOfShipBullet[index] -= shipBulletSpeed;
            if ( yOfShipBullet[index] < 0 )
            {
                shipIterator.remove();
            }
        }

        Iterator<Integer> alienIterator = alienBullet.iterator();
        while ( alienIterator.hasNext() )
        {
            int index = alienIterator.next();
            yOfAlienBullet[index] += alienBulletSpeed;
            if ( yOfAlienBullet[index] > SCREEN_HEIGHT )
            {
                alienIterator.remove();
            }
        }
    }

    /**
     * This method checks for collisions between ship bullets and aliens, as well as between alien bullets and the ship.
     * It also handles the logic for scoring, removing aliens and bullets upon collision, triggering explosions, and
     * updating the game state.
     *
     * @throws ConcurrentModificationException if there is a concurrent modification while iterating through the
     *     shipBullet or alienBullet lists
     */
    void checkCollisions()
    {
        boolean continueProcessing;
        Iterator<Integer> shipBulletIterator = shipBullet.iterator();

        while ( shipBulletIterator.hasNext() )
        {
            int index = shipBulletIterator.next();
            Rectangle bulletRect =
                new Rectangle( xOfShipBullet[index], yOfShipBullet[index], BULLET_WIDTH, BULLET_HEIGHT );
            continueProcessing = true; // Reset flag for each bullet

            for ( int i = 0; i < xOfAliens.size() && continueProcessing; i++ )
            {
                Rectangle alienRect = new Rectangle( xOfAliens.get( i ), yOfAliens.get( i ), UNIT_SIZE, UNIT_SIZE );
                if ( bulletRect.intersects( alienRect ) )
                {
                    handleAlienCollision( i, shipBulletIterator );
                    continueProcessing = false; // Set flag to false to skip further processing
                }
            }

            if ( continueProcessing && ufoActive )
            {
                checkUfoCollision( shipBulletIterator, bulletRect );
            }
        }

        checkAlienBulletCollisions();
        checkBulletCollisions();
        checkShelterCollisions();
    }

    /**
     * Checks for collisions between ship bullets and alien bullets.
     * If a collision is detected, removes the ship bullet, creates an explosion, and resets the ship bullet's position.
     *
     * @throws NoSuchElementException if shipBulletIterator is empty
     */
    void checkBulletCollisions()
    {
        Iterator<Integer> shipBulletIterator = shipBullet.iterator();

        while ( shipBulletIterator.hasNext() )
        {
            Integer shipIndex = shipBulletIterator.next();
            Rectangle shipBulletRect =
                new Rectangle( xOfShipBullet[shipIndex], yOfShipBullet[shipIndex], BULLET_WIDTH, BULLET_HEIGHT );

            for ( Integer alienIndex : alienBullet )
            {
                Rectangle alienBulletRect = new Rectangle( xOfAlienBullet[alienIndex], yOfAlienBullet[alienIndex],
                                                           ALIEN_BULLET_WIDTH, ALIEN_BULLET_HEIGHT );

                if ( shipBulletRect.intersects( alienBulletRect ) )
                {
                    shipBulletIterator.remove();
                    // Explode both bullets
                    explosions.add( new Explosion( new Point( xOfShipBullet[shipIndex], yOfShipBullet[shipIndex] ),
                                                   explosionDuration ) );
                    xOfShipBullet[shipIndex] = 0;
                    yOfShipBullet[shipIndex] = 0;
                    break;
                }
            }
        }
    }

    /**
     * Handles the collision between the player's ship bullet and an alien at the specified index.
     * Triggers an explosion at the alien's position, removes the alien from the list of aliens,
     * removes the ship bullet from the iterator, scores the shot based on the alien's type,
     * updates the player's score and high score, checks if all aliens are defeated, and adjusts
     * the game difficulty based on the number of remaining aliens.
     *
     * @param alienIndex The index of the alien that was hit by the player's ship bullet
     * @param shipBulletIterator An iterator for the list of ship bullets to remove the bullet after processing
     */
    void handleAlienCollision( int alienIndex, Iterator<Integer> shipBulletIterator )
    {
        // Trigger explosion
        explosions.add(
            new Explosion( new Point( xOfAliens.get( alienIndex ), yOfAliens.get( alienIndex ) ), explosionDuration ) );

        // Remove the alien from the list
        xOfAliens.remove( alienIndex );
        yOfAliens.remove( alienIndex );
        shipBulletIterator.remove(); // Remove the bullet after processing

        // Score the shot based on alien's type before removal
        if ( alienIndex < 11 )
        {
            score += 30; // Assume small alien score
        }
        else if ( alienIndex < 33 )
        {
            score += 20; // Medium alien
        }
        else
        {
            score += 10; // Large alien
        }

        // Update the high score
        highScore = Math.max( highScore, score );
        if ( score > highScore )
            Helpers.writeHighScore( highScore );

        // Check if all aliens are defeated
        if ( xOfAliens.isEmpty() )
        {
            GameState.gameWon( this );
            return;
        }

        // Adjust game dynamics based on the number of remaining aliens
        adjustGameDifficulty();
    }

    /**
     * Adjusts the game difficulty based on the number of remaining aliens.
     * Decreases the alien movement delay if the total number of aliens is less than a certain threshold.
     * Updates the alien movement delay and restarts the timer if the delay has changed.
     *
     * @param xOfAliens a list containing the x-coordinates of all aliens
     * @param difficultyMultiplier a multiplier to adjust the difficulty of the game
     * @throws IllegalArgumentException if xOfAliens is null or empty
     */
    void adjustGameDifficulty()
    {
        int totalAliens = xOfAliens.size();
        double speedIncreaseThreshold = 55 / 1.58; // Approximately 34.8 aliens
        int currentDelay = GameState.ALIEN_MOVEMENT_DELAY;

        // Decrease delay based on remaining aliens
        if ( totalAliens <= ( 55 - speedIncreaseThreshold ) )
        { // 20.2 aliens
            if ( GameState.ALIEN_MOVEMENT_GAMEWON_DELAY == 80 )
                GameState.ALIEN_MOVEMENT_DELAY = Math.max( GameState.ALIEN_MOVEMENT_DELAY - 4, 3 );
            if ( GameState.ALIEN_MOVEMENT_GAMEWON_DELAY == 60 )
                GameState.ALIEN_MOVEMENT_DELAY = Math.max( GameState.ALIEN_MOVEMENT_DELAY - 3, 3 );
            if ( GameState.ALIEN_MOVEMENT_GAMEWON_DELAY == 40 )
                GameState.ALIEN_MOVEMENT_DELAY = Math.max( GameState.ALIEN_MOVEMENT_DELAY - 2, 3 );
            if ( GameState.ALIEN_MOVEMENT_GAMEWON_DELAY == 20 )
                GameState.ALIEN_MOVEMENT_DELAY = Math.max( GameState.ALIEN_MOVEMENT_DELAY - 1, 3 );
        }

        System.out.println( "\nAliens remaining: " + xOfAliens.size() );
        System.out.println( "CURRENT DELAY: " + GameState.ALIEN_MOVEMENT_DELAY );
        System.out.println( "GAME WON DELAY: " + GameState.ALIEN_MOVEMENT_GAMEWON_DELAY );
        System.out.println( "DIFFICULTY MULTIPLIER: " + difficultyMultiplier );

        // Apply delay change and restart timer if delay has changed
        if ( currentDelay != GameState.ALIEN_MOVEMENT_DELAY )
        {
            GameState.alienTimer.setDelay( GameState.ALIEN_MOVEMENT_DELAY );
            GameState.alienTimer.restart();
        }
    }

    /**
     * Checks for collision between the ship's bullets and the UFO.
     * If a collision is detected, triggers an explosion, updates the score, deactivates the UFO,
     * and removes the bullet.
     *
     * @param shipBulletIterator An iterator for the ship's bullets
     * @param bulletRect The rectangle representing the ship's bullet
     */
    void checkUfoCollision( Iterator<Integer> shipBulletIterator, Rectangle bulletRect )
    {
        Rectangle ufoRect = new Rectangle( ufoX, ufoY, UNIT_SIZE * 2, UNIT_SIZE );
        if ( bulletRect.intersects( ufoRect ) )
        {
            // Trigger explosion
            explosions.add( new Explosion( new Point( ufoX + UNIT_SIZE, ufoY ), explosionDuration ) );

            // UFO is hit, random score for hitting UFO
            int[] possibleScores = { 50, 100, 150, 200, 300 };
            score += possibleScores[random.nextInt( possibleScores.length )];
            ufoActive = false;           // Deactivate UFO
            shipBulletIterator.remove(); // Remove the bullet
        }
    }

    /**
     * Checks for collision between the alien's bullets and the ship.
     * If a collision is detected, triggers an explosion for the ship, removes the bullet,
     * subtracts a life, and triggers game over if no lives remain.
     */
    void checkAlienBulletCollisions()
    {
        Iterator<Integer> alienBulletIterator = alienBullet.iterator();
        while ( alienBulletIterator.hasNext() )
        {
            int index = alienBulletIterator.next();
            Rectangle bulletRect =
                new Rectangle( xOfAlienBullet[index], yOfAlienBullet[index], ALIEN_BULLET_HEIGHT, ALIEN_BULLET_WIDTH );
            Rectangle shipRect = new Rectangle( xOfShip[0], SCREEN_HEIGHT - UNIT_SIZE, UNIT_SIZE, UNIT_SIZE );

            if ( bulletRect.intersects( shipRect ) )
            {
                // Trigger explosion for ship hit
                explosions.add(
                    new Explosion( new Point( xOfShip[0], SCREEN_HEIGHT - UNIT_SIZE ), explosionDuration ) );

                // Collision detected, remove the bullet and subtract a life
                alienBulletIterator.remove();
                lives--;
                if ( lives <= 0 )
                {
                    GameState.gameOver();
                }
            }
        }
    }

    /**
     * Checks for collisions between bullets and shelters, and updates the game state accordingly.
     *
     * This method iterates through each shelter in the game and checks for collisions with both ship bullets and alien
     * bullets. If a collision is detected, the shelter takes damage and an explosion is created at the location of the
     * bullet.
     *
     * @throws NullPointerException if any of the required objects (shelters, shipBullet, alienBullet) are null
     */
    void checkShelterCollisions()
    {
        List<Integer> bulletsToRemove = new ArrayList<>();
        for ( Shelter shelter : shelters )
        {
            if ( !shelter.isDestroyed() )
            {
                Iterator<Integer> shipBulletIterator = shipBullet.iterator();
                while ( shipBulletIterator.hasNext() )
                {
                    int bulletIndex = shipBulletIterator.next();
                    Rectangle bulletRect = new Rectangle( xOfShipBullet[bulletIndex], yOfShipBullet[bulletIndex],
                                                          BULLET_WIDTH, BULLET_HEIGHT );
                    if ( bulletRect.intersects( shelter.bounds ) )
                    {
                        shelter.takeDamage( this );
                        explosions.add(
                            new Explosion( new Point( xOfShipBullet[bulletIndex] - ( ( UNIT_SIZE / 3 ) + 2 ),
                                                      yOfShipBullet[bulletIndex] ),
                                           explosionDuration ) );
                        bulletsToRemove.add( bulletIndex );
                    }
                }
                shipBullet.removeAll( bulletsToRemove );

                Iterator<Integer> alienBulletIterator = alienBullet.iterator();
                while ( alienBulletIterator.hasNext() )
                {
                    int bulletIndex = alienBulletIterator.next();
                    Rectangle bulletRect = new Rectangle( xOfAlienBullet[bulletIndex], yOfAlienBullet[bulletIndex],
                                                          ALIEN_BULLET_WIDTH, ALIEN_BULLET_HEIGHT );
                    if ( bulletRect.intersects( shelter.bounds ) )
                    {
                        shelter.takeDamage( this );
                        explosions.add(
                            new Explosion( new Point( xOfAlienBullet[bulletIndex], yOfAlienBullet[bulletIndex] ),
                                           explosionDuration ) );
                        alienBulletIterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Responds to action events within the game such as timer ticks, handling movements of the UFO, ship, and bullets.
     * Manages alien movements and firing, checks for collisions, and updates the state of explosions.
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
     * MyKeyAdapter is a class that extends KeyAdapter and handles key events for a game.
     * It provides methods for handling key presses and releases to control the game's ship movement and shooting.
     */
    public class MyKeyAdapter extends KeyAdapter
    {
        /**
         * keyPressed is a method that is called when a key is pressed.
         * It checks the key code and updates the direction queue accordingly.
         * If the space key is pressed, it triggers shooting bullets from the ship.
         *
         * @param e The KeyEvent object representing the key event
         */
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

        /**
         * Clears the direction queue and stops the ship from moving and shooting when a key is released.
         *
         * @param e The KeyEvent object representing the key that was released
         * @throws NullPointerException if the KeyEvent object is null
         */
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
