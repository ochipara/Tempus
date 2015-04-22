package edu.uiowa.locks;

import soot.Local;
import soot.SootMethod;
import soot.Unit;

/**
 * Created by ochipara on 4/21/15.
 */
public class LockNotifySite extends WaitNotifyNode{
    public LockNotifySite(Unit unit, SootMethod notify, Local local) {
        super(unit, notify, local);
    }
}
