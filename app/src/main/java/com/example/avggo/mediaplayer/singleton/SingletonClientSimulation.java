package com.example.avggo.mediaplayer.singleton;

import java.util.Random;

/**
 * Created by kevin on 3/29/2016.
 */
public class SingletonClientSimulation extends SingletonSimulation {


   /*private static SingletonClientSimulation clientSimulation;
    private static int lossProbability;
    private static int timeout;
    private static int delay;
    private static int verbosity;*/
    private static SingletonClientSimulation clientSimulation;

    private SingletonClientSimulation() {}

    public static SingletonClientSimulation getInstance() {
        if (clientSimulation == null) {
            clientSimulation = new SingletonClientSimulation();
            setToDefaultSettings();
        }

        return clientSimulation;
    }


}
