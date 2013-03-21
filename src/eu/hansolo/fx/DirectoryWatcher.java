package eu.hansolo.fx;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.PGNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;


/**
 * Created by
 * User: hansolo
 * Date: 21.03.13
 * Time: 13:56
 */
public class DirectoryWatcher extends Node implements Runnable, EventTarget {

    private final Path          PATH;
    private final String        FILE_NAME;

    public DirectoryWatcher(final File FXG_FILE) {
        PATH      = FileSystems.getDefault().getPath(FXG_FILE.getParent());
        FILE_NAME = FXG_FILE.getName();
    }


    // ******************** Event handling ************************************
    public final ObjectProperty<EventHandler<WatcherEvent>> onFileModifiedProperty() { return onFileModified; }
    public final void setOnFileModified(EventHandler<WatcherEvent> value) { onFileModifiedProperty().set(value); }
    public final EventHandler<WatcherEvent> getOnFileModified() { return onFileModifiedProperty().get(); }
    private ObjectProperty<EventHandler<WatcherEvent>> onFileModified = new ObjectPropertyBase<EventHandler<WatcherEvent>>() {

        @Override protected void invalidated() { setEventHandler(WatcherEvent.FILE_MODIFIED, get()); }

        @Override public Object getBean() { return this; }

        @Override public String getName() { return "onFileModified"; }
    };

    public final ObjectProperty<EventHandler<WatcherEvent>> onFileRemovedProperty() { return onFileRemoved; }
    public final void setOnFileRemoved(EventHandler<WatcherEvent> value) { onFileRemovedProperty().set(value); }
    public final EventHandler<WatcherEvent> getOnFileRemoved() { return onFileRemovedProperty().get(); }
    private ObjectProperty<EventHandler<WatcherEvent>> onFileRemoved = new ObjectPropertyBase<EventHandler<WatcherEvent>>() {

        @Override protected void invalidated() { setEventHandler(WatcherEvent.FILE_REMOVED, get()); }

        @Override public Object getBean() { return this; }

        @Override public String getName() { return "onFileRemoved"; }
    };

    private void fireWatcherEvent(final Event EVENT) {
        fireEvent(EVENT);
        EVENT.consume();
    }


    @Override protected PGNode impl_createPGNode() {
        return null;
    }
    @Override public BaseBounds impl_computeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        return null;
    }
    @Override protected boolean impl_computeContains(double v, double v2) {
        return false;
    }
    @Override public Object impl_processMXNode(MXNodeAlgorithm mxNodeAlgorithm, MXNodeAlgorithmContext mxNodeAlgorithmContext) {
        return null;
    }


    private void handleEvent(WatchEvent<?> event) {
        final WatchEvent.Kind<?> KIND = event.kind();
        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(KIND)) {
            if (FILE_NAME.equals(event.context().toString())) {
                // fire update event here
                fireWatcherEvent(new WatcherEvent(this, null, WatcherEvent.FILE_MODIFIED));
            }
        } else if (StandardWatchEventKinds.ENTRY_CREATE.equals(KIND)) {
            //fireWatcherEvent(new WatcherEvent(this, null, WatcherEvent.FILE_CREATED));
        } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(KIND)) {
            if (FILE_NAME.equals(event.context().toString())) {
                // fire removed event here
                fireWatcherEvent(new WatcherEvent(this, null, WatcherEvent.FILE_REMOVED));
            }
        }
    }

    @Override public void run() {
        try {
            WatchService watchService = PATH.getFileSystem().newWatchService();
            PATH.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                                        StandardWatchEventKinds.ENTRY_MODIFY,
                                        StandardWatchEventKinds.ENTRY_DELETE);

            // loop forever to watch directory
            while (true) {
                WatchKey watchKey;
                watchKey = watchService.take(); // this call is blocking until events are present

                // poll for file system events on the WatchKey
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    handleEvent(event);
                }

                // if the watched directory gets deleted, get out of run method
                if (!watchKey.reset()) {
                    System.out.println("No longer valid");
                    watchKey.cancel();
                    watchService.close();
                    break;
                }
            }

        } catch (InterruptedException exception) {
            System.out.println("interrupted. Goodbye");
            return;
        } catch (IOException exception) {
            return;
        }
    }
}
