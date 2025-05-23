/*
 * Copyright (C) 2024 European Spallation Source ERIC.
 */

package org.phoebus.applications.saveandrestore.ui.contextmenu;


import javafx.collections.ObservableList;
import org.phoebus.applications.saveandrestore.Messages;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.NodeType;
import org.phoebus.applications.saveandrestore.ui.SaveAndRestoreBaseController;
import org.phoebus.ui.javafx.ImageCache;

public class CreateSnapshotMenuItem extends SaveAndRestoreMenuItem {

    public CreateSnapshotMenuItem(SaveAndRestoreBaseController saveAndRestoreController, ObservableList<Node> selectedItemsProperty, Runnable onAction) {
        super(saveAndRestoreController, selectedItemsProperty, onAction);
        setText(Messages.contextMenuCreateSnapshot);
        setGraphic(ImageCache.getImageView(ImageCache.class, "/icons/save-and-restore/snapshot.png"));
    }

    @Override
    public void configure() {
        disableProperty().set(saveAndRestoreBaseController.getUserIdentity().isNull().get() ||
                selectedItemsProperty.size() != 1 ||
                !selectedItemsProperty.get(0).getNodeType().equals(NodeType.CONFIGURATION));
    }
}
