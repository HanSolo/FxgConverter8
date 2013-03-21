package eu.hansolo.fx;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


/**
 * Created by
 * User: hansolo
 * Date: 21.03.13
 * Time: 14:52
 */
public class WatcherEvent extends Event {
    public static final EventType<WatcherEvent> FILE_CREATED = new EventType(ANY, "FILE_CREATED");
    public static final EventType<WatcherEvent> FILE_MODIFIED = new EventType(ANY, "FILE_MODIFIED");
    public static final EventType<WatcherEvent> FILE_REMOVED  = new EventType(ANY, "FILE_REMOVED");


    // ******************** Constructors **************************************
    public WatcherEvent(EventType<? extends Event> TYPE) {
        super(TYPE);
    }
    public WatcherEvent(final Object SOURCE, final EventTarget TARGET, EventType<? extends Event> TYPE) {
        super(SOURCE, TARGET, TYPE);
    }
}
