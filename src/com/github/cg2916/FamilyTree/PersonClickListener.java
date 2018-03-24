package com.github.cg2916.FamilyTree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class PersonClickListener implements ActionListener {
	public static final int EDIT = 0;
	public static final int SET_ETHNICITY = 1;
	public static final int ADD_FATHER = 2;
	public static final int ADD_MOTHER = 3;
	public static final int REMOVE = 4;

	private FamilyTree ftree;
	private Person person;
	private TreePath tp;
	private int action;

	public PersonClickListener(FamilyTree ftree, Person person, TreePath tp, int action) {
		this.ftree = ftree;
		this.person = person;
		this.tp = tp;
		this.action = action;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Determine which right-click menu option the user has clicked based on action
		switch(action) {
			case EDIT:
				// Edit the person's name
				String name = JOptionPane.showInputDialog("Enter this person's name");
				if (name != null) {
					person.setUserObject(name);
					refreshTree();
					ftree.setChangedSinceSave(true);
				}
				break;
			case SET_ETHNICITY:
				String ethnicity = JOptionPane.showInputDialog("What is this person's ethnicity?");
				if (ethnicity != null) {
					person.setEthnicity(ethnicity);
					ftree.refreshSummary();
					ftree.setChangedSinceSave(true);
				}
				break;
			case ADD_FATHER:
				if (person.getFather() == null) { // If the person doesn't already have a father
					String s = JOptionPane.showInputDialog("Enter this person's father");
					if (s != null) {
						person.addFather(new Person(s));
						refreshTree();
						ftree.setChangedSinceSave(true);
					}
				} else {
					JOptionPane.showMessageDialog(ftree, "This person already has a father.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case ADD_MOTHER:
				if (person.getMother() == null) { // If the person doesn't already have a mother
					String s = JOptionPane.showInputDialog("Enter this person's mother");
					if (s != null) {
						person.addMother(new Person(s));
						refreshTree();
						ftree.setChangedSinceSave(true);
					}
				} else {
					JOptionPane.showMessageDialog(ftree, "This person already has a mother.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case REMOVE:
				// Remove the person from the tree
				if (JOptionPane.showConfirmDialog(ftree, "Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					Person child = (Person) person.getParent(); // The person's child is considered a "parent" node in JTree, hence the confusing terminology
					child.removeParent(person); // Remove this person as its child's parent
					JTree tree = ftree.getTree();
					((DefaultTreeModel) tree.getModel()).reload(); // Refresh the JTree
					tree.expandPath(tp.getParentPath().getParentPath()); // Expand the tree to the person's child
					ftree.refreshSummary();
					ftree.setChangedSinceSave(true);
				}
				break;
		}
	}
	
	// Refresh the JTree and expand it to the selected person
	private void refreshTree() {
		JTree tree = ftree.getTree();
		((DefaultTreeModel) tree.getModel()).reload();
		tree.expandPath(tp);
	}
}