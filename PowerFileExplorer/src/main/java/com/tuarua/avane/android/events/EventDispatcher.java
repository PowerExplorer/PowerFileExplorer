package com.tuarua.avane.android.events;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by User on 02/10/2016.
 */

public class EventDispatcher implements IEventDispatcher {
    protected ArrayList<Listener> listenerList = new ArrayList<>();
    @Override
    public void addEventListener(String type, IEventHandler handler) {
        Listener listener = new Listener(type, handler);
        removeEventListener(type);
        listenerList.add(0, listener);
    }

    @Override
    public void removeEventListener(String type) {
        for (Iterator<Listener> iterator = listenerList.iterator();iterator.hasNext();) {
            Listener listener = iterator.next();
            if (listener.getType().equals(type)) {
                listenerList.remove(listener);
            }
        }
    }

    @Override
    public void dispatchEvent(Event event) {
        for (Iterator<Listener> iterator = listenerList.iterator();iterator.hasNext();) {
            Listener listener = iterator.next();
            if (event.getStrType().equals(listener.getType())) {
                IEventHandler eventHandler = listener.getHandler();
                eventHandler.callback(event);
            }
        }
    }

    @Override
    public boolean hasEventListener(String type) {
        for (Iterator<Listener> iterator = listenerList.iterator();iterator.hasNext();) {
            Listener listener = iterator.next();
            if (listener.getType().equals(type)) {
                return true;
            }
        }
		return false;
    }

    @Override
    public void removeAllListeners() {
		listenerList.clear();
    }
}
