package edu.uiowa;

public class Constants {
//	public static final String WAITUNTIL_SIGNATURE = "<edu.ucsd.ape.clienthelper.ServiceConnectionHelper: void waituntil_statement(int,int,boolean[])>";
//	public static final String DEADLINE_ID_SIGNATURE = "<edu.ucsd.ape.Deadline: void deadline(int,java.lang.Object,int)>";

	public static final String WAITUNTIL_SIGNATURE = "<edu.ucsd.ape.Annotations: void waituntil(int,int,int[])>";
	public static final String DEADLINE_ID_SIGNATURE = "<edu.ucsd.ape.Annotations: void deadline(int,java.lang.Object,int)>";

	public static final String DUMMY_SIGNATURE = "<dummyMainClass: void dummyMainMethod()>";
	public static final String NOTIFY_SIGNATURE = "<java.lang.Object: void notify()>";
	public static final String NOTIFY_ALL_SIGNATURE = "<java.lang.Object: void notifyAll()>";
	public static final String WAIT_SIGNATURE = "<java.lang.Object: void wait()>";

	public static boolean isThreadNotify(String signature) {
		if (NOTIFY_SIGNATURE.equals(signature)) return true;
		if (NOTIFY_ALL_SIGNATURE.equals(signature)) return true;

		return false;
	}

	public static boolean isThreadWait(String signature) {
		if (WAIT_SIGNATURE.equals(signature)) return true;
		return false;
	}
}
