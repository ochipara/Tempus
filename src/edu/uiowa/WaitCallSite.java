package edu.uiowa;

import java.util.HashSet;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;

public class WaitCallSite {
	private final Unit unit;
	private final SootMethod target;	
	private final Set<BudgetSite> scope = new HashSet<BudgetSite>();
	private final String key;

	public WaitCallSite(SootClass sootClass, SootMethod target, Unit unit) {
		this.unit = unit;
		this.target = target;	
		
		SourceFileTag file = (SourceFileTag) sootClass.getTag("SourceFileTag");
		LineNumberTag line = (LineNumberTag) unit.getTag("LineNumberTag");
		
		//String clsName = sootClass.getJavaPackageName() + sootClass.getJavaStyleName();
		this.key = file.getSourceFile() + ":" + line.getLineNumber();		

	}

	public SootMethod getMethod() {
		return target;
	}
	
	public Set<BudgetSite> getScope() {
		return scope;
	}

	public String getKey() {
		return key;
	}

	public String getScopeAsString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean first = true;
		for (BudgetSite budget : scope) {
			if (!first) sb.append(",");
			sb.append(budget.getKey());
			first = false;
		}
		
		sb.append("}");
		
		return sb.toString();
	}

}
