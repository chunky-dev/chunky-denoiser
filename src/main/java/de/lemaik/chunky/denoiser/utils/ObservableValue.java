package de.lemaik.chunky.denoiser.utils;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObservableValue<T> {
    public interface ChangeListener<T> {
        void onChange(T newValue);
    }

    protected static final ExecutorService executor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "ObservableValue Update Thread"));

    protected final CopyOnWriteArraySet<ChangeListener<T>> changeListeners = new CopyOnWriteArraySet<>();
    protected T value;

    public ObservableValue(T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        this.update(value);
    }

    public void update() {
        this.update(this.value);
    }

    protected void update(T newValue) {
        changeListeners.forEach(listener -> executor.execute(() -> listener.onChange(newValue)));
    }

    public void addListener(ChangeListener<T> listener) {
        changeListeners.add(listener);
    }

    public void removeListener(ChangeListener<T> listener) {
        changeListeners.remove(listener);
    }
}
