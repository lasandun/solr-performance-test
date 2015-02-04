/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package corpus.sinhala.solr.performance.test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author lahiru
 */
public class UploadTimeTest {
    
    public UploadTimeTest() {
        
    }
    
    public UploadTimeTest(String s) {
        
    }
    
    private LinkedList<String> getXMLFiles() {
        String dir = "/home/lahiru/Desktop/parsed/";
        File folder = new File(dir);
        File fList[] = folder.listFiles();
        LinkedList<String> xmlFileList = new LinkedList<String>();
        for(File f: fList) {
            if(f.getName().endsWith(".xml"))
                xmlFileList.addLast(f.getAbsolutePath());
        }
        for(String s : xmlFileList) {
            System.out.println(s);
        }
        return xmlFileList;
    }
    
    public void testUploadTime() throws IOException {
        
    }
    
    public static void main(String[] args) {
        String s = "00";
        int i = Integer.parseInt(s);
        System.out.println(i);
    }
}
