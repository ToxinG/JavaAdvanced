package ru.ifmo.ctddev.maltsev.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {

    private static final long FNV_MOD = (1L << 32);
    private static final long FNV_INIT = 2166136261L;
    private static final long FNV_PRIME = 16777619;
    private static final int REQUIRED_BITS = (1 << 8) - 1;
    private static final int BUFFER_LENGTH = 4096;

    public static void main(String args[]) {
        try {
            FileInputStream fileInputStream = new FileInputStream(args[0]);
            FileOutputStream fileOutputStream = new FileOutputStream(args[1]);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8 ))) {
                String pathString;
                while ((pathString = br.readLine()) != null) {
                    processPath(Paths.get(pathString), pw);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Not enough arguments. Expected 2, found " + args.length);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void processPath(Path path, PrintWriter pw) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    pw.format("%08x %s\n", hashFNV(file), file.toString());
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    pw.format("%08x %s\n", 0, file.toString());
                    System.err.println(e.getMessage() + "(Failed to process this path)");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static long hashFNV(Path path) {
        long hashSum = FNV_INIT;
        try {
            InputStream input = new FileInputStream(path.toFile());
            byte[] b = new byte[BUFFER_LENGTH];
            int size = 0;
            while ((size = input.read(b, 0, b.length)) >= 0) {
                for (int i = 0; i < size; i++) {
                    hashSum = (hashSum * FNV_PRIME) % FNV_MOD ^ ((long) b[i] & REQUIRED_BITS);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage() + " (No such file found)");
            return 0;
        } catch (IOException e) {
            System.err.println(e.getMessage() + " (Failed to read from file)");
            return 0;
        }
        return hashSum;
    }
}

/*
package ru.ifmo.ctddev.maltsev.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {

    private static final long FNV_MOD = (1L << 32);
    private static final long FNV_INIT = 2166136261L;
    private static final long FNV_PRIME = 16777619;
    private static final int REQUIRED_BITS = (1 << 8) - 1;
    private static final int BUFFER_LENGTH = 4096;

    public static void main(String args[]) {
        try {
            FileInputStream fileInputStream;
            FileOutputStream fileOutputStream;
            BufferedReader br;
            PrintWriter pw;
            try {
                fileInputStream = new FileInputStream(args[0]);
            } catch (FileNotFoundException e) {
                System.err.println("Failed to open input file");
                return;
            }
            try {
                fileOutputStream = new FileOutputStream(args[1]);
            } catch (FileNotFoundException e) {
                System.err.println("Failed to create output file");
                return;
            }
            try {
                br = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
                pw = new PrintWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
                String pathString;
                while ((pathString = br.readLine()) != null) {
                    processPath(Paths.get(pathString), pw);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Not enough arguments. Expected 2, found " + args.length);
        }
    }

    private static void processPath(Path path, PrintWriter pw) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    pw.format("%08x %s\n", hashFNV(file), file.toString());
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    pw.format("%08x %s\n", 0, file.toString());
                    System.err.println(e.getMessage() + "(Failed to process this path)");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static long hashFNV(Path path) {
        long hashSum = FNV_INIT;
        try {
            InputStream input = new FileInputStream(path.toFile());
            byte[] b = new byte[BUFFER_LENGTH];
            int size = 0;
            while ((size = input.read(b, 0, b.length)) >= 0) {
                for (int i = 0; i < size; i++) {
                    hashSum = (hashSum * FNV_PRIME) % FNV_MOD ^ ((long) b[i] & REQUIRED_BITS);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage() + " (No such file found)");
            return 0;
        } catch (IOException e) {
            System.err.println(e.getMessage() + " (Failed to read from file)");
            return 0;
        }
        return hashSum;
    }
}
*/