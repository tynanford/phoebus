/*******************************************************************************
 * Copyright (c) 2017 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.phoebus.framework.persistence;

import java.util.List;

/** Tree-structured Memento
 *
 *  @author Kay Kasemir
 */
public interface MementoTree extends Memento
{
    /** @param key Key that identifies a child memento
     *  @return Child memento.
     *          Never <code>null</code>, will be created if it didn't exist.
     */
    public MementoTree getChild(String key);

    /** @return Child mementos. May be empty array */
    public List<MementoTree> getChildren();
}
