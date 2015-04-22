package edu.uiowa.annotations;

/**
 * Created by ochipara on 4/22/15.
 */
public class Annotations {
    public static void delaybudget(Object target, int budget) {
        System.out.println(budget + " " + target);
    }

    public static void waituntil(String[] namespaces, int maxDelay) {
        if (namespaces == null) {
            System.out.println("default " + maxDelay);
        } else {
            System.out.println(namespaces.toString() + " " + maxDelay);
        }
    }
}
