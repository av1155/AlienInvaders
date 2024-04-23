package edu.aav66;

import java.awt.Rectangle;

/**
 * Shelter represents a protective structure in a game environment that can take damage and be destroyed.
 */
public class Shelter
{
    // Shelter Dimensions
    static final int SHELTER_WIDTH = GamePanel.UNIT_SIZE * 2;
    static final int SHELTER_HEIGHT = GamePanel.UNIT_SIZE;
    static final int SHELTER_PADDING = GamePanel.UNIT_SIZE * 2;

    Rectangle bounds;
    int hitPoints;

    /**
     * Constructs a Shelter object with the specified coordinates.
     * @param x the x-coordinate of the shelter
     * @param y the y-coordinate of the shelter
     */
    public Shelter( int x, int y )
    {
        this.bounds = new Rectangle( x, y, SHELTER_WIDTH, SHELTER_HEIGHT );
        this.hitPoints = 10; // Total hit points for a shelter
    }

    /**
     * Reduces the hit points of the shelter by 1 and repaints the game panel.
     * If the hit points reach 0 or below, the shelter is considered destroyed.
     * @param panel the GamePanel object to repaint
     */
    public void takeDamage( GamePanel panel )
    {
        hitPoints--;
        panel.repaint(); // Ensure the panel is repainted whenever a shelter takes damage
        if ( hitPoints <= 0 )
        {
            // Handle shelter destruction here
        }
    }

    /**
     * Checks if the object is destroyed based on its hit points.
     *
     * @return true if the object is destroyed (hit points <= 0), false otherwise
     */
    public boolean isDestroyed() { return hitPoints <= 0; }
}
