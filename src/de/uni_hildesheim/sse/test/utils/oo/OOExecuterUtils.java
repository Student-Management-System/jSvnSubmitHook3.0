package de.uni_hildesheim.sse.test.utils.oo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Methods for instantiation of objects and method class.
 * @author El-Sharkawy
 *
 */
//checkstyle: stop exception type check
public class OOExecuterUtils {
    
    /**
     * Creates a new instance of the <tt>javaClass</tt>.
     * @param javaClass The class from which a new instance shall be created.
     * @param parameters An optional list of parameters which shall be passed to the constructor call.
     *     If the default constructor should be used, these parameters can be omitted.
     *     Wrapper classes will be transformed to primitive types.
     * @return The new instance of <tt>null</tt> in case of any (technical) errors.
     */
    public static Object initializeClass(Class<?> javaClass, Object... parameters) {
        Object instance = null;
        try {
            if (null != parameters && parameters.length > 0) {
                Class<?>[] parameterTypes = createParameterTypes(parameters);
                Constructor<?> constructor = javaClass.getDeclaredConstructor(parameterTypes);
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                instance = constructor.newInstance(parameters);
            } else {
                Constructor<?> constructor = javaClass.getDeclaredConstructor();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                instance = constructor.newInstance();
            }
        } catch (Exception exc) {
            /*
             * In case of any (technical) problems, do not report an error to the students
             * and accept their submission.
             */
        }
        
        return instance;
    }
    
    /**
     * Invokes <tt>object.methodName(parameters)</tt>.
     * @param object The object from where the method shall be called.
     * @param methodName The name of the method to be invoked.
     * @param parameters The parameters to be passed to the method. If the method has no parameters, tan please also
     *     pass no parameters to this method. Wrapper classes will be transformed to primitive types.
     * @return The return value of the method. Maybe <tt>null</tt> in case of a void method or in case of any
     *     (technical) errors.
     */
    public static Object callMethod(Object object, String methodName, Object... parameters) {
        Object returnValue = null;
        
        try {
            if (null != object && null != methodName) {
                Class<?> clazz = object.getClass();
                if (null != parameters && parameters.length > 0) {
                    Class<?>[] parameterTypes = createParameterTypes(parameters);
                    Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    returnValue = method.invoke(object, parameters);
                } else {
                    Method method = clazz.getDeclaredMethod(methodName);
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    returnValue = method.invoke(object);
                }
            }
        } catch (Exception exc) {
            /*
             * In case of any (technical) problems, do not report an error to the students
             * and accept their submission.
             */
        }
        
        return returnValue;
    }
    
    /**
     * Invokes <tt>object.methodName(parameters)</tt>.
     * @param object The object from where the method shall be called.
     * @param methodName The name of the method to be invoked.
     * @param parameters The parameters to be passed to the method. If the method has no parameters, tan please also
     *     pass no parameters to this method. Wrapper classes will be transformed to primitive types.
     * @return The return value of the method. Maybe <tt>null</tt> in case of a void method or in case of any
     *     (technical) errors.
     */
    public static Object callMethod(Object object, String methodName, Parameter... parameters) {
        Object returnValue = null;
        
        try {
            if (null != object && null != methodName) {
                Class<?> clazz = object.getClass();
                if (null != parameters && parameters.length > 0) {
                    Class<?>[] parameterTypes = new Class<?>[parameters.length];
                    Object[] parameterValues = new Object[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        parameterTypes[i] = parameters[i].getType();
                        parameterValues[i] = parameters[i].getValue();
                    }
                    
                    Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    returnValue = method.invoke(object, parameterValues);
                } else {
                    Method method = clazz.getDeclaredMethod(methodName);
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    returnValue = method.invoke(object);
                }
            }
        } catch (Exception exc) {
            /*
             * In case of any (technical) problems, do not report an error to the students
             * and accept their submission.
             */
        }
        
        return returnValue;
    }

    /**
     * Collects the class type objects for the given parameters. Wrapper classes will be transformed to primitive
     * types.
     * @param parameters Objects for which the data types shall be retrieved. Must not be <tt>null</tt>.
     * @return The corresponding data types for the given object values.
     */
    private static Class<?>[] createParameterTypes(Object... parameters) {
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameters[i].getClass();
            if (type == Integer.class) {
                type = int.class;
            } else if (type == Byte.class) {
                type = byte.class;
            } else if (type == Short.class) {
                type = short.class;
            } else if (type == Long.class) {
                type = long.class;
            } else if (type == Double.class) {
                type = double.class;
            } else if (type == Float.class) {
                type = float.class;
            } else if (type == Boolean.class) {
                type = boolean.class;
            } else if (type == Character.class) {
                type = char.class;
            }
            parameterTypes[i] = type;
        }
        return parameterTypes;
    }

}
