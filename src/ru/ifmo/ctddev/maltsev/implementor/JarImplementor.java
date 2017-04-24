package ru.ifmo.ctddev.maltsev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * The <tt>JarImplementor</tt> class implements {@link JarImpler} interface.
 * <p>
 * Class provides instruments for packing files in
 * runnable JAR archives with method
 * {@link #implementJar(Class, Path) implementJar}.
 * Before it would be done this method also will
 * generate the same necessary implementations of
 * class or interface as a {@link Implementor} class
 *
 * @version 1.0.0
 * @see JarImpler
 * @see Implementor
 */

public class JarImplementor extends Implementor implements JarImpler{

    /**
     * Generates <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Created class has name same to the name of the type token with <tt>Impl</tt> suffix added.
     *
     * @param token - type token to create implementation for
     * @param jarFile - target <tt>.jar</tt> file
     * @throws ImplerException when implementation cannot be generated
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        File jarArchive = new File(jarFile.toString());
        jarArchive.getParentFile().mkdirs();
        if (!jarArchive.exists())
            try {
                jarArchive.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        File tempDirectory = new File(jarFile.getParent().toString(), "temp");
        try(OutputStream fileStream = Files.newOutputStream (jarFile);
            JarOutputStream jarOutputStream = new JarOutputStream (fileStream, createManifest (className + ".class"));) {
            Implementor implementor = new Implementor();
            implementor.implement(token, tempDirectory.toPath());
            compileClass(implementor.classFile);

            ZipEntry entry = new ZipEntry(token.getPackage().getName().replace(".", "/") + "/" + implementor.className + ".class");
            jarOutputStream.putNextEntry(entry);
            addJarEntry(implementor.classFile, jarOutputStream);
        } catch (IOException e) {
            System.out.flush();
            e.printStackTrace();
        }
    }

    /**
     * Compiles java file.
     * <p>
     * This method compiles file with <i>.java</i> extension
     * to a byte-code files with <i>.class</i> extension by
     * using a default system {@link JavaCompiler}
     *
     * @param file - descriptor of file for compilation
     * @throws IOException when impossible to close file manager
     */
    private void compileClass(File file) throws IOException {
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);
        Iterable <? extends JavaFileObject> unit
                = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(file.getAbsolutePath()));
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, null, null, null, unit);
        task.call();
        fileManager.close();
    }

    /**
     * Add file to a JAR archive.
     *
     * @param file - descriptor of {@link File} to write in JAR
     * @param stream - pointer to {@link JarOutputStream} for writing
     */
    private void addJarEntry(File file, JarOutputStream stream) {
        File output = new File(file.getParentFile(), className + ".class");
        try(FileInputStream inputStream = new FileInputStream(output);) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int k;
            while ((k = inputStream.read(buffer)) != -1)
                stream.write(buffer, 0, k);
            stream.flush();
        } catch (IOException e) {
            System.out.println("Failed to write in a JarOutputStream");
        }
    }

    /**
     * Creates manifest for class with given name.
     *
     * @param className - name of the class which this method creates manifest for
     * @return Manifest for class with given name
     */
    private Manifest createManifest(String className) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, className);
        return manifest;
    }
}