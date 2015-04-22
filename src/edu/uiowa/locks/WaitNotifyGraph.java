package edu.uiowa.locks;

import edu.uiowa.Constants;
import soot.*;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;

/**
 * Created by ochipara on 4/21/15.
 *
 * Constructs a wait-notify graph for the application
 * The graph has edges from notify to affected waits
 *
 * TODO: handle notifyAll and the other wait methods
 *
 */
public class WaitNotifyGraph {
    private final PointsToAnalysis pointsToAnalysis;
    private final CallGraph callGraph;
    private final List<LockWaitSite> waits = new LinkedList<>();
    private final List<LockNotifySite> notifies = new LinkedList<>();
    private final List<WaitNotifyNode> nodes = new LinkedList<>();
    private final Set<WaitNotifyEdge> edges = new HashSet<>();

    private final Map<Unit, WaitNotifyNode> unit2node = new HashMap<>();


    public WaitNotifyGraph(CallGraph callGraph, PointsToAnalysis pointsToAnalysis) {
        this.callGraph = callGraph;
        this.pointsToAnalysis = pointsToAnalysis;
    }

    public void doAnalysis() {
        collectNotifyLock();
        collectWaitLock();


        Iterator<LockNotifySite> notifyIterator = notifies.iterator();
        while (notifyIterator.hasNext()) {
            LockNotifySite notifySite = notifyIterator.next();
            PointsToSet notifyPTS = pointsToAnalysis.reachingObjects(notifySite.getLocal());

            Iterator<LockWaitSite> waitIterator = waits.iterator();
            while(waitIterator.hasNext()) {
                LockWaitSite waitSite = waitIterator.next();

                PointsToSet waitPTS = pointsToAnalysis.reachingObjects(waitSite.getLocal());
                if (notifyPTS.hasNonEmptyIntersection(waitPTS)) {
                    addEdge(notifySite, waitSite);
                }
            }
        }
    }

    private void addEdge(LockNotifySite notifySite, LockWaitSite waitSite) {
        WaitNotifyEdge edge = new WaitNotifyEdge(notifySite, waitSite);
        edges.add(edge);

        notifySite.addOutgoing(edge);
        waitSite.addIncoming(edge);
    }

    private void collectNotifyLock() {
        SootMethod wait = Scene.v().getMethod(Constants.WAIT_SIGNATURE);

        Iterator<Edge> edgeIterator = callGraph.edgesInto(wait);
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            Unit source = edge.srcUnit();
            SootMethod method = edge.getSrc().method();

            List<ValueBox> values = source.getUseBoxes();
            assert(values.size() ==  2);

            Local local = (Local) values.get(0).getValue();
            newLockWaitSite(source, method, local);
        }
    }



    private void collectWaitLock() {
        SootMethod notify = Scene.v().getMethod(Constants.NOTIFY_SIGNATURE);

        Iterator<Edge> edgeIterator = callGraph.edgesInto(notify);
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            Unit source = edge.srcUnit();
            SootMethod method = edge.getSrc().method();

            List<ValueBox> values = source.getUseBoxes();
            assert(values.size() ==  2);

            Local local = (Local) values.get(0).getValue();
            newLockNotifySite(source, method, local);
        }
    }

    private void newLockWaitSite(Unit source, SootMethod wait, Local local) {
        LockWaitSite lockWaitSite = new LockWaitSite(source, wait, local);
        nodes.add(lockWaitSite);
        waits.add(lockWaitSite);

        unit2node.put(source, lockWaitSite);
    }

    private void newLockNotifySite(Unit source, SootMethod notify, Local local) {
        LockNotifySite lockNotifySite = new LockNotifySite(source, notify, local);
        nodes.add(lockNotifySite);
        notifies.add(lockNotifySite);

        unit2node.put(source, lockNotifySite);
    }


    public String graphviz() {
        StringBuffer sb = new StringBuffer();
        sb.append("digraph locknotify {\n");
        for (WaitNotifyNode node : nodes) {
            sb.append(String.format("N%d [ label=\"%s\" ];\n", node.getId(), node.getLabel()));
        }

        for (WaitNotifyEdge edge : edges) {
            sb.append(String.format("N%d -> N%d;\n", edge.getSource().getId(), edge.getDestination().getId()));
        }

        sb.append("}");

        return sb.toString();
    }


    public WaitNotifyNode getNode(JInvokeStmt invoke) {
        return unit2node.get(invoke);
    }
}
