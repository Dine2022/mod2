package com.concertmod.radio;

/** Implemented by TileEntities that react to wireless radio channel state changes. */
public interface IRadioListener {
    void onRadioStateChanged(boolean active);
}
