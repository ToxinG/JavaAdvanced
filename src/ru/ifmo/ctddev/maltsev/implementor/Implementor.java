package ru.ifmo.ctddev.maltsev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The <tt>Implementor</tt> class implements {@link Impler} interface.
 * <p>
 * Class gives instruments for generating compilable
 * representation of java {@link Class}.
 *
 * With method {@link #implement(Class, Path) implement}
 * can be generated stub of {@link Class} that can be
 * compiled without errors. All other methods are needed
 * for supporting it.
 * <p>
 * @version 1.0.0
 * @see Impler
 *
 * */
public class Implementor implements Impler {

    /**
     * This field is a type token to be implemented.
     */
    private Class<?> processedType;
    /**
     * This field is a simple name of implementation class.
     */
    protected String className = "";
    /**
     * This field contains a pointer to {@link OutputStream} as a value.
     * It should be initialized before writing implementation.
     */
    private Writer writer;
    /**
     * Directory of the folder which implementation is to be written to.
     */
    protected File directory = null;
    /**
     * Directory of file with implementation.
     */
    protected File classFile = null;
    /**
     * Size of buffer used for writing in JAR archive
     */
    protected static final int BUFFER_SIZE = 2048;


    /**
     * Generates code of class implementing an interface or a class specified by given <tt>token</tt>
     * <p>
     * Created class has name same to the name of the type token with <tt>Impl</tt> suffix added.
     * Generated source code should be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and have correct file name.
     *
     * @param token - type token to create implementation for
     * @param root - root directory
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     * generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (root == null) {
            throw new ImplerException("Wrong argument: given path is null");
        }
        if (token == null) {
            throw new ImplerException("Wrong argument: given token is null");
        }
        if (token.isPrimitive() || token.isArray() || token == Enum.class) {
            throw new ImplerException("Wrong argument: token should be an interface or a class");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Wrong argument: a final class cannot be extended");
        }
        processedType = token;
        String path = processedType.getPackage().getName().replace(".", File.separator);
        directory = new File(root.toFile(), path);
        className = processedType.getSimpleName() + "Impl";
        try {
            directory.mkdirs();
            classFile = new File(directory, className + ".java");
            writer = Files.newBufferedWriter(Paths.get(classFile.getAbsolutePath()), StandardCharsets.UTF_8);
            printPackage();
            printClassHeader();
            printConstructors();
            printMethods(processedType, new HashSet<String>());
            writer.write("}\n");
        } catch (IOException e) {
            throw new ImplerException("Failed to write in output file");
        } catch (SecurityException se) {
            throw new ImplerException ("Failed to create source folder");
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                throw new ImplerException("Failed to complete writing");
            }
        }
    }

    /**
     * Generates string representation of the package of implemented class.
     *
     * @throws IOException when impossible to write packages
     */
    private void printPackage() throws IOException {
        if (processedType.getPackage() != null) {
            writer.write("package " + processedType.getPackage().getName() + ";\n\n");
        }
    }

    /**
     * Generates string representation of the header of implemented class or interface.
     * Header is the string which contains access modifiers (this method prints <tt>public</tt>)
     * and names of implemented interfaces and extended class.
     *
     * @throws IOException when impossible to write header
     */
    private void printClassHeader() throws IOException {
        writer.write("public class " + className + " ");

        if (processedType.isInterface()) {
            writer.write("implements " + processedType.getSimpleName());
        } else {
            writer.write("extends " + processedType.getSimpleName());
        }
        writer.write (" {\n\n");
    }

    /**
     * Generates string representation of public constructors of implemented class.
     *
     * @throws IOException when impossible to write constructors
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation generation fails
     */
    private void printConstructors() throws IOException, ImplerException {
        boolean publicConstructorExists = false;
        for (Constructor <?> c : processedType.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                writer.write("\t" + writeModifiers(c.getModifiers()) + " " + className + " (");
                writer.write(writeParameters(c.getParameterTypes(), c.isVarArgs()) + ") ");
                writer.write(writeExceptions(c.getExceptionTypes()));
                writer.write(" {\t\tsuper (");

                for (int i = 0; i < c.getParameterCount(); i++) {
                    writer.write("arg" + i + ((i < c.getParameterCount() - 1) ? ", " : ");"));
                }

                if (c.getParameterCount() == 0) {
                    writer.write(");");
                }

                writer.write("\n\t}\n\n");
                publicConstructorExists = true;
            }
        }

        if (!publicConstructorExists && !processedType.isInterface()) {
            throw new ImplerException("Failed to create any public constructors");
        }
    }

    /**
     * Generates string representation for each unimplemented method
     * of implemented (or extended) {@link Class}.
     * <p>
     * This method generates totally completed block of code
     * of stubs for unimplemented methods in interface or
     * abstract class. To prevent double-implementation
     * should be given an {@link Set} with signatures
     * (That are generated in {@link #writeSignature(Method)
     * writeSignature}) of {@link Method}s witch is already done.
     * <p>
     * The result of this method will be written in {@link #writer writer}

     * @param c - type token of {@link Class} methods of which should be implemented
     * @param methodSet - set of signatures of methods that are already written
     * @throws IOException when impossible to write methods
     */
    private void printMethods(Class <?> c, Set<String> methodSet) throws IOException {
        if (c == null)
            return;

        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods) {
            String signature = writeSignature(method);
            if (!methodSet.contains(signature)) {
                if (Modifier.isAbstract(method.getModifiers()) && !Modifier.isFinal(method.getModifiers()) &&
                        !Modifier.isPrivate(method.getModifiers())) {
                    writer.write(writeMethod(method));
                }
                methodSet.add(signature);
            }
        }

        printMethods(c.getSuperclass(), methodSet);
        for (Class <?> cl : c.getInterfaces()) {
            printMethods(cl, methodSet);
        }
    }

    /**
     * Generates string representation of given {@link Method}.
     * <p>
     * The result is:
     * <b>{@code [modifiers] [return type] [method name] ([arguments]) ['throws' declaration] { return [value]; }}</b>
     *
     * @param m - given {@link Method} witch should be implemented
     * @return String representation of given {@link Method}
     */
    private String writeMethod (Method m) {
        StringBuilder res = new StringBuilder();
        res.append("\t");
        Annotation[] annotations = m.getDeclaredAnnotations();
        for (Annotation a : annotations) {
            res.append("@" + a.annotationType().getSimpleName() + " ");
        }

        res
                .append("\n\t")
                .append(writeModifiers(m.getModifiers()))
                .append(" ")
                .append(writeType(m.getReturnType()))
                .append(" ")
                .append(m.getName())
                .append(" (")
                .append(writeParameters(m.getParameterTypes(), m.isVarArgs()))
                .append(") ")
                .append(writeExceptions(m.getExceptionTypes()))
                .append(" {\n\t\t")
                .append(writeReturnValue(m))
                .append("\n\t}\n");
        return res.toString();
    }

    /**
     * Generates string representation of modifiers of given {@link Method}.
     *
     * @param m - given {@link Method} this method writes modifiers for
     * @return String with modifiers representation
     */
    private String writeModifiers(int m) {
        return Modifier.toString(m & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT);
    }

    /**
     * Generates string representation of parameters of given {@link Method}.
     *
     * @param types - array of types arguments of some {@link Method}
     * @param varArg - flag if the last argument is varArg
     * @return String with some {@link Method}'s arguments representation
     */
    private String writeParameters(Class <?> [] types, boolean varArg) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0)
                res.append(", ");
            if (i == types.length - 1 && varArg) {
                res.append(writeType(types[i].getComponentType()) + "... arg" + i);
                break;
            }
            res.append(writeType(types[i]) + " arg" + i);
        }
        return res.toString();
    }

    /**
     * Generates string representation of given type.
     * <p>
     * If it is a raw type then will be returned string
     * with the same name, but if it is an array then
     * will be added {@code "[]"} for each dimension
     *
     * @param type - type-token of representing {@link java.lang.Class Class}
     * @return String with {@link java.lang.Class Class} type representation
     */
    private String writeType(Class <?> type) {
        StringBuilder res = new StringBuilder();
        while (type.isArray()) {
            res.append("[]");
            type = type.getComponentType();
        }
        return type.getName() + res.toString();
    }

    /**
     * Generates string of given declared method's exceptions.
     * <p>
     * If method has declared exceptions then will be
     * returned string of format: {@code throws [Exceptions names,]...}
     * If method has more than one declared exception then they
     * will be separated with ',' char.
     *
     * @param types - array of possible exceptions for such method
     * @return String that declares all possible exceptions
     */
    private String writeExceptions (Class <?> [] types) {
        if (types.length == 0)
            return "";
        StringBuilder res = new StringBuilder();
        res.append("throws ");
        for (int i = 0; i < types.length; i++) {
            if (i > 0)
                res.append(", ");
            res.append(types[i].getName());
        }
        return res.toString();
    }

    /**
     * Generates string of return statement of given {@link Method}.
     *
     * @param m - the pointer for {@link Method} that should return some value
     * @return Correct string of return statement for given {@link Method}
     */
    private String writeReturnValue (Method m) {
        if (m.getReturnType().isPrimitive()) {
            if(m.getReturnType().equals(Boolean.TYPE)) {
                return "return false;";
            } else if (m.getReturnType().equals(Void.TYPE)) {
                return "return;";
            } else {
                return "return 0;";
            }
        }
        return "return null;";
    }

    /**
     * Generates string representation of signature of given {@link Method}.
     *
     * @param m - the pointer for {@link Method} that this method writes signature for
     * @return String of signature for given {@link Method}
     */
    private String writeSignature (Method m) {
        return m.getName() + " (" + writeParameters(m.getParameterTypes(), m.isVarArgs()) + ")";
    }


    /**
     * Creates java class and depends on command line arguments
     * puts it in jar file
     * <br>
     * Usage:
     * <br>
     * to create java class
     * <br>
     * java Implementor &lt;classname&gt;
     * <br>
     * to create jar file
     * <br>
     * java -jar &lt;classname&gt; &lt;filename&gt;
     * <br>
     * @param args program arguments
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 1 && args.length != 3) || (args.length == 1 && args[0] != null)
                || (args.length == 3 && args[0] != null && args[1] != null && args[2] != null)) {
            System.err.println("Usage: java Implementor <classname>\n        java -jar <classname> <filename>");
        }
        boolean jarFlag = args[0].equals("-jar");

        try {
            String className;
            if (jarFlag) {
                className = args[1];
            } else {
                className = args[0];
            }
            Class c = Class.forName(className);
            if (jarFlag) {
                (new JarImplementor()).implementJar(c, Paths.get(args[2]));
            } else {
                (new Implementor()).implement(c, Paths.get("."));
            }
        } catch (ClassNotFoundException | ImplerException e) {
            e.printStackTrace();
        }
    }
}