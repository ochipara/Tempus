package edu.uiowa;

import java.util.*;

import edu.uiowa.locks.*;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JInvokeStmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * This is an intra-procedural analyzer that labels the program points with the 
 * wait statement that affects them, if one exists
 * 
 * @author ochipara
 *
 */
public class MethodAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<WaitState>> {
	private final FlowSet<WaitState> emptySet;
	private final SootMethod method;
	private final SourceFileTag file;
	private final SootClass sootClass;
	private final Map<String, WaitCallSite> waitSites;
	private final FlowSet<WaitState> returnState;
	private final FlowSet<WaitState> entryState;
	private final Map<String, MethodAnalysis> allMethods;
	private final Queue<LockNotifySite> threadNotifySites = new LinkedList<>();
	private final WaitNotifyGraph waitNotifyGraph;

	public MethodAnalysis(DirectedGraph g, SootMethod method, Map<String, WaitCallSite> waitSites, Map<String, MethodAnalysis> allMethods, WaitNotifyGraph waitNotifyGraph)  {
		super(g);
		emptySet = new ArraySparseSet<>();
		this.method = method;
		this.sootClass = method.getDeclaringClass();
		this.file = (SourceFileTag) method.getDeclaringClass().getTag("SourceFileTag");
		this.waitSites = waitSites;
		this.returnState = emptySet.clone();
		this.entryState = emptySet.clone();
		this.allMethods = allMethods;
		this.waitNotifyGraph = waitNotifyGraph;
	}

	public boolean doAnalysis(FlowSet<WaitState> entryStateIn) {
		FlowSet<WaitState> saveReturnState = returnState.clone();

		// set the entry state of the method
		if (entryStateIn != null) {
			this.entryState.union(entryStateIn);
		}
		super.doAnalysis();

		// compute the exit value
		for (Unit unit : graph.getTails()) {
			FlowSet<WaitState> wait = getWaits(unit);
			returnState.union(wait);
		}

		return !returnState.equals(saveReturnState);
	}

	@Override
	protected void flowThrough(FlowSet<WaitState> in, Unit u, FlowSet<WaitState> out) {
		System.out.println(String.format("\t[method-analysis/through] in:%s u:%s", in.toString(), u));
		if (u instanceof JInvokeStmt) {
			JInvokeStmt invoke = (JInvokeStmt) u;			
			String signature = invoke.getInvokeExpr().getMethod().getSignature();
			if (Constants.WAITUNTIL_SIGNATURE.equals(signature)) {					
				LineNumberTag line = (LineNumberTag) u.getTag("LineNumberTag");
				final String key = String.format("%s:%d", file, line.getLineNumber());			
				final WaitCallSite waitCall = waitSites.get(key);
				
				// generate the unique id
				final WaitState waitState = new WaitState(waitCall);
				out.union(in);
				out.add(waitState);				
				
				System.out.println(String.format("\t[method-analysis/wait-found] %s => %s", in.toString(), out.toString()));
			} else if (Constants.isThreadNotify(signature)) {
				LockNotifySite notifySite = (LockNotifySite) waitNotifyGraph.getNode(invoke);
				threadNotifySites.add(notifySite);
				out.union(in);
				System.out.println(String.format("\t[method-analysis/notify] in:%s site:%s", in.toString(), notifySite));

			} else if (Constants.isThreadWait(signature)) {
				LockWaitSite wnNode = (LockWaitSite) waitNotifyGraph.getNode(invoke);
				List<WaitNotifyEdge> edgeList = wnNode.getIncoming();

				Iterator<WaitNotifyEdge> iter = edgeList.iterator();
				while(iter.hasNext()) {
					WaitNotifyEdge edge = iter.next();
					LockNotifySite notifySite = edge.getSource();

					// get the state of the notify site
					MethodAnalysis m = allMethods.get(notifySite.getSignature());
					FlowSet<WaitState> inFromNotify = m.getWaits(notifySite.getUnit());
					out.union(inFromNotify);

					System.out.println(String.format("\t[method-analysis/wait] incoming:%s in:%s", notifySite, inFromNotify));
				}
			} else {
				MethodAnalysis callMethodAnalysis = allMethods.get(signature);
				if (callMethodAnalysis == null) {
					// these are methods without active bodies
					out.union(in);
				} else {
					out.union(callMethodAnalysis.getReturnState());
				}
			}
		} else {
			// propagate the waits
			out.union(in);
		}
	}

	@Override
	protected FlowSet<WaitState> newInitialFlow() {
		return emptySet.clone();
	}

	@Override
	protected FlowSet<WaitState> entryInitialFlow() {		
		return entryState;
	}

	@Override
	protected void merge(FlowSet<WaitState> in1, FlowSet<WaitState> in2, FlowSet<WaitState> out) {
		// out = in1 U in2
		in1.union(in2, out);		
	}

	@Override
	protected void copy(FlowSet<WaitState> source, FlowSet<WaitState> dest) {
		source.copy(dest);
	}

	public FlowSet<WaitState> getWaits(Unit unit) {
		return getFlowAfter(unit);		
	}

	public ExceptionalUnitGraph getGraph() {	
		return (ExceptionalUnitGraph) this.graph;
	}
	
	public SootMethod getMethod() {
		return method;
	}

	public Queue<LockNotifySite> getThreadNotifySites() {
		return threadNotifySites;
	}

	public FlowSet<WaitState> getReturnState() {
		return returnState;
	}

	public String getSignature() {
		return method.getSignature();
	}

	public String toString() {
		return getSignature();
	}
}
