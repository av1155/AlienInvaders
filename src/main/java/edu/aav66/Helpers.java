package edu.aav66;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javazoom.jl.player.Player;

/**
 * This class provides utility functions to support the game,
 * including image management, music playback, and high score handling.
 */
class Helpers
{
    private static BufferedImage yellowAlien;
    private static BufferedImage redAlien;
    private static BufferedImage greenAlien;
    private static BufferedImage ufo;
    private static BufferedImage playerShip;

    private static final String resourcesPath = "/Users/andreaventi/Developer/AlienInvaders/src/main/resources/";

    private static String highScorePath;
    private static final String devPath = resourcesPath + "highscore.txt";
    private static final String prodPath = "highscore.txt";

    private static final String trackPath =
        resourcesPath + "Loyalty_Freak_Music_-_02_-_High_Technologic_Beat_Explosion(chosic.com).mp3";

    private static final String ufoPath = resourcesPath + "ufo.png";
    private static final String yellowAlienPath = resourcesPath + "yellow.png";
    private static final String greenAlienPath = resourcesPath + "green.png";
    private static final String redAlienPath = resourcesPath + "red.png";
    private static final String playerShipPath = resourcesPath + "player.png";

    static { loadSprites(); }

    /**
     * Loads and resizes sprite images from files upon class loading. Each sprite is resized according to the unit size
     * defined in GamePanel to ensure consistency with the game's scaling. If images fail to load, an error message is
     * printed and the error is logged.
     */
    static void loadSprites()
    {
        try
        {
            BufferedImage originalUfo = ImageIO.read( new File( ufoPath ) );
            BufferedImage originalYellowAlien = ImageIO.read( new File( yellowAlienPath ) );
            BufferedImage originalGreenAlien = ImageIO.read( new File( greenAlienPath ) );
            BufferedImage originalRedAlien = ImageIO.read( new File( redAlienPath ) );
            BufferedImage originalPlayerShip = ImageIO.read( new File( playerShipPath ) );

            ufo = resizeImage( originalUfo, GamePanel.UNIT_SIZE * 2, GamePanel.UNIT_SIZE );
            yellowAlien = resizeImage( originalYellowAlien, GamePanel.UNIT_SIZE, GamePanel.UNIT_SIZE );
            greenAlien = resizeImage( originalGreenAlien, GamePanel.UNIT_SIZE, GamePanel.UNIT_SIZE );
            redAlien = resizeImage( originalRedAlien, GamePanel.UNIT_SIZE, GamePanel.UNIT_SIZE );
            playerShip = resizeImage( originalPlayerShip, GamePanel.UNIT_SIZE, GamePanel.UNIT_SIZE );
        }
        catch ( IOException e )
        {
            System.err.println( "Error loading sprite images." );
            e.printStackTrace();
        }
    }

    /**
     * Resizes a given image to specified width and height using high-quality rendering settings.
     * @param originalImage The original image to be resized.
     * @param targetWidth The target width of the image.
     * @param targetHeight The target height of the image.
     * @return A new BufferedImage of the specified size, resized with attention to maintaining image quality.
     */
    public static BufferedImage resizeImage( BufferedImage originalImage, int targetWidth, int targetHeight )
    {
        BufferedImage resizedImage = new BufferedImage( targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB );
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage( originalImage, 0, 0, targetWidth, targetHeight, null );
        graphics2D.dispose();
        return resizedImage;
    }

    /**
     * Retrieves the UFO image.
     * @return The UFO BufferedImage.
     */
    public static BufferedImage getUfo() { return ufo; }

    /**
     * Retrieves the yellow alien image.
     * @return The yellow alien BufferedImage.
     */
    public static BufferedImage getYellowAlien() { return yellowAlien; }

    /**
     * Retrieves the green alien image.
     * @return The green alien BufferedImage.
     */
    public static BufferedImage getGreenAlien() { return greenAlien; }

    /**
     * Retrieves the red alien image.
     * @return The red alien BufferedImage.
     */
    public static BufferedImage getRedAlien() { return redAlien; }

    /**
     * Retrieves the player ship image.
     * @return The player ship BufferedImage.
     */
    public static BufferedImage getPlayerShip() { return playerShip; }

    /**
     * Plays background music continuously from a specified file path. The music playback runs in a separate thread to
     * prevent blocking the GUI thread. If there is an issue loading or playing the music file, it logs an error.
     * The music will loop indefinitely until the application is closed or the thread is interrupted.
     */
    static void playMusic()
    {
        new Thread( new Runnable() {
            public void run()
            {
                try
                {
                    while ( true )
                    { // Loop to allow the music to replay indefinitely
                        FileInputStream fileInputStream = new FileInputStream( trackPath );
                        Player player = new Player( fileInputStream );
                        player.play();
                        player.close();
                    }
                }
                catch ( Exception e )
                {
                    System.err.println( "Problem playing file " + trackPath );
                    e.printStackTrace();
                }
            }
        } ).start();
    }

    /**
     * Initializes and reads the high score from a file. The path of the high score file is determined based on the
     * existence of the development path file; if not found, it defaults to the production path. It returns the high
     * score read from the file or zero if the file is missing or invalid.
     * @param highScore The initial high score, typically the current high score at the start.
     * @return The high score read from the file, or 0 if the file is invalid or not found.
     */
    static int initializeHighScore( int highScore )
    {
        highScorePath = new File( devPath ).exists() ? devPath : prodPath;
        highScore = readHighScore();
        return highScore;
    }

    /**
     * Writes the high score to a designated file.
     * @param highScore The high score to be written to the file.
     */
    static void writeHighScore( int highScore )
    {
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( highScorePath ) ) )
        {
            writer.write( Integer.toString( highScore ) );
        }
        catch ( IOException e )
        {
            System.err.println( "Problem writing high score file " + highScorePath );
            e.printStackTrace();
        }
    }

    /**
     * Reads and returns the high score from the configured file path. If the file does not exist, is inaccessible, or
     * contains invalid data, it returns 0 and logs the issue.
     * @return The high score, or 0 if the file is missing or contains invalid data.
     */
    static int readHighScore()
    {
        File file = new File( highScorePath );
        if ( !file.exists() )
        {
            return 0;
        }
        try ( Scanner scanner = new Scanner( file ) )
        {
            return scanner.hasNextInt() ? scanner.nextInt() : 0;
        }
        catch ( FileNotFoundException e )
        {
            System.err.println( "High score file not found: " + highScorePath );
            e.printStackTrace();
            return 0;
        }
    }
}
