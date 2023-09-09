package chess.util;

import java.util.HashSet;
import java.util.Set;

public abstract class Observable {

    private final Set<Observer> observers = new HashSet<>();

    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    public void notifyAll(Object userInfo) {
        this.observers.forEach(observer -> observer.update(this, userInfo));
    }

}
