package edu.uiowa;

import soot.*;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author ochipara
 *
 *	To run the analysis you should:
 *	- checkout in the same directory: heros, jasmin, soot, soot-infoflow, soot-infoflow-android
 *	- create a new Eclipse project and import all the above directories
 *  - import the APEAnalysis/APE into the eclipse project
 *  
 *  - now, you'll have to modify the apkLocation, sdkLocation, and sourceAndSinks to match your local configuration
 *  - the SourcesAndSinks file is provided by infoflow-android project (aka flowdroid)
 *
 *
 *  obtaning the real android.jar from https://github.com/Sable/android-platforms/blob/master/android-17/android.jar
 *  
 */
public class TempusMain {	
	//public static final String apkLocation = "/Users/ochipara/AndroidstudioProjects/TempusTests/app/build/outputs/apk/app-debug.apk";
	//public static final String apkLocation = "/Users/ochipara/AndroidstudioProjects/ProducerConsumer/app/build/outputs/apk/app-debug.apk";
	public static final String apkLocation = "/Users/ochipara/Working/Tempus/tests/ProducerConsumerExample/app/build/outputs/apk/app-debug.apk";
	//public static final String apkLocation = "/Users/ochipara/Working/Tempus/tests/HealthReportExample/app/build/outputs/apk/app-debug.apk";
	

//	public static final String sdkLocation = "/Users/ochipara/android-sdk/platforms/";
	public static final String sdkLocation = "/Users/ochipara/Working/Tempus/";


	public static final String sourceAndSinks = "/Users/ochipara/tmp/workspace2/APEAnalysis/SourcesAndSinks.txt";
	
	/**
	 * Settings for spark
	 */
	static void runSparkAnalysis() {				
		HashMap<String,String> opt = new HashMap<>();
		opt.put("enabled","true");
		opt.put("verbose","true");
		opt.put("ignore-types","false");          
		opt.put("force-gc","false");            
		opt.put("pre-jimplify","false");          
		opt.put("vta","false");                   
		opt.put("rta","false");                   
		opt.put("field-based","false");           
		opt.put("types-for-sites","false");        
		opt.put("merge-stringbuffer","true");   
		opt.put("string-constants","false");     
		opt.put("simulate-natives","true");      
		opt.put("simple-edges-bidirectional","false");
		opt.put("on-fly-cg","true");            
		opt.put("simplify-offline","false");    
		opt.put("simplify-sccs","false");        
		opt.put("ignore-types-for-sccs","false");
		opt.put("propagator","worklist");
		opt.put("set-impl","double");
		opt.put("double-set-old","hybrid");         
		opt.put("double-set-new","hybrid");
		opt.put("dump-html","false");           
		opt.put("dump-pag","false");             
		opt.put("dump-solution","false");        
		opt.put("topo-sort","false");           
		opt.put("dump-types","true");             
		opt.put("class-method-var","true");     
		opt.put("dump-answer","false");          
		opt.put("add-tags","false");             
		opt.put("set-mass","false"); 		
		
		SparkTransformer.v().transform("",opt);		
		System.out.println("[spark] Done!");
	}
	
	
	/**
	 * Based on infoflow-android 
	 * @param sdkLocation
	 * @param apkFileLocation
	 */
	private static void initializeSoot(String sdkLocation, String apkFileLocation) {
//		Options.v().set_no_bodies_for_excluded(false);
//		Options.v().set_allow_phantom_refs(false);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_include_all(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(true);
		Options.v().set_android_jars(sdkLocation);
		Options.v().set_process_dir(Collections.singletonList(apkFileLocation));
		String androidJar = Scene.v().getAndroidJarPath(sdkLocation, apkFileLocation);
		Options.v().set_soot_classpath(androidJar);

		Options.v().set_src_prec(Options.src_prec_apk);
		Main.v().autoSetOptions();

		Options.v().setPhaseOption("cg.spark", "on");
		Scene.v().setSootClassPath("/Users/ochipara/Working/Tempus/android-4.4.2_r1.jar:" + Scene.v().getSootClassPath());


		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
		Scene.v().loadClassAndSupport("java.util.concurrent.BlockingQueue");
		Scene.v().loadClassAndSupport("java.util.concurrent.LinkedBlockingDeque");
		Scene.v().addBasicClass("java.util.LinkedList", SootClass.BODIES);
		Scene.v().loadClassAndSupport("fake.java.util.LinkedList");

		Scene.v().loadNecessaryClasses();


		System.out.println(Utils.section("Initializing Soot"));
		System.out.println("ClassPath: " + Scene.v().getSootClassPath());
		//System.exit(0);
	}


	static void printCode(String signature) {
		SootMethod method = Scene.v().getMethod(signature);
		printMethod(method);
	}

	private static void printMethod(SootMethod method) {
		SootClass cls = method.getDeclaringClass();
		boolean isReachable = Scene.v().getReachableMethods().contains(method);

		PatchingChain<Unit> unitBoxes = method.retrieveActiveBody().getUnits();
		Iterator<Unit> iter = unitBoxes.iterator();
		System.out.println(String.format("--(%s:%s0)--> reachable=%s", cls.getName(), method.getName(), isReachable));

		while (iter.hasNext()) {
			Unit unit = iter.next();
			System.out.println(String.format("--(%s:%s0)--> %s", cls.getName(), method.getName(), unit));
		}
	}

	public static void printCode(String className, String methodName) {
		SootClass cls = Scene.v().getSootClass(className);
		if (cls == null) {
			throw new IllegalArgumentException("Could not find class " + cls);
		}
		SootMethod method = cls.getMethodByName(methodName);
		printMethod(method);
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {		
		// initialize logging
		Properties props = System.getProperties();
		System.out.println("Working Directory = " +  System.getProperty("user.dir"));
		props.setProperty("log4j.configurationFile", "/Users/ochipara/tmp/workspace2/APEAnalysis/log4j.xml");
		
		// compute the main of the application
		SetupApplication app = new SetupApplication(TempusMain.sdkLocation, apkLocation);
		app.calculateSourcesSinksEntrypoints(TempusMain.sourceAndSinks);		
		soot.G.reset();		 
		
		// initialize soot
		initializeSoot(sdkLocation, apkLocation);

		// additional configuration
		Options.v().set_output_format(Options.output_format_none);
		SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();

		Options.v().set_main_class(entryPoint.getSignature());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));

		// run the soot
//		Options.v().set_whole_program(true);
//		Options.v().setPhaseOption("cg.spark", "on");
//		Options.v().setPhaseOption("jb", "use-original-names:true");
//		Scene.v().loadNecessaryClasses();
//		PackManager.v().runPacks();

		LibraryClassPatcher patcher = new LibraryClassPatcher();
		patcher.patchLibraries();

//		CollectionPatcher collectionPatcher = new CollectionPatcher();
//		collectionPatcher.patchCollections();


		// run the spark analysis
		runSparkAnalysis();
		
		System.out.println("---------------------------------");

		CallGraph cg = Scene.v().getCallGraph();
		PointsToAnalysis pta = Scene.v().getPointsToAnalysis();



//		Iterator<Edge> edgeIterator = cg.iterator();
//		Set<String> signatures = new HashSet<>();
//		while(edgeIterator.hasNext()) {
//			Edge edge = edgeIterator.next();
//
//			signatures.add(edge.getSrc().method().getSignature());
//			signatures.add(edge.getTgt().method().getSignature());
//			System.out.println(edge.getSrc().method().getSignature() + " => " + edge.getTgt().method().getSignature());
//
//		}
//
//		for (String signature : signatures) {
//			System.out.println("Called method " + signature);
//		}
		
//		SootClass cls = Scene.v().getSootClass("com.example.test1.MainActivity");
//		SootMethod onResume = cls.getMethodByName("onResume");
//		boolean hasOnResume = Scene.v().getReachableMethods().contains(onResume);
//		System.out.println("has " + hasOnResume);

				
//		printCode(onResume);
//		Map<Integer,Local> ls = PTATest.getLocals(cls, "onResume", null);
//		System.out.println("---------------------------------");
		
//		PTATest.printLocalIntersects(ls);

//		for (SootClass cls : Scene.v().getClasses()) {
//			System.out.println("basic class " + cls);
//		}

		//printCode("uiowa.edu.producerconsumer.Consumer", "run");
		//printCode("java.lang.Thread", "run");
//		printCode("uiowa.edu.producerconsumer.Producer", "run");
//		printCode("uiowa.edu.producerconsumer.Consumer", "run");

		//ystem.exit(0);

		printCode("<uiowa.edu.waitnotify.TestWaitNotify$Waiter: void run()>");


		System.out.println("\n\nInvoking deadline analysis\n\n");
		//TempusAnalysis.deadlineAnalysis(cg, pta);
		TempusProgramAnalysis tpa = new TempusProgramAnalysis(cg, pta);
		tpa.doAnalysis();
	}




}

