package net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree;
/*
 * Copyright (C) 2002 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.sql.DatabaseObjectType;
import net.sourceforge.squirrel_sql.fw.sql.IDatabaseObjectInfo;

import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree.tabs.DatabaseObjectInfoTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.ConnectionStatusTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.DataTypesTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.DateTimeFunctionsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.KeywordsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.MetaDataTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.NumericFunctionsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.StringFunctionsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.SystemFunctionsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.databasepanel.TableTypesTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.objectpanel.IObjectPanelTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.procedurepanel.ProcedureColumnsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.ColumnPriviligesTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.ColumnsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.ContentsTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.ExportedKeysTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.ImportedKeysTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.IndexesTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.PrimaryKeyTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.RowCountTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.RowIDTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.TablePriviligesTab;
import net.sourceforge.squirrel_sql.client.session.objectstree.tablepanel.VersionColumnsTab;
/**
 * This is the panel for the Object Tree tab.
 *
 * @author  <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class ObjectTreePanel extends JPanel
{
	/** Current session. */
	private ISession _session;

	/** Tree of objects within the database. */
	private ObjectTree _tree;

	/** Split pane between the object tree and the data panel. */
	private final JSplitPane _splitPane =
		new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	/**
	 * Empty data panel. Used if the object selected in the object
	 * tree doesn't require a panel.
	 */
	private final ObjectTreeTabbedPane _emptyTabPane;

	/**
	 * Contains instances of <TT>ObjectTreeTabbedPane</TT> objects keyed by
	 * the node type. I.E. the tabbed folders for each node type are kept here.
	 */
	private Map _tabbedPanes = new HashMap();

	/**
	 *  Collection of <TT>IObjectPanelTab</TT> objects to be displayed for all
	 * nodes in the object tree.
	 */
	private List _tabsForAllNodes = new ArrayList();
	
	/**
	 * ctor specifying the current session.
	 * 
	 * @param	session	Current session.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>ISession</TT> passed.
	 */
	public ObjectTreePanel(ISession session)
	{
		super();
		if (session == null)
		{
			throw new IllegalArgumentException("ISession == null");
		}
		_session = session;

		_emptyTabPane = new ObjectTreeTabbedPane(_session);

		createUserInterface();

		// Register tabs to appear for all nodes in the object tree.
//		addDetailTab(new DatabaseObjectInfoTab());

		// Register tabs to display in the details panel for database nodes.
		addDetailTab(DatabaseObjectType.DATABASE, new MetaDataTab());
		addDetailTab(DatabaseObjectType.DATABASE, new ConnectionStatusTab());
		addDetailTab(DatabaseObjectType.DATABASE, new TableTypesTab());
		addDetailTab(DatabaseObjectType.DATABASE, new DataTypesTab());
		addDetailTab(DatabaseObjectType.DATABASE, new NumericFunctionsTab());
		addDetailTab(DatabaseObjectType.DATABASE, new StringFunctionsTab());
		addDetailTab(DatabaseObjectType.DATABASE, new SystemFunctionsTab());
		addDetailTab(DatabaseObjectType.DATABASE, new DateTimeFunctionsTab());
		addDetailTab(DatabaseObjectType.DATABASE, new KeywordsTab());

		// Register tabs to display in the details panel for catalog nodes.
		addDetailTab(DatabaseObjectType.CATALOG, new DatabaseObjectInfoTab());

		// Register tabs to display in the details panel for schema nodes.
		addDetailTab(DatabaseObjectType.SCHEMA, new DatabaseObjectInfoTab());

		// Register tabs to display in the details panel for table nodes.
		addDetailTab(DatabaseObjectType.TABLE, new DatabaseObjectInfoTab());
//		addDetailTab(DatabaseObjectType.TABLE, new TableInfoTab());
		addDetailTab(DatabaseObjectType.TABLE, new ContentsTab());
		addDetailTab(DatabaseObjectType.TABLE, new RowCountTab());
		addDetailTab(DatabaseObjectType.TABLE, new ColumnsTab());
		addDetailTab(DatabaseObjectType.TABLE, new PrimaryKeyTab());
		addDetailTab(DatabaseObjectType.TABLE, new ExportedKeysTab());
		addDetailTab(DatabaseObjectType.TABLE, new ImportedKeysTab());
		addDetailTab(DatabaseObjectType.TABLE, new IndexesTab());
		addDetailTab(DatabaseObjectType.TABLE, new TablePriviligesTab());
		addDetailTab(DatabaseObjectType.TABLE, new ColumnPriviligesTab());
		addDetailTab(DatabaseObjectType.TABLE, new RowIDTab());
		addDetailTab(DatabaseObjectType.TABLE, new VersionColumnsTab());

		// Register tabs to display in the details panel for procedure nodes.
//		addDetailTab(DatabaseObjectType.PROCEDURE, new ProcedureInfoTab());
		addDetailTab(DatabaseObjectType.PROCEDURE, new DatabaseObjectInfoTab());
		addDetailTab(DatabaseObjectType.PROCEDURE, new ProcedureColumnsTab());

		// Register tabs to display in the details panel for UDT nodes.
		// TODO: get rid of this once all nodes have a DatabaseObjectInfoTab tab.
		addDetailTab(DatabaseObjectType.UDT, new DatabaseObjectInfoTab());
	}

	/**
	 * Add an expander for the specified database object type.
	 * 
	 * @param	dboType		Database object type.
	 * @param	expander	Expander called to add children to a parent node.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> <TT>DatabaseObjectType</TT>
	 * 			or <TT>INodeExpander</TT> passed.
	 */
	public void addExpander(DatabaseObjectType dboType, INodeExpander expander)
	{
		if (dboType == null)
		{
			throw new IllegalArgumentException("Null DatabaseObjectType passed");
		}
		if (expander == null)
		{
			throw new IllegalArgumentException("Null INodeExpander passed");
		}
		_tree.getTypedModel().addExpander(dboType, expander);
	}

	/**
	 * Add a tab to be displayed in the detail panel for all nodes
	 * in the object tree.
	 * 
	 * @param	tab			Tab to be displayed.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown when a <TT>null</TT> <TT>IObjectPanelTab</TT> passed.
	 */
	public void addDetailTab(IObjectPanelTab tab)
	{
		if (tab == null)
		{
			throw new IllegalArgumentException("IObjectPanelTab == null");
		}

		_tabsForAllNodes.add(tab);
//		Iterator it = _tabbedPanes.values().iterator();
//		while (it.hasNext())
//		{
//			ObjectTreeTabbedPane tabPane = (ObjectTreeTabbedPane)it.next();
//			tabPane.addObjectPaneltab(tab);
//		}
	}

	/**
	 * Add a tab to be displayed in the detail panel for the passed
	 * database object type type.
	 * 
	 * @param	dboType		Database Object type.
	 * @param	tab			Tab to be displayed.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown when a <TT>null</TT> <TT>DatabaseObjectType</TT> or
	 * 			<TT>IObjectPanelTab</TT> passed.
	 */
	public void addDetailTab(DatabaseObjectType dboType, IObjectPanelTab tab)
	{
		if (dboType == null)
		{
			throw new IllegalArgumentException("Null DatabaseObjectType passed");
		}
		if (tab == null)
		{
			throw new IllegalArgumentException("IObjectPanelTab == null");
		}
		
		getOrCreateObjectPanelTabbedPane(dboType).addObjectPaneltab(tab);
	}

	/**
	 * Add a listener to the object tree for structure changes. I.E nodes
	 * added/removed.
	 * 
	 * @param	lis		The <TT>TreeModelListener</TT> you want added.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>TreeModelListener</TT> passed.
	 */
	public void addTreeModelListener(TreeModelListener lis)
	{
		if (lis == null)
		{
			throw new IllegalArgumentException("TreeModelListener == null");
		}
		_tree.getModel().addTreeModelListener(lis);
	}

	/**
	 * Remove a structure changes listener from the object tree.
	 * 
	 * @param	lis		The <TT>TreeModelListener</TT> you want removed.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>TreeModelListener</TT> passed.
	 */
	public void removeTreeModelListener(TreeModelListener lis)
	{
		if (lis == null)
		{
			throw new IllegalArgumentException("TreeModelListener == null");
		}
		_tree.getModel().removeTreeModelListener(lis);
	}

	/**
	 * Add an item to the popup menu for the specified database object type
	 * in the object tree.
	 * 
	 * @param	dboType		Database Object type.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</passed> <TT>DatabaseObjectType</TT>
	 * 			or <TT>Action</TT> passed.
	 */
	public void addToObjectTreePopup(DatabaseObjectType dboType, Action action)
	{
		if (dboType == null)
		{
			throw new IllegalArgumentException("Null DatabaseObjectType passed");
		}
		if (action == null)
		{
			throw new IllegalArgumentException("Null Action passed");
		}
		_tree.addToPopup(dboType, action);
	}

	/**
	 * Add an item to the popup menu for all node types in the object
	 * tree.
	 * 
	 * @param	action		Action to add to menu.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> <TT>Action</TT> passed.
	 */
	public void addToObjectTreePopup(Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Null Action passed");
		}
		_tree.addToPopup(action);
	}

	/**
	 * Return an array of the currently selected nodes.
	 *
	 * @return	array of <TT>ObjectTreeNode</TT> objects.
	 */
	public ObjectTreeNode[] getSelectedNodes()
	{
		return _tree.getSelectedNodes();
	}

	/**
	 * Return an array of the currently selected database
	 * objects. This is guaranteed to be non-null.
	 *
	 * @return	array of <TT>ObjectTreeNode</TT> objects.
	 */
	public IDatabaseObjectInfo[] getSelectedDatabaseObjects()
	{
		return _tree.getSelectedDatabaseObjects();
	}

	/**
	 * Refresh object tree.
	 */
	public void refreshTree()
	{
		_tree.refresh();
	}

	/**
	 * Remove one or more nodes from the tree.
	 * 
	 * @param	nodes	Array of nodes to be removed.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> <TT>ObjectTreeNode[]</TT> thrown.
	 */
	public void removeNodes(ObjectTreeNode[] nodes)
	{
		if (nodes == null)
		{
			throw new IllegalArgumentException("ObjectTreeNode[] == null");
		}
		ObjectTreeModel model = _tree.getTypedModel();
		for (int i = 0; i < nodes.length; ++i)
		{
			model.removeNodeFromParent(nodes[i]);
		}
	}

	/**
	 * Set the panel to be shown in the data area for the passed
	 * path.
	 * 
	 * @param	path	path of node currently selected.
	 */
	private void setSelectedObjectPanel(TreePath path)
	{
		ObjectTreeTabbedPane tabPane = null;
		if (path != null)
		{
			Object lastComp = path.getLastPathComponent();
			if (lastComp instanceof ObjectTreeNode)
			{
				ObjectTreeNode node = (ObjectTreeNode)lastComp;
				tabPane = getDetailPanel(node);
				tabPane.setDatabaseObjectInfo(node.getDatabaseObjectInfo());
				tabPane.selectCurrentTab();
			}
		}
		setSelectedObjectPanel(tabPane);
	}

	/**
	 * Set the panel in the data area to that passed.
	 * 
	 * @param	comp	Component to be displayed. If <TT>null</TT> use an empty
	 * 					panel.
	 */
	private void setSelectedObjectPanel(Component comp)
	{
		if (comp == null)
		{
			comp = _emptyTabPane;
		}

		int divLoc = _splitPane.getDividerLocation();
		Component existing = _splitPane.getRightComponent();
		if (existing != null)
		{
			_splitPane.remove(existing);
		}
		_splitPane.add(comp, JSplitPane.RIGHT);
		_splitPane.setDividerLocation(divLoc);
	}

	/**
	 * Get the detail panel to be displayed for the passed node.
	 * 
	 * @param	node	Node to get details panel for.
	 * 
	 * @throws	IllegalArgumentException
	 * 			Thrown if a <TT>null</TT> <TT>ObjectTreeNode</TT> passed.
	 */
	private ObjectTreeTabbedPane getDetailPanel(ObjectTreeNode node)
	{
		if (node == null)
		{
			throw new IllegalArgumentException("ObjectTreeNode == null");
		}

		ObjectTreeTabbedPane tabPane = getTabbedPane(node.getDatabaseObjectType());
		if (tabPane != null)
		{
			return tabPane;
		}

		return _emptyTabPane;
	}

	/**
	 * Return the tabbed pane for the passed object tree node type.
	 * 
	 * @param	dboType		The database object type we are getting a tabbed
	 *						pane for.
	 * 
	 * @return		the <TT>ObjectTreeTabbedPane</TT> for the passed database object
	 *				type.
	 */
	private ObjectTreeTabbedPane getTabbedPane(DatabaseObjectType dboType)
	{
		return (ObjectTreeTabbedPane)_tabbedPanes.get(dboType.getIdentifier());
	}

	/**
	 * Return the tabbed pane for the passed database object type. If one
	 * doesn't exist then create it.
	 * 
	 * @param	dboType		The database object type we are getting a tabbed
	 *						pane for.
	 * 
	 * @return	the <TT>List</TT> containing all the <TT>IObjectPanelTab</TT>
	 * 			instances for the passed object tree node type.
	 */
	private ObjectTreeTabbedPane getOrCreateObjectPanelTabbedPane(DatabaseObjectType dboType)
	{
		if (dboType == null)
		{
			throw new IllegalArgumentException("Null DatabaseObjectType passed");
		}

		final IIdentifier key = dboType.getIdentifier();
		ObjectTreeTabbedPane tabPane = (ObjectTreeTabbedPane)_tabbedPanes.get(key);
		if (tabPane == null)
		{
			tabPane = new ObjectTreeTabbedPane(_session);
			_tabbedPanes.put(key, tabPane);

			// Add tabs that are displayed for all nodes to this new tabbed
			// folder.
//			Iterator it = _tabsForAllNodes.iterator();
//			while (it.hasNext())
//			{
//				//?? TODO: Should clone/copy the tab
//				IObjectPanelTab tab = (IObjectPanelTab)it.next();
//				tabPane.addObjectPaneltab(tab);
//			}
		}
		return tabPane;
	}

	/**
	 * Create the user interface.
	 */
	private void createUserInterface()
	{
		setLayout(new BorderLayout());

		_tree = new ObjectTree(_session);

		_splitPane.setOneTouchExpandable(true);
		_splitPane.setContinuousLayout(true);
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(_tree);
		sp.setPreferredSize(new Dimension(200, 200));
		_splitPane.add(sp, JSplitPane.LEFT);
		add(_splitPane, BorderLayout.CENTER);

		_tree.addTreeSelectionListener(new ObjectTreeSelectionListener());

		setSelectedObjectPanel(_emptyTabPane);
		_splitPane.setDividerLocation(200);
	}

	/**
	 * This class listens for changes in the node selected in the tree
	 * and displays the appropriate detail panrl for the node.
	 */
	private final class ObjectTreeSelectionListener
		implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent evt)
		{
			setSelectedObjectPanel(evt.getNewLeadSelectionPath());
		}
	}
}
