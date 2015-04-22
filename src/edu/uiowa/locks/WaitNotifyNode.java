package edu.uiowa.locks;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ochipara on 4/21/15.
 */
public class WaitNotifyNode{
    protected final SootMethod method;
    protected final Local local;
    protected final Unit unit;
    protected static int idCounter = 0;
    protected final int id;
    protected final String label;

    protected final List<WaitNotifyEdge> incoming = new LinkedList<>();
    protected final List<WaitNotifyEdge> outgoing = new LinkedList<>();

    public WaitNotifyNode(Unit unit, SootMethod method, Local local) {
        this.id = idCounter++;
        this.unit = unit;
        this.method = method;
        this.local = local;

        SootClass sootClass = method.getDeclaringClass();
        SourceFileTag file = (SourceFileTag) sootClass.getTag("SourceFileTag");
        LineNumberTag line = (LineNumberTag) unit.getTag("LineNumberTag");

        this.label = String.format("%s:%s", file.getSourceFile(), line.getLineNumber());
    }

    public void addIncoming(WaitNotifyEdge edge) {
        incoming.add(edge);
    }

    public List<WaitNotifyEdge> getIncoming() {
        return incoming;
    }

    public void addOutgoing(WaitNotifyEdge edge) {
        outgoing.add(edge);
    }

    public List<WaitNotifyEdge> getOutgoing() {
        return outgoing;
    }

    public Local getLocal() {
        return local;
    }

    public Unit getUnit() {
        return unit;
    }

    public SootMethod getMethod() {
        return method;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getSignature() {
        return method.getSignature();
    }

    public String toString() {
        return label;
    }

}

