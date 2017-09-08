/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.phoebus.ui.dialog;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/** Dialog that prompts for a file name to 'save'
 *  @author Kay Kasemir
 */
public class SaveAsDialog
{
    public static File promptForFile(final Window window, final String title, File file, final ExtensionFilter[] filters)
    {
        if (Platform.isFxApplicationThread())
            return doPromptForFile(window, title, file, filters);

        final AtomicReference<File> the_file = new AtomicReference<>();
        final CountDownLatch done = new CountDownLatch(1);

        Platform.runLater(() ->
        {
            the_file.set(doPromptForFile(window, title, file, filters));
            done.countDown();
        });

        try
        {
            done.await();
            return the_file.get();
        }
        catch (InterruptedException ex)
        {
            return null;
        }
    }

    private static File doPromptForFile(final Window window, final String title, File file, final ExtensionFilter[] filters)
    {

        final FileChooser dialog = new FileChooser();
        dialog.setTitle(title);

        if (file != null)
        {
            dialog.setInitialDirectory(file.getParentFile());
            dialog.setInitialFileName(file.getName());
        }
        dialog.getExtensionFilters().addAll(filters);
        return dialog.showSaveDialog(window);
    }
}