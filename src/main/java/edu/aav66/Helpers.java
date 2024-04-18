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
     * Loads sprite images from files.
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
     * Resizes an image to the given width and height.
     * @param originalImage The original image to resize.
     * @param targetWidth The desired width.
     * @param targetHeight The desired height.
     * @return A new BufferedImage instance with the specified dimensions.
     */
    public static BufferedImage resizeImage( BufferedImage originalImage, int targetWidth, int targetHeight )
    {
        BufferedImage resizedImage = new BufferedImage( targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB );
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage( originalImage, 0, 0, targetWidth, targetHeight, null );
        graphics2D.dispose();
        return resizedImage;
    }

    public static BufferedImage getUfo() { return ufo; }

    public static BufferedImage getYellowAlien() { return yellowAlien; }

    public static BufferedImage getGreenAlien() { return greenAlien; }

    public static BufferedImage getRedAlien() { return redAlien; }

    public static BufferedImage getPlayerShip() { return playerShip; }

    /**
     * Plays background music from a specified file path. This method runs
     * the music in a separate thread to ensure it does not block the GUI thread.
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
     * Initializes high score management by setting the file path and reading the high score.
     * This method determines the correct path for the high score file by checking if the
     * development path exists; if not, it uses the production path. It then reads the
     * high score from the determined file.
     */
    static int initializeHighScore( int highScore )
    {
        highScorePath = new File( devPath ).exists() ? devPath : prodPath;
        highScore = readHighScore();
        return highScore;
    }

    /**
     * Writes the current high score to the file.
     * This method attempts to write the current high score to a designated file. If an error occurs
     * during the file writing process, such as an IOException, the error is logged and the stack
     * trace is printed. This ensures that the application can gracefully handle file system issues.
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
     * Reads the high score from a file and returns it.
     * This method checks if the high score file exists and reads the high score if available.
     * If the file does not exist or no valid integer score is found, it returns 0. It handles
     * the FileNotFoundException to ensure the application remains stable if the file is
     * unexpectedly missing.
     *
     * @return The high score read from the file, or 0 if the file does not exist or contains no valid score.
     */
    static int readHighScore()
    {
        File file = new File( highScorePath );
        if ( !file.exists() )
            return 0;

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
