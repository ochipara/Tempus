package edu.uiowa.locks;

/**
 * Created by ochipara on 4/21/15.
 */
public class WaitNotifyEdge {
    private final LockNotifySite notifySite;
    private final LockWaitSite waitSite;

    public WaitNotifyEdge(LockNotifySite notifySite, LockWaitSite waitSite) {
        this.notifySite = notifySite;
        this.waitSite = waitSite;
    }

    public LockNotifySite getSource() {
        return notifySite;
    }

    public LockWaitSite getDestination() {
        return waitSite;
    }
}
