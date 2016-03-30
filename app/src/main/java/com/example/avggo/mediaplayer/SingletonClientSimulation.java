package com.example.avggo.mediaplayer;

/**
 * Created by kevin on 3/29/2016.
 */
public class SingletonClientSimulation {

    private static SingletonClientSimulation clientSimulation;
    private static int lossProbability;
    private static int timeout;
    private static int delay;
    private static int verbosity;

    private SingletonClientSimulation() {}

    public static SingletonClientSimulation getInstance() {
        if (clientSimulation == null) {
            clientSimulation = new SingletonClientSimulation();
            lossProbability = 0;
            timeout = 0;
            delay = 0;
            verbosity = 1;
        }

        return clientSimulation;
    }

    public boolean setLossProbability(int lp) {
        if (lp >= 0) {
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
}
