package edu.uiowa.locks;

import soot.Local;
import soot.SootMethod;
import soot.Unit;

/**
 * Created by ochipara on 4/21/15.
 */
public class LockWaitSite extends WaitNotifyNode {
    public LockWaitSite(Unit source, SootMethod waitMethod, Local local) {
      super(source, waitMethod, local);
    }
}
