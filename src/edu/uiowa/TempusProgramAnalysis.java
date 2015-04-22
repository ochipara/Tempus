package edu.uiowa;

import edu.uiowa.locks.LockNotifySite;
import edu.uiowa.locks.WaitNotifyEdge;
import edu.uiowa.locks.WaitNotifyGraph;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.FlowSet;

import java.util.*;

/**
 * 1. the the points-to sets for all the variables specified by a delay annotation
 * 2. for each reachable method
 * construct a CFG
 * for each program point
 * if p has a @Wait(budget):
 * wait_id create new id
 * out[p] = {wait_id}
 * else if p is merge(in1, in2)
 * out[p] = in1[p] U in2[p]
 * else
 * out[p] = in[p]
 * <p>
 * 3.
 * let B be the set of variables with budgets
 * for each program point p that is annotated
 * let wait-id be the id of the wait that p is annotated with
 * let V be the set of variable uses at p
 * for each pair (b, v) in B x V:
 * if point-to(b) intersect point-to(v) != empty
 * scope[wait-id] = scope[wait-id] U v
 *
 * @author ochipara
 */
public class TempusProgramAnalysis {
    private CallGraph callGraph;
    private PointsToAnalysis pta;

    private final SootMethod waituntilMethod = Scene.v().getMethod(Constants.WAITUNTIL_SIGNATURE);
    private final List<BudgetSite> budgets = new LinkedList<BudgetSite>();
    private final Map<String, MethodAnalysis> allAnalysis = new HashMap<String, MethodAnalysis>();
    private final List<MethodAnalysis> workList = new ArrayList<>();
    private final Map<String, WaitCallSite> waitSites = new HashMap<String, WaitCallSite>();
    private final WaitNotifyGraph waitNotifyGraph;

    public TempusProgramAnalysis(CallGraph callGraph, PointsToAnalysis pta) {
        this.callGraph = callGraph;
        this.pta = pta;
        this.waitNotifyGraph = new WaitNotifyGraph(callGraph, pta);
    }

    public void doAnalysis() {
        this.waitNotifyGraph.doAnalysis();
        System.out.println(this.waitNotifyGraph.graphviz());

        /**
         * Collect the budgets of each budget call
         */
        collectBudgets();

        /**
         * Initialize the analysis, by creating a analysis for each method in the callgraph
         */
        initializeAnalysis();


        /**
         * Initialize the worklist. Initially add all the wait calls sites
         *
         */
        for (WaitCallSite callSite : waitSites.values()) {
            SootMethod method = callSite.getMethod();
            MethodAnalysis methodAnalysis = allAnalysis.get(method.getSignature());
            assert (method != null);

            workList.add(methodAnalysis);
        }


        /**
         * Propagate until convergence
         */
        int numInterations = 0;
        while (workList.isEmpty() == false) {
            MethodAnalysis methodAnalysis = workList.remove(0);

            analyze(methodAnalysis, null);
            numInterations++;
        }

        System.out.println(String.format("Dataflow analysis converged in %d iterations", numInterations));

        //
        // compute the scopes of the waits
        //
        for (MethodAnalysis methodAnalysis : allAnalysis.values()) {
            SootMethod method = methodAnalysis.getMethod();
            ExceptionalUnitGraph graph = methodAnalysis.getGraph();

            Iterator<Unit> iter = graph.iterator();
            while (iter.hasNext()) {
                Unit unit = iter.next();
                FlowSet<WaitState> waits = methodAnalysis.getWaits(unit);

                if ((waits != null) && (waits.isEmpty() == false)) {
                    // this is a unit that is affected by a wait
                    List<ValueBox> valueBoxes = unit.getUseAndDefBoxes();
                    for (ValueBox valueBox : valueBoxes) {
                        Value value = valueBox.getValue();
                        if (value instanceof Local) {
                            Local local = (Local) value;

                            // check if there is any budget associated with pts
                            Iterator<WaitState> waitIter = waits.iterator();
                            while (waitIter.hasNext()) {
                                WaitState wait = waitIter.next();

                                checkBudget(local, wait.getSite().getScope());
                            }
                        }
                    }

                }
            }
        }

        // print the scopes of the waits
        System.out.println("\n\n\n************* Analysis Results *****************");
        for (WaitCallSite wait : waitSites.values()) {
            System.out.println(String.format("Wait @ %s depends on the following budgets", wait.getKey()));
            Set<BudgetSite> budgets = wait.getScope();
            for (BudgetSite budget : budgets) {
                System.out.println(String.format("\t\t==> budget @ %s", budget.toString()));
            }
        }
    }


    private void initializeAnalysis() {
        /**
         * generate information regarding the call sites of waits
         */
        Iterator<Edge> waituntilEdges = callGraph.edgesInto(waituntilMethod);
        while (waituntilEdges.hasNext()) {
            Edge callIntoWait = waituntilEdges.next();
            Unit srcUnit = callIntoWait.srcUnit();
            SootMethod method = callIntoWait.getSrc().method();

            WaitCallSite waitSite = new WaitCallSite(method.getDeclaringClass(), method, srcUnit);
            waitSites.put(waitSite.getKey(), waitSite);
        }

        if (waitSites.size() == 0) {
            System.err.println("Not waits found");
            System.exit(-1);
        }


        /**
         * initialize the results of the analysis for each method
         */
        Iterator<Edge> callIterator = callGraph.iterator();
        while (callIterator.hasNext()) {
            Edge edge = callIterator.next();
            SootMethod source = edge.getSrc().method();
            initializeAnalysisForMethod(source);

            SootMethod dest = edge.getTgt().method();
            initializeAnalysisForMethod(dest);
        }

    }

    private void initializeAnalysisForMethod(SootMethod method) {
        String signature = method.getSignature();
        if (allAnalysis.containsKey(signature) == false) {
            if (method.hasActiveBody()) {
                Body body = method.retrieveActiveBody();
                ExceptionalUnitGraph exceptionGraph = new ExceptionalUnitGraph(body);

                MethodAnalysis methodAnalysis = new MethodAnalysis(exceptionGraph, method, waitSites, allAnalysis, waitNotifyGraph);
                allAnalysis.put(signature, methodAnalysis);
            }
        }
    }

    private void checkBudget(Local local, Set<BudgetSite> scope) {
        PointsToSet variablePTS = pta.reachingObjects(local);

        for (BudgetSite budget : budgets) {
            PointsToSet budgetPTS = budget.getPointsTo();
            if (budgetPTS.hasNonEmptyIntersection(variablePTS)) {
                // add this variable to the scope
                scope.add(budget);
                System.out.println(String.format("\tAdd to budget %s due to variables (%s, %s)", budget.toString(), local.getName(), budget.getLocalValue().getName()));
            }
        }
    }

    /**
     * Collect the budgets
     */
    private void collectBudgets() {
        SootMethod deadlineMethod;
        try {
            deadlineMethod = Scene.v().getMethod(Constants.DEADLINE_ID_SIGNATURE);
        } catch (RuntimeException e) {
            G.v().out.println("Failed finding method: " + Constants.DEADLINE_ID_SIGNATURE);
            G.v().out.println("No deadlines found, skipping analysis.");
            return;
        }

        Iterator<Edge> deadlineEdges = callGraph.edgesInto(deadlineMethod);
        while (deadlineEdges.hasNext()) {
            Edge e = deadlineEdges.next();
            Unit u = e.srcUnit();

            // Get parameters to BoundDelay call
            List<ValueBox> values = u.getUseBoxes();
//			int delayBudget = Integer.parseInt(values.get(0).getValue().toString());
            Local localValue = (Local) values.get(0).getValue();
//			int budgetId = Integer.parseInt(values.get(2).getValue().toString());

            SourceFileTag file = (SourceFileTag) e.getSrc().method().getDeclaringClass().getTag("SourceFileTag");
            LineNumberTag line = (LineNumberTag) u.getTag("LineNumberTag");

            PointsToSet pts = pta.reachingObjects(localValue);

            BudgetSite budget = new BudgetSite(file.getSourceFile(), line.getLineNumber(), localValue, pts);
            budgets.add(budget);
        }
    }

    private void analyze(MethodAnalysis methodAnalysis, FlowSet<WaitState> initialState) {
        // do the analysis
        System.out.println(String.format("************* Analyzing (%s) *************", methodAnalysis));
        boolean changed = methodAnalysis.doAnalysis(initialState);

        // inspect the results
        System.out.println(String.format("\t--- Propagated Waits (%s, changed=%s) ---", methodAnalysis, changed));
        Iterator<Unit> unitIter = methodAnalysis.getGraph().iterator();
        while (unitIter.hasNext()) {
            Unit unit = unitIter.next();
            FlowSet<WaitState> waits = methodAnalysis.getWaits(unit);

            System.out.println("\t" + waits.toString() + " => " + unit);
        }
        System.out.println("\tExit set: " + methodAnalysis.getReturnState());

        String added = "";
        if (changed) {
            /**
             * The functions calling into this method must be recomputed
             */
            Iterator<Edge> callsIntoIterator = callGraph.edgesInto(methodAnalysis.getMethod());
            int addedCount = 0;
            while(callsIntoIterator.hasNext()) {
                Edge callInto = callsIntoIterator.next();
                SootMethod nextMethod = callInto.getSrc().method();
                if (Constants.DUMMY_SIGNATURE.equals(nextMethod.getSignature()) == false) {
                    MethodAnalysis nextMethodAnalysis = allAnalysis.get(nextMethod.getSignature());
                    added = added + " " + nextMethod.getName();
                    workList.add(nextMethodAnalysis);
                    addedCount++;
                }
            }

            /**
             * The functions waiting on the objects into this method must be recocomputed
             */
            while(methodAnalysis.getThreadNotifySites().isEmpty() == false) {
                LockNotifySite notifySite = methodAnalysis.getThreadNotifySites().remove();

                List<WaitNotifyEdge> outgoingEdges = notifySite.getOutgoing();
                Iterator<WaitNotifyEdge> outgoingIter = outgoingEdges.iterator();
                while (outgoingIter.hasNext()) {
                    WaitNotifyEdge edge = outgoingIter.next();

                    SootMethod nextMethod = edge.getDestination().getMethod();
                    MethodAnalysis nextMethodAnalysis = allAnalysis.get(nextMethod.getSignature());
                    added = added + " " + nextMethod.getName();
                    workList.add(nextMethodAnalysis);
                    addedCount++;
                }

            }
            System.out.println(String.format("\tadded (%d): %s", addedCount, added));
        }
    }
}
