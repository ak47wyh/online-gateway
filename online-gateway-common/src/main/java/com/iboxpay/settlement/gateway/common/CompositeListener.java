package com.iboxpay.settlement.gateway.common;

public abstract class CompositeListener<T> {

    private Object[] listeners = new Object[0];

    public synchronized void addListener(T listener) {
        if (listener == null) throw new NullPointerException("Unexpected NULL listener argument received");

        Object[] newListeners = new Object[listeners.length + 1];
        System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
        newListeners[listeners.length] = listener;
        listeners = newListeners;
    }

    public synchronized boolean removeListener(T listener) {
        if (listener == null) throw new NullPointerException("Unexpected NULL listener argument received");

        // find listener to remove in the list
        int index = -1;
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == listener) {
                index = i;
                break;
            }
        }
        if (index == -1) return false;

        Object[] newListeners = new Object[listeners.length - 1];
        if (index > 0) System.arraycopy(listeners, 0, newListeners, 0, index);

        if (index < listeners.length - 1) System.arraycopy(listeners, index + 1, newListeners, index, listeners.length - index - 1);

        listeners = newListeners;
        return true;
    }

    protected Object[] getListeners() {
        return this.listeners;
    }

    public boolean isEmpty() {
        return listeners.length == 0;
    }
}
