/*
 * Copyright (C) 2019 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.applications.saveandrestore.ui.model;

import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author georgweiss
 * Created 7 Jan 2019
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SnapshotTreeNode extends TreeNode{

	private String comment;
	
	@Builder
	public SnapshotTreeNode(int id, String name) {
		super(id, name, null, null, TreeNodeType.SNAPSHOT, null);
	}
	
	@Builder
	public SnapshotTreeNode(int id, String name, String userName, Date lastModified) {
		super(id, name, userName, lastModified, TreeNodeType.SNAPSHOT, null);
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append(getName());
		if(getType().equals(TreeNodeType.SAVESET) && getLastModified() != null) {
			stringBuffer.append(" " + getLastModified());
		}
		
		if(getUserName() != null && !getUserName().isEmpty()) {
			stringBuffer.append(" (" + getUserName() + ")");
		}
		
		return stringBuffer.toString();
	}
}

