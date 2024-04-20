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
     * Constructor for the GamePanel class.
     * Initializes the game panel with necessary components and settings.
     *
     * This constructor sets up the game panel with the following components:
     * - Random object for generating random numbers
     * - Preferred size of the panel
     * - Background color
     * - Double buffering for smooth rendering
     * - Focusable for key events
     * - Key listener for handling key events
     * - Layout set to null for custom positioning
     * - Key listener setup for handling key presses and releases
     * - Centering the ship horizontally on the screen
     * - Replay button setup with text, font, action listener, position, and size
     * - Initialization of aliens, alien timer, UFO timer, and high score
     * - Starting the game and playing background music
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

    /**
     * Updates the movement and shooting status of the ship based on the keys that are currently pressed.
     * Sets the shipMoving flag to true if the left or right arrow keys are pressed.
     * Sets the shipShooting flag to true if the space key is pressed.
     * Sets the shipDirection to 'L' if the left arrow key is pressed, 'R' if the right arrow key is pressed.
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
        boolean changeDirection = false;

        // Move aliens left or right
        for ( int i = 0; i < xOfAliens.size(); i++ )
        {
            if ( aliensDirection == 'R' )
            {
                xOfAliens.set( i, xOfAliens.get( i ) + UNIT_SIZE / 4 ); // Move right
                // Check if any alien touches the right boundary
                if ( xOfAliens.get( i ) > SCREEN_WIDTH - ( UNIT_SIZE * 2 ) )
                {
                    changeDirection = true;
                }
            }

            else if ( aliensDirection == 'L' )
            {
                xOfAliens.set( i, xOfAliens.get( i ) - UNIT_SIZE / 4 ); // Move left
                // Check if any alien touches the left boundary
                if ( xOfAliens.get( i ) < UNIT_SIZE )
                {
                    changeDirection = true;
                }
            }
        }

        // Move aliens down and change direction if needed
        if ( changeDirection )
        {
            // Change direction of all aliens
            aliensDirection = ( aliensDirection == 'R' ) ? 'L' : 'R';
            for ( int i = 0; i < yOfAliens.size(); i++ )
            {
                // Move aliens down by UNIT_SIZE
                yOfAliens.set( i, yOfAliens.get( i ) + UNIT_SIZE );
            }
        }
    }

    /**
     * Generates bullets from the ship if the ship is shooting and there are no existing ship bullets.
     *
     * This method checks if the ship is shooting and if the ship bullet list is empty. If both conditions are met,
     * a new ship bullet is created at the center of the ship's x-coordinate and at the bottom of the screen.
     *
     * @throws IndexOutOfBoundsException if the ship bullet list is empty and shipShooting is true
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
     * This method generates bullets from aliens if the alien shooting flag is true and the alien bullet list is empty.
     * It finds the bottom aliens in each column and selects one randomly to shoot a bullet.
     *
     * @throws IndexOutOfBoundsException if the shooter index is out of bounds
     */
    void bulletsFromAliens()
    {
        if ( alienShooting && alienBullet.isEmpty() )
        {
            // Find the bottom aliens in each column
            int[] bottomAliens = new int[11];

            // Fill the array with -1 to indicate no alien in the column
            Arrays.fill( bottomAliens, -1 );

            // Find the bottom alien in each column
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

    /**
     * Moves the ship and alien bullets on the screen.
     *
     * This method iterates through the shipBullet and alienBullet lists, updating the y-coordinate of each bullet
     * based on its direction of movement. If a bullet goes off the screen, it is removed from the respective list.
     */
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
     * This method checks for collisions between ship bullets and aliens, as well as between alien bullets and the ship.
     * It also handles the logic for scoring, removing aliens and bullets upon collision, triggering explosions, and
     * updating the game state.
     *
     * @throws ConcurrentModificationException if there is a concurrent modification while iterating through the
     *     shipBullet or alienBullet lists
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
     * Updates the player's score based on the type of alien shot down and updates the high score if necessary.
     *
     * If a small alien is shot down, the player's score is increased by 30 points.
     * If a medium alien is shot down, the player's score is increased by 20 points.
     * If a big alien is shot down, the player's score is increased by 10 points.
     *
     * The high score is updated to the maximum value between the current high score and the player's score.
     * If the player's score surpasses the current high score, the high score is updated and saved to a file.
     *
     * @param smallAlienShot a boolean indicating if a small alien was shot down
     * @param mediumAlienShot a boolean indicating if a medium alien was shot down
     * @param bigAlienShot a boolean indicating if a big alien was shot down
     * @param score the player's current score
     * @param highScore the current high score
     * @throws IOException if there is an error writing the high score to a file
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
     * This method is called when an action event occurs, such as a button click or menu selection.
     * It handles the movement of the UFO, ship, bullets, aliens, and explosions in the game.
     *
     * @param e The ActionEvent that triggered this method
     * @return void
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
