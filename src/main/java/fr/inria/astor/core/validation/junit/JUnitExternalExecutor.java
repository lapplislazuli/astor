package fr.inria.astor.core.validation.junit;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import fr.inria.astor.core.validation.junit.filters.TestFilter;

/**
 * This class runs a JUnit test suite i.e., a set of test cases.
 * 
 * @author Matias Martinez, matias.martinez@inria.fr
 * 
 */
public class JUnitExternalExecutor {

	List<String> successTest = new ArrayList<String>();

	List<String> failTest = new ArrayList<String>();

	public final static String OUTSEP = "astoroutdel";

	public String createOutput(Result r) {
		String out = "[";
		int count = 0;
		int failures = 0;
		try {
			for (Failure f : r.getFailures()) {
				String s = failureMessage(f);
				if (!s.startsWith("warning")) {
					failures++;
				}
				out += s + "-,";
				count++;
				if (count > 10) {
					out += "...and " + (r.getFailureCount() - 10) + " failures more,";
					// break;
				}
			}
		} catch (Exception e) {
			// We do not care about this exception,
		}
		out = out + "]";
		return (OUTSEP + r.getRunCount() + OUTSEP + failures + OUTSEP + out + OUTSEP);
	}

	protected String failureMessage(Failure f) {
		try {
			return f.toString();
		} catch (Exception e) {
			return f.getTestHeader();
		}
	}

	public static void main(String[] arg) throws Exception {
		System.out.println("JUnitExternalExecutorMainStart");
		JUnitExternalExecutor re = new JUnitExternalExecutor();

		Result result = re.run(arg);
		// This sysout is necessary for the communication between process...
		System.out.println(re.createOutput(result));

		System.out.println("JUnitExternalExecutorMainEnd");
		System.exit(0);
	}

	/*
	This Args should only contain the tests to run within the Classpath.
	Any ClassPath from the JunitLauncher is "consumed" at this point.
	It fails silently if the methods are not found.
	 */
	public Result run(String[] arg) throws Exception {
		System.out.println("JUnitExternalExecutor Run Start");
		for (String a : arg){
			System.out.println("Received arg: " + a);
		}
		PrintStream original = System.out;
		/*
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));
		 */
		System.out.println("JUnitExternalExecutor Run Outputs Reset");

		List<Class> classes = getClassesToRun(arg);
		JUnitCore runner = new JUnitCore();
		Logger.getGlobal().setLevel(Level.OFF);
		//System.out.println("Hello JUnitExternalExecutor!");
		Result resultjUnit = runner.run(classes.toArray(new Class[classes.size()]));
		System.setOut(original);

		System.out.println("JUnitExternalExecutorRunEnd");
		return resultjUnit;
	}

	protected List<Class> getClassesToRun(String[] arg) throws ClassNotFoundException {
		TestFilter tf = new TestFilter();
		List<Class> classes = new ArrayList<Class>();
		for (int i = 0; i < arg.length; i++) {
			String classString = arg[i];
			Class c = Class.forName(classString);
			if (tf.acceptClass(c)) {
				if (!classes.contains(c)) {
					System.out.println("We accept : " + classString);

					classes.add(c);
				}
			} else
				System.out.println("We discard : " + classString);

		}
		return classes;
	}

}
