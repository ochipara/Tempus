package edu.uiowa;

import soot.Local;
import soot.PointsToSet;

public class BudgetSite {
	private int line;
	private Local localValue;
	private PointsToSet pts;
	private String sourceFile;
	private final String key;

	public BudgetSite(String sourceFile, int line, Local localValue, PointsToSet pts) {
		this.sourceFile = sourceFile;
		this.line = line;
		this.localValue = localValue;
		this.pts = pts;
		this.key = String.format("%s:%d", sourceFile, line);
	}

	public PointsToSet getPointsTo() {
		return pts;
	}
	
	public String toString() {
		return key;
	}
	
	public String getKey() {
		return key;
	}

	public Local getLocalValue() {
		return localValue;
	}
}
