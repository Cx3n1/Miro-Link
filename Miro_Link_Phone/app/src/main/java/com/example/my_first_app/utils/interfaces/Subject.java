package com.example.my_first_app.utils.interfaces;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Subject {
    private final static Set<Listener> LISTENERS = new HashSet<>();

    public static void addListener(Listener listener){
        LISTENERS.add(listener);
    }

    public static void removeListener(Listener listener){
        LISTENERS.remove(listener);
    }

    public static void updateAll(){
        for (Listener listener : LISTENERS) {
            listener.update();
        }
    }
}
