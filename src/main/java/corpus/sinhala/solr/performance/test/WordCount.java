package corpus.sinhala.solr.performance.test;
/**
 *
 * @author lahiru
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.codehaus.stax2.XMLOutputFactory2;

public class WordCount {
    
    String pathToXML, destinationPath;
    int authorCount;
    int contentCount;
    int topicCount;
    
    void resetCounts() {
        authorCount = 0;
        contentCount = 0;
        topicCount = 0;
    }

    public WordCount(String pathToXML, String destinationPath) {
        if(pathToXML.charAt(pathToXML.length() - 1) != '/') {
            System.out.println("err");
            pathToXML = pathToXML + "/";
        }
        if(destinationPath.charAt(destinationPath.length() - 1) != '/') {
            System.out.println("err");
            destinationPath = destinationPath + "/";
        }
        this.pathToXML = pathToXML;
        this.destinationPath = destinationPath;
        
        authorCount = 0;
        contentCount = 0;
        topicCount = 0;
    }
    
    void getWordCount() {
        
    }
        
    public void parseXMLFiles() {
        resetCounts();
        PrintWriter writer = null;
        
        LinkedList<String> xmlFiles = getXMLFiles();
        for(String fileName : xmlFiles) {
            try {
                System.out.println("parsing file : " + fileName);
                parseFile(fileName);
            } catch (Exception ex) {
                System.out.println("error while parsing file: " + pathToXML + fileName);
                Logger.getLogger(WordCount.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // write to metadata.txt file
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(destinationPath + "metadata.txt")));
                writer.write("topic:" + topicCount + "\n");
                writer.write("author:" + authorCount + "\n");
                writer.write("content:" + contentCount + "\n");
                writer.write("\n");                
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(WordCount.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private LinkedList<String> getXMLFiles() {
        resetCounts();
        String dir = pathToXML;
        File folder = new File(dir);
        File fList[] = folder.listFiles();
        LinkedList<String> xmlFileList = new LinkedList<String>();
        for(File f: fList) {
            if(f.getName().endsWith(".xml"))
                xmlFileList.addLast(f.getName());
        }
        return xmlFileList;
    }
    
    void parseFile(String fileName) throws Exception {
        int fileCounter = 0;
        OMElement documentElement = new StAXOMBuilder(pathToXML + fileName).getDocumentElement();
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement(new QName("root"));

        Iterator children = documentElement.getChildElements();
            while(children.hasNext()) {
                OMElement element = (OMElement)children.next();
                
                OMElement link  = element.getFirstChildWithName(new QName("link"));
                OMElement topic = element.getFirstChildWithName(new QName("topic"));
                OMElement date  = element.getFirstChildWithName(new QName("date"));
                Iterator date_children = date.getChildElements();
                    
                OMElement date_element = (OMElement)date_children.next();
                String year = date_element.getText().trim();
                date_element = (OMElement)date_children.next();
                String month = date_element.getText().trim();
                date_element = (OMElement)date_children.next();
                String day = date_element.getText().trim();
                String dateString = year + "-" + month + "-" + day + "T00:00:00:Z";
                
                OMElement author = element.getFirstChildWithName(new QName("author"));
                OMElement content = element.getFirstChildWithName(new QName("content"));
                
                OMElement add = factory.createOMElement(new QName("add"));
                OMElement doc = factory.createOMElement(new QName("doc"));
                
                // generate ID for the xml object
                OMElement idField = factory.createOMElement(new QName("field"));
                idField.addAttribute("name", "id", null);
                String id = "i" + fileName.substring(2, fileName.length()-4).replaceAll("-", "");
                idField.setText(id);
                
                OMElement linkField = factory.createOMElement(new QName("field"));
                linkField.addAttribute("name", "link", null);
                linkField.setText(new QName(link.getText()));
                
                OMElement topicField = factory.createOMElement(new QName("field"));
                topicField.addAttribute("name", "topic", null);
                topicField.setText(new QName(topic.getText()));
                
                OMElement dateField = factory.createOMElement(new QName("field"));
                dateField.addAttribute("name", "date", null);
                dateField.setText(new QName(dateString));
                
                OMElement authorField = factory.createOMElement(new QName("field"));
                authorField.addAttribute("name", "author", null);
                authorField.setText(new QName(author.getText()));
                
                OMElement contentField = factory.createOMElement(new QName("field"));
                contentField.addAttribute("name", "content", null);
                contentField.setText(new QName(content.getText()));
                
                authorCount += wordCount(author.getText());
                topicCount += wordCount(topic.getText());
                contentCount += wordCount(content.getText());
                
                doc.addChild(idField);
                doc.addChild(linkField);
                doc.addChild(topicField);
                doc.addChild(dateField);
                doc.addChild(authorField);
                doc.addChild(contentField);
                add.addChild(doc);
                root.addChild(add);
        }
        String rawXMLWithoutExtension = fileName.substring(0, fileName.length() - 4);
        String filePrefix = "out-" + rawXMLWithoutExtension;
        String outpath = destinationPath + filePrefix + ".xml";
        OutputStream out = new FileOutputStream(outpath);
        XMLStreamWriter writer = XMLOutputFactory2.newInstance().createXMLStreamWriter(out);
        root.serialize(writer);
        writer.flush();
        fileCounter++;
    }
    
    int wordCount(String data) {
        StringTokenizer st = new StringTokenizer(data, "[\u002E\u003F\u0021\u0020]");
        return st.countTokens();
    }
    
    public void countFromAllParsedFiles(String path) {
        PrintWriter writer = null;
        LinkedList<String> xmlFiles = Util.getXMLFiles(path);
        for(String fileName : xmlFiles) {            
            try {
                // write to metadata.txt file
                countWordsInParsedFile(path + fileName);
            } catch (Exception ex) {
                System.out.println("error parsing at : " + path + fileName);
                Logger.getLogger(WordCount.class.getName()).log(Level.SEVERE, null, ex);
            }
//            try {
//                writer = new PrintWriter(new BufferedWriter(new FileWriter(destinationPath + "metadata.txt")));
//                writer.write("topic:" + topicCount + "\n");
//                writer.write("author:" + authorCount + "\n");
//                writer.write("content:" + contentCount + "\n");
//                writer.write("\n");                
//                writer.flush();
//                writer.close();
//            } catch (IOException ex) {
//                Logger.getLogger(WordCount.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            try {
//                writer = new PrintWriter(new BufferedWriter(new FileWriter(destinationPath + "metadata.txt")));
//                writer.write("topic:" + topicCount + "\n");
//                writer.write("author:" + authorCount + "\n");
//                writer.write("content:" + contentCount + "\n");
//                writer.write("\n");                
//                writer.flush();
//                writer.close();
//            } catch (IOException ex) {
//                Logger.getLogger(WordCount.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }
    
    void countWordsInParsedFile(String file) throws Exception {
        resetCounts();
        OMElement documentElement = new StAXOMBuilder(file).getDocumentElement();
        OMElement add  = documentElement.getFirstElement().getFirstElement();

        Iterator children = add.getChildElements();
        while(children.hasNext()) {
                OMElement doc = (OMElement)children.next();
                System.out.println(doc.getFirstChildWithName(new QName("field")).getText());
                Iterator fields = doc.getChildElements();
                
                LinkedList<Integer> counts = new LinkedList<Integer>();
                while(fields.hasNext()) {
                    System.out.println("-----------_");
                    OMElement field = (OMElement)fields.next();
                    counts.addLast(wordCount(field.getText()));
                    //System.out.println(field.getText());
                }
                
//                authorCount  += counts.get(4);
//                topicCount   += counts.get(2);
//                contentCount += counts.get(5);
        }
        System.out.println("author: " + authorCount);
        System.out.println("topic: " + topicCount);
        System.out.println("content: " + contentCount);
    }
    
    public static void main(String[] args) {
        WordCount x = new WordCount("-", "-");
        x.countFromAllParsedFiles("/home/lahiru/solr/parsed/set1/set1/");
//        x.parseXMLFiles();
    }
    
}
