package edu.aav66;

import java.awt.Rectangle;

/**
 * Shelter
 */
public class Shelter
{
    // Shelter Dimensions
    static final int SHELTER_WIDTH = GamePanel.UNIT_SIZE * 2;
    static final int SHELTER_HEIGHT = GamePanel.UNIT_SIZE;
    static final int SHELTER_PADDING = GamePanel.UNIT_SIZE * 2;

    Rectangle bounds;
    int hitPoints;

    public Shelter( int x, int y )
    {
        this.bounds = new Rectangle( x, y, SHELTER_WIDTH, SHELTER_HEIGHT );
        this.hitPoints = 10; // Total hit points for a shelter
    }

    public void takeDamage()
    {
        hitPoints--;
        if ( hitPoints <= 0 )
        {
            // Handle shelter destruction here
        }
    }

    public boolean isDestroyed() { return hitPoints <= 0; }
}
