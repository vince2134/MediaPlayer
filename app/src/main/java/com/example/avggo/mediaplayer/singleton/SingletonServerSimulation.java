package com.example.avggo.mediaplayer.singleton;

/**
 * Created by kevin on 3/30/2016.
 */
public class SingletonServerSimulation extends SingletonSimulation {

    private static SingletonServerSimulation singletonSimulation;

    private SingletonServerSimulation() {}

    public static SingletonServerSimulation getInstance() {
        if (singletonSimulation == null) {
            singletonSimulation = new SingletonServerSimulation();
            setToDefaultSettings();
        }

        return singletonSimulation;
    }

}
