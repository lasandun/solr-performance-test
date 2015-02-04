/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package corpus.sinhala.solr.performance.test;


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

public class XMLParser {
    
    String pathToXML, destinationPath;
    //int idPrefix;

    public XMLParser() {
        pathToXML = SysProperty.getProperty("rawXMLPath");
        destinationPath = SysProperty.getProperty("parsedXMLPath");
        //idPrefix = 0;
    }
    
    public XMLParser(String pathToXML, String destinationPath) {
        this.pathToXML = pathToXML;
        this.destinationPath = destinationPath;
        //idPrefix = 0;
    }
    
    public void parseXMLFiles() {
        PrintWriter writer = null;
        try {
             writer = new PrintWriter(new BufferedWriter(new FileWriter(SysProperty.getProperty("solrParserMetadata"), true)));
        } catch (IOException ex) {
            Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        LinkedList<String> xmlFiles = getXMLFiles();
        for(String fileName : xmlFiles) {
            try {
                System.out.println("parsing file : " + fileName);
                parseFile(fileName, writer);
            } catch (Exception ex) {
                System.out.println("error while parsing file: " + pathToXML + fileName);
                Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private LinkedList<String> getXMLFiles() {
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
    
    void parseFile(String fileName, PrintWriter metadataWriter) throws Exception {
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
                
                // print metadata
                if(metadataWriter != null) {
                    metadataWriter.write(fileName + "\n");
                    metadataWriter.write("topic:" + wordCount(topic.getText()) + "\n");
                    metadataWriter.write("author:" + wordCount(author.getText()) + "\n");
                    metadataWriter.write("content:" + wordCount(content.getText()) + "\n");
                    metadataWriter.write("\n");
                }
                
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
}