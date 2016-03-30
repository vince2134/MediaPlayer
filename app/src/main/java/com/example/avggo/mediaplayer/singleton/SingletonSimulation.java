package com.example.avggo.mediaplayer.singleton;

import java.util.Random;

/**
 * Created by kevin on 3/30/2016.
 */
public abstract class SingletonSimulation {

    //protected static SingletonSimulation simulation;
    protected static int lossProbability;
    protected static int timeout;
    protected static int delay;
    protected static int verbosity;

    protected SingletonSimulation() {}

    protected static void setToDefaultSettings() {
        lossProbability = 0;
        timeout = 3;
        delay = 0;
        verbosity = 1;
    }

    public boolean setLossProbability(int lp) {
        if (lp >= 0 && lp <= 100) {
            lossProbability = lp;
            return true;
        }
        return false;
    }

    public boolean setTimeout(int t) {
        if (t >= 0) {
            timeout = t;
            return true;
        }
        return false;
    }

    public boolean setDelay(int d) {
        if (d >= 0) {
            delay = d;
            return true;
        }
        return false;
    }

    public boolean setVerbosity(int v) {
        if (v >= 1 && v <= 3) {
            verbosity = v;
            return true;
        }
        return false;
    }

    public int getLossProbability() {
        return lossProbability;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getDelay() {
        return delay;
    }

    public int getVerbosity() {
        return verbosity;
    }

    public boolean getRandomLossProbability() {
        Random rand = new Random();
        int n  = rand.nextInt(100) + 1;

        return n <= lossProbability;
    }
}
