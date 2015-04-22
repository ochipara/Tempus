package edu.uiowa;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.*;

/**
 * Created by ochipara on 4/20/15.
 */
public class CollectionPatcher {
    private final String basePackage = "fake.";
    private final List<String> toPatch = new ArrayList<>();

    public CollectionPatcher() {
        toPatch.add("java.util.LinkedList");
    }

    public void patchCollections() {
        for (String cls : toPatch) {
            SootClass fakeClass = Scene.v().getSootClass(basePackage + cls);
            SootClass targetClass = Scene.v().getSootClass(cls);

            for (SootMethod method : targetClass.getMethods()) {
                if (method.isPublic()) {
                    SootMethod fakeMethod = fakeClass.getMethod(method.getSubSignature());
                    //if (fakeMethod.hasActiveBody()) {
                    method.releaseActiveBody();
                    Body body =  fakeMethod.retrieveActiveBody();
                    method.setActiveBody((Body) body.clone());


                    Body newBody = method.retrieveActiveBody();
                    System.out.println(newBody);

                    //}
                }


            }
            //
        }
    }
}
