@ECHO off
SET DOCS="doc"
@RD /S /Q %DOCS%
javadoc -d %DOCS% src\ru\ifmo\ctddev\maltsev\implementor\Implementor.java -link https://docs.oracle.com/javase/8/docs/api -private -classpath artifacts\JarImplementorTest.jar;lib\junit-4.11.jar;lib\hamcrest-core-1.3.jar;lib\jsoup-1.8.1.jar;lib\quickcheck-0.6.jar