package com.example.TempusTest;

import android.app.Activity;
import android.os.Bundle;
import edu.uiowa.annotations.Annotations;

public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        test1();
    }

    /**
     * Simple use case for Tempus.
     * The deadline object is defined prior to the wait until annotation.
     * We should expect that the "o" object is within the scope of the wait_until
     */
    public void test1() {
        Object o = new Object();
        Annotations.delaybudget(o, 10);

        Annotations.waituntil(new String[1], 100);
        System.out.println(o);
    }

    /**
     * Single threaded example
     * The wait statement is before the deadline.
     * We should expect that object o is within the scope of the wait
     */
    public void test2() {
        Object o = new Object();
        //for (int x = 0; x < 100; x++) {
        Annotations.waituntil(null, 100);

        Annotations.delaybudget(o, 0);
        System.out.println(o);
        //}
    }

    /**
     * Single threaded example, with non-deterministic wait
     * The object o should be added to the scope
     */
    public void test3() {
        Object o = new Object();
        double r = Math.random();

        if (r > .5) {
            Annotations.delaybudget(o, 0);
        }

        Annotations.waituntil(null, 100);
        System.out.println(o);
    }

    /**
     * Single threaded example, with non-deterministic wait
     * The object o should be added to the scope
     */
    public void test4() {
        Object o = new Object();
        double r = Math.random();

        if (r > .5) {
            Annotations.waituntil(null, 100);
        }

        Annotations.delaybudget(o, 0);
        System.out.println(o);
    }

    /**
     * Single threaded example, with non-deterministic wait
     * The object o should be added to the scope
     */
    public void test5() {
        Object o = new Object();
        double r = Math.random();

        Annotations.delaybudget(o, 0);

        if (r > .5) {
            Annotations.waituntil(null, 100);
        }

        System.out.println(o);
    }

    public void test6() {
        Object o = new Object();

        Annotations.delaybudget(o, 0);
        test6_foo();
        System.out.println(o);
    }

    private void test6_foo() {
        double r = Math.random();
        if (r > 5) {
            Annotations.waituntil(null, 100);
        }
    }

    /**
     * Check for 2 objects
     */
    public void test7() {
        Object o1 = new Object();
        Object o2 = new Object();

        Annotations.delaybudget(o1, 10);
        Annotations.delaybudget(o2, 100);
        test6_foo();
        System.out.println(o1);
        System.out.println(o2);
    }

    /**
     * Check in the presence of simple aliasing
     */
    public void test8() {
        Object o1 = new Object();
        Annotations.delaybudget(o1, 10);
        test6_foo();
        Object o2 = o1;
        System.out.println(o2);
    }

    //
    public void test9() {
        Object o1 = new Object();
        Object o2 = null;
        Annotations.delaybudget(o1, 10);

        double r = Math.random();
        if (r > 10) {
            o2 = o1;
        } else {
            Annotations.waituntil(null, 100);
        }
        System.out.println(o2);
    }

    /**
     * This is an overly pessimistic test.
     * o2 should not be included in the scope. the call to foo kills the definition of o2
     *
     */
    public void test10() {
        Annotations.waituntil(null, 100);
        Object o1 = new Object();
        Object o2 = new Object();

        Annotations.delaybudget(o1, 10);
        Annotations.delaybudget(o1, 10);
        o2 = test10_foo(o1);                // killing definition
        System.out.println(o2);
    }

    private Object test10_foo(Object o1) {
        System.out.println(o1);
        return new Object();
    }

    public void test11() {
        Object o1 = new Object();
        Annotations.delaybudget(o1, 10);

        Annotations.waituntil(null, 100);
        test11_foo();                // killing definition
    }



    private Object test11_foo() {
        Object o2 = new Object();
        Annotations.delaybudget(o2, 10);

        System.out.println(o2);
        return o2;
    }
}
