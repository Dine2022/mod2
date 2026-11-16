package com.concertmod.tile;

/** Same as a lamp, but flickers by default. */
public class TileStrobe extends TileLamp {
    public TileStrobe() {
        this.strobe = true;
        this.speed = 1.5F;
    }
}
