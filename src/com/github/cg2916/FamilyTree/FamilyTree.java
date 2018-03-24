package com.github.cg2916.FamilyTree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class FamilyTree extends JFrame implements MouseListener, WindowListener {
	private static final long serialVersionUID = -4187147642903930746L;
	
	private JTree tree;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem newTree, open, save, saveAs;
	private JLabel summary; // Label summarizing the root person's ethnic makeup
	private JScrollPane jsp;
	
	private File lastDir; // The last directory accessed by the user when opening or saving
	private File currentFile = new File(""); // The currently open file
	
	private boolean startedUp = false; // Indicates if the application has completely gone through initialization
	private boolean changedSinceSave = false; // Indicates if the tree has changed since the user's last save

	public FamilyTree() {
		super("Family Tree");

		lastDir = new File("");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// Startup popup menu
		String[] options = { "New tree", "Open tree" };
		int startChoice = JOptionPane.showOptionDialog(this, "Would you like to start a new tree or open a previously saved tree?", "FamilyTree", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (startChoice == 0) {
			// Create a new tree
			String rootName = JOptionPane.showInputDialog("Enter the name of the root person for your tree");
			Person root = new Person(rootName);
			tree = new JTree(root);
			changedSinceSave = true;
		} else if (startChoice == 1) {
			// Open a previously saved tree
			tree = new JTree(new Person(""));
			openTree();
		} else {
			// If the user closes the dialog box, exit the application
			System.exit(0);
		}

		setupGUI();

		startedUp = true;
	}

	private void setupGUI() {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(600, 400));

		// Creates the menu bar and associate menu items
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		newTree = new JMenuItem("New");
		fileMenu.add(newTree);
		open = new JMenuItem("Open...");
		fileMenu.add(open);
		save = new JMenuItem("Save...");
		fileMenu.add(save);
		saveAs = new JMenuItem("Save as...");
		fileMenu.add(saveAs);
		setJMenuBar(menuBar);

		// Add listeners to each menu item
		newTree.addActionListener(e -> {openNewTree();});
		open.addActionListener(e -> {openTree();});
		save.addActionListener(e -> {saveTree();});
		saveAs.addActionListener(e -> {saveAsTree();});
		
		tree.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		
		// Ensure that the tree can only contain one path at a time
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// Add listener to implement right-click functionality on the tree
		tree.addMouseListener(this);
		
		// Allow the user to scroll around the tree if the window is too small
		jsp = new JScrollPane(tree);
		add(jsp, BorderLayout.CENTER);

		summary = new JLabel();
		summary.setFont(new Font("Label.font", Font.PLAIN, 14));
		add(summary, BorderLayout.SOUTH);
		
		// The following lines allow the application to ask the user if they want to save changes before closing
		addWindowListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		pack();
		setVisible(true);
	}

	public JTree getTree() {
		return tree;
	}
	
	public void setChangedSinceSave(boolean changedSinceSave) {
		this.changedSinceSave = changedSinceSave;
	}

	public void refreshSummary() {
		// Refreshes the text summary label after a change in the tree
		Person root = (Person) (tree.getModel().getRoot());
		summary.setText(formatOrigins(root));
	}
	
	// Creates an entirely new tree
	public void openNewTree() {
		// Before creating the new tree, check if the user wants/needs to save the current tree
		if (changedSinceSave) {
			String[] options = {"Save", "Don't Save", "Cancel"};
			int choice = JOptionPane.showOptionDialog(this, "Do you want to save changes?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (choice == 0) {
				saveTree();
			} else if (choice != 1) {
				// If the user clicks Cancel or closes the dialog box, don't create a new tree
				return;
			}
			// If the user clicks Don't Save (so choice=1), simply proceed with new tree creation
		}
		
		String rootName = JOptionPane.showInputDialog("Enter the name of the root person for your tree");
		Person root = new Person(rootName);
		// Essentially wipes the tree clean and sets a newly specified person as the new root
		((DefaultTreeModel) tree.getModel()).setRoot(root);
		
		// Reset appropriate data members
		refreshSummary();
		currentFile = new File("");
		changedSinceSave = true;
	}

	public void openTree() {
		// Before opening a tree, check if the user wants/needs to save the current tree
		if (changedSinceSave) {
			String[] options = {"Save", "Don't Save", "Cancel"};
			int choice = JOptionPane.showOptionDialog(this, "Do you want to save changes?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (choice == 0) {
				saveTree();
			} else if (choice != 1) {
				// If the user clicks Cancel or closes the dialog box, don't open a tree
				return;
			}
			// If the user clicks Don't Save (so choice=1), simply proceed with tree opening
		}
		
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new FileNameExtensionFilter("Family tree files (*.fam)", "fam")); // Only allow .fam files to be seen
		// If the user has previously used a directory in the filesystem, start with that directory
		if (!lastDir.getPath().equals((""))) {
			jfc.setCurrentDirectory(lastDir);
		}
		int userSelection = jfc.showOpenDialog(this);

		// If the user has selected a file
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			// Set lastDir to the directory of the opened file
			lastDir = jfc.getCurrentDirectory(); 
			currentFile = jfc.getSelectedFile();
			try {
				// Load the TreeModel from the file
				FileInputStream fis = new FileInputStream(currentFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				tree.setModel((TreeModel) (ois.readObject()));
				// The following conditional exists to avoid a NullPointerException if the user is opening a file at startup when the summary label hasn't been initialized
				if (startedUp)
					refreshSummary();
				// Close input streams
				ois.close();
				fis.close();
			} catch (IOException | ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			changedSinceSave = false;
		}
	}
	
	public void saveTree() {
		// If the user has not previously saved this file
		if (currentFile.getName().equals("")) { 
			saveAsTree();
		} else { // If the user has previously saved this file
			try {
				// Write the TreeModel to currentFile
				writeTreeToFile(currentFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			changedSinceSave = false;
		}
	}
	
	// Save the tree as a potentially new file
	public void saveAsTree() {
		JFileChooser jfc = new JFileChooser();
		// Only allow .fam files to be seen
		jfc.setFileFilter(new FileNameExtensionFilter("Family tree files (*.fam)", "fam"));
		// If the user has previously used a directory in the filesystem, start with that directory
		if (!lastDir.getPath().equals((""))) {
			jfc.setCurrentDirectory(lastDir);
		}
		int userSelection = jfc.showSaveDialog(this);

		// If the user has specified a file to save to
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			// Set lastDir to the directory in which the file will be saved
			lastDir = jfc.getCurrentDirectory();
			File file = jfc.getSelectedFile();
	        if(!file.getAbsolutePath().endsWith(".fam")) {
	        	// If the specified file doesn't end with .fam, append it to the file name
	        	file = new File(file.getAbsolutePath() + ".fam");
	        }
			try {
				// Write the TreeModel to the specified file
				writeTreeToFile(file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// Set currentFile to the chosen file
			currentFile = file;
			changedSinceSave = false;
		}
	}
	
	// Write the TreeModel to the given file
	private void writeTreeToFile(File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(tree.getModel());
		// Close output streams
		oos.close();
		fos.close();
	}

	// Create a formatted string to display in the summary label
	private String formatOrigins(Person p) {
		// Begin crafting the string to be retunred
		String s = "<html>" + (String) p.getUserObject() + ": "; // The html tags allow for wrapping in the summary JLabel
		
		// Obtain a hashmap of the root person's ethnic makeup, sorted by ethnicity in descending order by percentage
		Map<String, Double> m = MapUtil.sortByValue(p.getOrigins());

		Set<Entry<String, Double>> es = m.entrySet();
		Iterator<Entry<String, Double>> iter = es.iterator();
		// For each ethnicity, add its name and percentage (formatted to 2 decimal places) to the string
		while (iter.hasNext()) {
			Entry<String, Double> e = (Entry<String, Double>) iter.next();
			Double percentage = e.getValue() * 100;
			DecimalFormat format = new DecimalFormat("0.00");
			String percent = format.format(percentage);
			s += percent + "% " + e.getKey() + (iter.hasNext() ? ", " : "");
		}
		s += "</html>";
		return s;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			// If the user right-clicks, create a right click menu at the user's cursor with an appropriate listener for each item
			
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			tree.setSelectionPath(selPath);
			if (selRow > -1) {
				tree.setSelectionRow(selRow);
			}
			Person node = (Person) tree.getLastSelectedPathComponent();
			if (node == null)
				return;
			tree.clearSelection();

			JPopupMenu popup = new JPopupMenu();
			JMenuItem edit = new JMenuItem("Edit name");
			edit.addActionListener(new PersonClickListener(this, node, selPath, PersonClickListener.EDIT));
			popup.add(edit);
			JMenuItem setEthnicity = new JMenuItem("Set ethnicity");
			setEthnicity.addActionListener(new PersonClickListener(this, node, selPath, PersonClickListener.SET_ETHNICITY));
			popup.add(setEthnicity);
			JMenuItem addFather = new JMenuItem("Add father");
			addFather.addActionListener(new PersonClickListener(this, node, selPath, PersonClickListener.ADD_FATHER));
			popup.add(addFather);
			JMenuItem addMother = new JMenuItem("Add mother");
			addMother.addActionListener(new PersonClickListener(this, node, selPath, PersonClickListener.ADD_MOTHER));
			popup.add(addMother);
			JMenuItem remove = new JMenuItem("Remove");
			remove.addActionListener(new PersonClickListener(this, node, selPath, PersonClickListener.REMOVE));
			popup.add(remove);
			popup.show(tree, e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			// If the user left-clicks, get the ethnic makeup summary for the selected person
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			if (selRow > -1) { // If the user selects an actual person
				Person p = (Person) tree.getLastSelectedPathComponent();
				summary.setText(formatOrigins(p));
			} else { // If the user clicks white space
				refreshSummary();
			}
		}
	}

	// When the user tries to close the application, check if the user wants/needs to save their changes
	@Override
	public void windowClosing(WindowEvent e) {
		if (changedSinceSave) {
			String[] options = {"Save", "Don't Save", "Cancel"};
			int choice = JOptionPane.showOptionDialog(this, "Do you want to save changes?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (choice == 0) {
				saveTree();
			} else if (choice == 1) {
				// If the user chooses "Don't Save", simply close the application
				dispose();
			}
			// Otherwise, don't close the window
		} else {
			// If the user hasn't changed anything since the last save, close the application
			dispose();
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new FamilyTree();
			}
		});
	}
}