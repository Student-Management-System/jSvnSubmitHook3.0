package de.uni_hildesheim.sse.test.utils.oo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.WrappedClass;
import de.uni_hildesheim.sse.test.suite.AbstractJavaTestSuite;

/**
 * Wrapper to execute user defined classes.
 * This class can be used to test the behavior of user defined classes. The tests will abort after the first error is
 * detected and will than return a meaningful error message to the student.
 * @author El-Sharkawy
 *
 */
public class ObjectExecuter {
    private static final String LINEFEED = "\n";
    
    /**
     * Represents a method/constructor call.
     * @author El-Sharkawy
     *
     */
    private static class MethodCall {
        private String methodName;
        private Object[] parameters;
        
        /**
         * Sole constructor.
         * @param methodName The name of the called method. Maybe <tt>null</tt> in case of the constructor.
         * @param parameters The parameters passed to the method. Maybe <tt>null</tt>.
         */
        private MethodCall(String methodName, Object[] parameters) {
            this.methodName = methodName;
            this.parameters = parameters;
        }
        
        /**
         * Appends the parameter list (including surrounding parenthesis) to an error message.
         * @param errorMsg The message where to append the parameter list.
         */
        private void printParameters(StringBuffer errorMsg) {
            errorMsg.append("(");
            if (null != parameters && parameters.length > 0) {
                if (parameters[0] instanceof CharSequence) {
                    errorMsg.append("\"");
                    errorMsg.append(parameters[0]);
                    errorMsg.append("\"");
                } else {
                    errorMsg.append(parameters[0]);
                }
                for (int i = 1; i < parameters.length; i++) {
                    errorMsg.append(", ");
                    if (parameters[i] instanceof CharSequence) {
                        errorMsg.append("\"");
                        errorMsg.append(parameters[i]);
                        errorMsg.append("\"");
                    } else {
                        errorMsg.append(parameters[i]);
                    }
                }
            }
            errorMsg.append(");");
        }
    }
    
    private String packageName;
    private String className;
    private Object userdefinedObj;
    private String file;
    private boolean isMandatory;
    private boolean isWorking;
    private List<MethodCall> callHistory;
    
    /**
     * Creates a new ObjectExecuter for testing runtime behavior.
     * Errors will be reported as mandatory failures.
     * @param packageName The package name of the user defined class. Should not be <tt>null</tt>.
     *     Package name check will be case sensitive and must be in java syntax (e.g. <tt>java.util</tt>).
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param parameters Optional list of parameters, can be <tt>null</tt>.
     */
    public ObjectExecuter(String packageName, String className, Object... parameters) {
        this(true, packageName, className, parameters);
    }
    
    /**
     * Creates a new ObjectExecuter for testing runtime behavior.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param packageName The package name of the user defined class. Should not be <tt>null</tt>.
     *     Package name check will be case sensitive and must be in java syntax (e.g. <tt>java.util</tt>).
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param parameters Optional list of parameters, can be <tt>null</tt>.
     */
    public ObjectExecuter(boolean isMandatory, String packageName, String className, Object... parameters) {
        this.packageName = packageName;
        this.className = className;
        this.isMandatory = isMandatory;
        this.callHistory = new ArrayList<MethodCall>();

        Set<WrappedClass> javaClasses = OOAnalyzerUtils.getClasses(packageName, className);
        if (javaClasses != null && javaClasses.size() == 1) {
            Iterator<WrappedClass> itr = javaClasses.iterator();
            WrappedClass jClass = itr.hasNext() ? itr.next() : null;
            file = null != jClass ? jClass.getFileName() : null;
        }
        
        Class<?> clazz = OOAnalyzerUtils.getUserDefinedType(packageName, className);
        if (null != clazz) {
            userdefinedObj = OOExecuterUtils.initializeClass(clazz, parameters);
            isWorking = null != userdefinedObj;
            if (isWorking) {
                addCall(null, parameters);
            }
        }
    }
    
    /**
     * Switches whether the next tests are mandatory or not.
     * @param isMandatory <tt>true</tt> will block the submission, <tt>false</tt> are only hints.
     */
    public void setMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }
    
    /**
     * Stores the called methods. Must be called inside the constrcutor/invoke methods.
     * @param methodName The name of the called method. Maybe <tt>null</tt> in case of the constructor.
     * @param parameters The parameters passed to the method. Maybe <tt>null</tt>.
     * @return The added/created MethodCall.
     */
    private MethodCall addCall(String methodName, Object... parameters) {
        MethodCall currentCall = new MethodCall(methodName, parameters);
        callHistory.add(currentCall);
        
        return currentCall;
    }
    
    /**
     * Stores the called methods. Must be called inside the constrcutor/invoke methods.
     * @param methodName The name of the called method. Maybe <tt>null</tt> in case of the constructor.
     * @param parameters The parameters passed to the method. Maybe <tt>null</tt>.
     * @return The added/created MethodCall.
     */
    private MethodCall addCall(String methodName, Parameter... parameters) {
        MethodCall call = null;
        if (null != parameters && parameters.length > 0) {
            Object[] values = new Object[parameters.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = parameters[i].getValue();
                call = addCall(methodName, values);
            }
        } else {
            call = addCall(methodName, (Object) null);
        }
        
        return call;
    }
    
    /**
     * Invokes a method.
     * This method should only be used, if the parameter types differ from the parameter values, i.g. inherited classes
     * are used.
     * @param methodName The name of the method, must not be <tt>null</tt>.
     * @param expectedResult The expected result of the invocation or <tt>void.class</tt> for a void method.
     * @param parameters The parameters of the method invocation.
     * @see #invoke(String, Object, Object...)
     */
    public void invoke(String methodName, Object expectedResult, Parameter[] parameters) {
        if (isWorking) {
            Object realResult = OOExecuterUtils.callMethod(userdefinedObj, methodName, parameters);
            MethodCall currentCall = addCall(methodName, parameters);
            
            if (expectedResult != void.class && !expectedResult.equals(realResult)) {
                isWorking = false;
                createErrorMsg(methodName, expectedResult, realResult, currentCall);
            }
        }
    }
    
    /**
     * Invokes a method.
     * @param methodName The name of the method, must not be <tt>null</tt>.
     * @param expectedResult The expected result of the invocation or <tt>void.class</tt> for a void method.
     * @param parameters The parameters of the method invocation.
     */
    public void invoke(String methodName, Object expectedResult, Object... parameters) {
        if (isWorking) {
            Object realResult = OOExecuterUtils.callMethod(userdefinedObj, methodName, parameters);
            MethodCall currentCall = addCall(methodName, parameters);
            
            if (expectedResult != void.class && !expectedResult.equals(realResult)) {
                isWorking = false;
                createErrorMsg(methodName, expectedResult, realResult, currentCall);
            }
        }
    }

    /**
     * Create a meaningful error message if an error was detected.
     * @param methodName The name of the method which was called last.
     * @param expectedResult The expected result or <tt>void.class</tt> if no result was expected.
     * @param realResult The result which was created by the students implementation.
     * @param currentCall The last method call.
     */
    private void createErrorMsg(String methodName, Object expectedResult, Object realResult, MethodCall currentCall) {
        StringBuffer errorMsg = new StringBuffer();
        if (isMandatory) {
            errorMsg.append("Fehler: ");
        } else {
            errorMsg.append("Warnung: ");
        }
        errorMsg.append("Beim Aufruf von \"");
        errorMsg.append(methodName);
        currentCall.printParameters(errorMsg);
        errorMsg.append("\" in der Klasse \"");
        errorMsg.append(packageName);
        errorMsg.append(".");
        errorMsg.append(className);
        errorMsg.append("\" wurde der falsche Wert zurück gegeben. Erwartet wurde \"");
        errorMsg.append(expectedResult);
        errorMsg.append("\" Ihre Methode lieferte aber \"");
        errorMsg.append(realResult);
        errorMsg.append("\". Nachfolgend der Verlauf aller getesteten Aufrufe:");
        errorMsg.append(LINEFEED);
        printCallHistory(errorMsg);
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, file, -1));
    }
    
    /**
     * Appends the history method class which where invoked by the executer, including the constructor call.
     * @param errorMsg the existing error Message where to append the history.
     */
    private void printCallHistory(StringBuffer errorMsg) {
        // Constructor call
        MethodCall constructorCall = callHistory.get(0);
        if (null != constructorCall) {
            errorMsg.append(className);
            errorMsg.append(" testObjekt = new ");
            errorMsg.append(className);
            constructorCall.printParameters(errorMsg);
        }
        
        // Method calls
        for (int i = 1; i < callHistory.size(); i++) {
            MethodCall methodCall = callHistory.get(i);
            if (null != methodCall) {
                errorMsg.append(LINEFEED);
                errorMsg.append("testObjekt.");
                errorMsg.append(methodCall.methodName);
                methodCall.printParameters(errorMsg);
            }
        }
    }
}
