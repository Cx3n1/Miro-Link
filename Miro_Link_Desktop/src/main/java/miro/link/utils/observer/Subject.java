package miro.link.utils.observer;

import java.util.HashSet;
import java.util.Set;

public abstract class Subject {
    private static final Set<Listener> LISTENERS = new HashSet<>();

    public static void updateAll() {
        for (Listener listener : LISTENERS) {
            listener.update();
        }
    }

    public static boolean addListener(Listener newListener) {
        return LISTENERS.add(newListener);
    }

    public static boolean removeListener(Listener listener) {
        return LISTENERS.remove(listener);
    }
}
