package edu.aav66;

import java.awt.Point;

/**
 * Represents an explosion at a specific location with a timer.
 */
class Explosion
{
    Point location;
    int timer;

    /**
     * Constructs an Explosion object with the given location and timer.
     *
     * @param location the location of the explosion as a Point object
     * @param timer the timer for the explosion in seconds
     */
    Explosion( Point location, int timer )
    {
        this.location = location;
        this.timer = timer;
    }
}
