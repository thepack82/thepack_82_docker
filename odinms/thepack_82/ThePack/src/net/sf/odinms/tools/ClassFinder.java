package net.sf.odinms.tools;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClassFinder.class);
    List<JarFile> jars = new LinkedList<JarFile>();
    List<File> dirs = new LinkedList<File>();

    public ClassFinder() {
        String classpath = System.getProperty("java.class.path");
        String[] splittedPath = classpath.split(File.pathSeparator);
        for (String cpe : splittedPath) {
            File cpeFile = new File(cpe);
            if (cpeFile.isDirectory()) {
                dirs.add(cpeFile);
            } else {
                try {
                    jars.add(new JarFile(cpeFile));
                } catch (IOException e) {
                    log.error("ERROR", e);
                }
            }
        }
    }

    private void addClassesInFolder(List<String> classes, File folder, String packageName, boolean recurse) {
        for (File f : folder.listFiles()) {
            if (!f.isDirectory()) {
                if (f.getName().endsWith(".class")) {
                    classes.add(packageName + "." + f.getName().substring(0, f.getName().length() - 6));
                }
            } else if (f.isDirectory() && recurse) {
                addClassesInFolder(classes, f, packageName + "." + f.getName(), recurse);
            }
        }
    }

    public String[] listClasses(String packageName, boolean recurse) {
        List<String> ret = new LinkedList<String>();
        final String fileSystemPackagePath = packageName.replace('.', File.separatorChar);
        for (File dir : dirs) {
            File subfolder = new File(dir, fileSystemPackagePath);
            if (subfolder.exists() && subfolder.isDirectory()) {
                addClassesInFolder(ret, subfolder, packageName, recurse);
            }
        }
        final String jarPackagePath = packageName.replace('.', '/');
        for (JarFile jar : jars) {
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
                String entryName = (entries.nextElement()).getName();
                if (entryName.endsWith(".class") && entryName.startsWith(jarPackagePath)) {
                    int lastSlash = entryName.lastIndexOf('/');
                    if (lastSlash <= jarPackagePath.length() || recurse) {
                        String path = entryName.substring(0, lastSlash);
                        String className = entryName.substring(lastSlash + 1, entryName.length() - 6);
                        ret.add(path.replace('/', '.') + "." + className);
                    }
                }
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    public void dispose() {
        for (JarFile jar : jars) {
            try {
                jar.close();
            } catch (IOException e) {
                log.error("THROW", e);
            }
        }
    }
}