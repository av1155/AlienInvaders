package edu.aav66;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javazoom.jl.player.Player;

class Helpers
{
    private static final String resourcesPath = "/Users/andreaventi/Developer/AlienInvaders/src/main/resources/";
    private static final String trackPath = resourcesPath + "DRIVE(chosic.com).mp3";

    private static String highScorePath;
    private static final String devPath = resourcesPath + "highscore.txt";
    private static final String prodPath = "highscore.txt";

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
