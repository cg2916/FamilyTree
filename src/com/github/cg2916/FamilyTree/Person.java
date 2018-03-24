package com.github.cg2916.FamilyTree;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;

public class Person extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 2509829861828673415L;
	private Person father, mother;
	private String ethnicity;

	public Person(String name) {
		super(name);
	}

	public void setEthnicity(String s) {
		if (s.equals(""))
			ethnicity = null;
		else
			ethnicity = s;
	}

	public void addFather(Person father) {
		this.father = father;
		add(father); // Adds father as a subnode
	}

	public void addMother(Person mother) {
		this.mother = mother;
		add(mother); // Adds mother as a subnode
	}

	public Person getFather() {
		return father;
	}

	public Person getMother() {
		return mother;
	}

	public void removeParent(Person parent) {
		// Remove the parent as a subnode
		parent.removeFromParent();
		
		// Set the appropriate object to point to null
		if (parent == father)
			father = null;
		else if (parent == mother)
			mother = null;
	}

	// Create a Map of a person's ethnic makeup with ethnicity name as the key and the ethnicity's decimal proportion as the value
	public HashMap<String, Double> getOrigins() {
		HashMap<String, Double> hm = new HashMap<String, Double>();
		if (ethnicity != null) {
			// If an ethnicity has been set for this person, report that the percent is 100% that ethnicity
			hm.put(ethnicity, new Double(1.0));
			return hm;
		}

		if (father != null) {
			// If the person has a father set, call getOrigins() on the father and add half of the father's values to this person's values
			HashMap<String, Double> fatherMap = father.getOrigins();
			for (Entry<String, Double> entry : fatherMap.entrySet()) {
				String key = entry.getKey();
				Double value = entry.getValue();
				if (!hm.containsKey(key))
					hm.put(key, new Double(value.doubleValue() / 2.0));
				else {
					hm.put(key, new Double(hm.get(key).doubleValue() + value.doubleValue() / 2.0));
				}
			}
		}
		if (mother != null) {
			// If the person has a mother set, call getOrigins() on the mother and add half of the mother's values to this person's values
			HashMap<String, Double> motherMap = mother.getOrigins();
			for (Entry<String, Double> entry : motherMap.entrySet()) {
				String key = entry.getKey();
				Double value = entry.getValue();
				if (!hm.containsKey(key))
					hm.put(key, new Double(value.doubleValue() / 2.0));
				else {
					hm.put(key, new Double(hm.get(key).doubleValue() + value.doubleValue() / 2.0));
				}
			}
		}

		return hm;
	}
}
