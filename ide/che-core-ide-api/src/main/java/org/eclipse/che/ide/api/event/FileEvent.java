/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.event;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.resources.VirtualFile;
import com.google.gwt.event.shared.GwtEvent;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.SAVE;

/**
 * Event that describes the fact that file is going to be opened.
 *
 * @author Nikolay Zamosenchuk
 * @author Artem Zatsarynnyi
 */
public class FileEvent extends GwtEvent<FileEventHandler> {

    public static Type<FileEventHandler> TYPE = new Type<>();
    private VirtualFile   file;
    private FileOperation fileOperation;
    private String        tabId;

    /**
     * Creates new {@link FileEvent}.
     *
     * @param file
     *         {@link VirtualFile} that represents an affected file
     * @param fileOperation
     *         file operation
     */
    private FileEvent(VirtualFile file, FileOperation fileOperation) {
        this.file = file;
        this.fileOperation = fileOperation;
    }

    private FileEvent(String tabId, VirtualFile file, FileOperation fileOperation) {
        this(file, fileOperation);
        this.tabId = tabId;
    }

    /**
     * Creates a event for {@code FileOperation.OPEN}.
     */
    public static FileEvent createOpenFileEvent(VirtualFile file) {
        return new FileEvent(file, OPEN);
    }

    /**
     * Creates a event for {@code FileOperation.CLOSE}.
     */
    public static FileEvent createCloseFileEvent(String tabId, VirtualFile file) {
        return new FileEvent(tabId, file, CLOSE);
    }

    /**
     * Creates a event for {@code FileOperation.SAVE}.
     */
    public static FileEvent createSaveFileEvent(VirtualFile file) {
        return new FileEvent(file, SAVE);
    }

    /** {@inheritDoc} */
    @Override
    public Type<FileEventHandler> getAssociatedType() {
        return TYPE;
    }

    /** @return {@link VirtualFile} that represents an affected file */
    public VirtualFile getFile() {
        return file;
    }

    /** @return the type of operation performed with file */
    public FileOperation getOperationType() {
        return fileOperation;
    }

    @Nullable
    public String getTabId() {
        return tabId;
    }

    @Override
    protected void dispatch(FileEventHandler handler) {
        handler.onFileOperation(this);
    }

    public enum FileOperation {
        OPEN, SAVE, CLOSE
    }
}
