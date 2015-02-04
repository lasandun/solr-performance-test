/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package corpus.sinhala.solr.performance.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

/**
 *
 * @author lahiru
 */
public class Util {

    public static String addBackSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path = path + "/";
        }
        return path;
    }

    public static LinkedList<String> getDirectoryList(String path) {
        LinkedList<String> directoryList = new LinkedList<String>();
        File rootFolder = new File(path);
        String fList[] = rootFolder.list();
        for (String fileName : fList) {
            File f = new File(path + fileName);
            if (f.isDirectory()) {
                directoryList.addFirst(addBackSlash(f.getAbsolutePath()));
            }
        }
        return directoryList;
    }

    public static LinkedList<String> getXMLFiles(String dir) {
        File folder = new File(dir);
        File fList[] = folder.listFiles();
        LinkedList<String> xmlFileList = new LinkedList<String>();
        for (File f : fList) {
            if (f.getName().endsWith(".xml")) {
                xmlFileList.addLast(f.getName());
            }
        }
        return xmlFileList;
    }

    public static void main(String[] args) {
        LinkedList<String> list = Util.getDirectoryList("/home/lahiru/solr/parsed/");
        for (String x : list) {
            System.out.println(x);
        }
    }
    
    public static void clearSolrDataAndIndexes() throws Exception {
        URL query = new URL("http://localhost:8983/solr/update?stream.body=%3Cdelete%3E%3Cquery%3E*:*%3C/query%3E%3C/delete%3E&commit=true");
        URLConnection connection = query.openConnection();
        InputStream is = connection.getInputStream();
        is.close();
    }
    
    public static InputStream runCommand(String command) throws IOException {
        System.out.println(command);
        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        InputStream inputStream = p.getInputStream();
        return inputStream;
    }

}
