/*
 * Copyright (C) 2024 European Spallation Source ERIC.
 */

package org.phoebus.applications.saveandrestore.ui.contextmenu;

import javafx.collections.ObservableList;
import org.phoebus.applications.saveandrestore.Messages;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.NodeType;
import org.phoebus.applications.saveandrestore.ui.SaveAndRestoreController;
import org.phoebus.ui.javafx.ImageCache;

import java.util.function.Consumer;

public class EditCompositeMenuItem extends SaveAndRestoreMenuItem {

    public EditCompositeMenuItem(SaveAndRestoreController saveAndRestoreController,
                                 ObservableList<Node> selectedItemsProperty,
                                 Runnable onAction) {
        super(saveAndRestoreController, selectedItemsProperty, onAction);
        setText(Messages.Edit);
        setGraphic(ImageCache.getImageView(ImageCache.class, "/icons/save-and-restore/edit-configuration.png"));
    }

    @Override
    public void configure() {
        disableProperty().set(saveAndRestoreController.getUserIdentity().isNull().get() ||
                selectedItemsProperty.size() != 1 ||
                !selectedItemsProperty.get(0).getNodeType().equals(NodeType.COMPOSITE_SNAPSHOT));
    }
}
