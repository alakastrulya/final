package tanks.observer;

import tanks.observer.Observer;

public interface Observable {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(String eventType, Object subject);
}