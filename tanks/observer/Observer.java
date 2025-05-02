package tanks.observer;

public interface Observer {
    void update(String eventType, Object subject);
}