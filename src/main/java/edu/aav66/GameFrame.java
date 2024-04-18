package edu.aav66;

import javax.swing.JFrame;

public class GameFrame extends JFrame
{
    GameFrame()
    {
        // Add an instance of GamePanel to this frame
        this.add( new GamePanel() );

        // Set the title of the frame to "Snake"
        this.setTitle( "Space Invaders" );

        // Ensure the application exits when the frame is closed
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        // Disable resizing of the frame to maintain consistent gameplay
        this.setResizable( false );

        // Pack the components within the frame
        this.pack();

        // Make the frame visible to the user
        this.setVisible( true );

        // Position the frame in the center of the screen
        this.setLocationRelativeTo( null );
    }
}
