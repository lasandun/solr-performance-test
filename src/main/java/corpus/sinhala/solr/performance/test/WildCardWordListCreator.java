package corpus.sinhala.solr.performance.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import javax.xml.stream.XMLStreamWriter;
import org.codehaus.stax2.XMLOutputFactory2;
/**
 *
 * @author lahiru
 */
public class WildCardWordListCreator {
    int id;
    OMFactory factory = OMAbstractFactory.getOMFactory();
    OMElement root = factory.createOMElement(new QName("root"));
    OMElement add = factory.createOMElement(new QName("add"));
    

    public WildCardWordListCreator() {
        id = 10000000;
    }
    
    public static void createWordFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/home/lahiru/Desktop/1.xml"));
        String line;
        FileWriter writer = new FileWriter("/home/lahiru/Desktop/words.txt");
        while((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, "[\u002E\u003F\u0021\u0020]");
            while(st.hasMoreElements()) {
                //System.out.println(st.nextElement());
                writer.write((String)st.nextElement());
                writer.write("\n");
            }
        }
        writer.close();
        br.close();
    }
    
    void addWord(String word) {
        OMElement doc = factory.createOMElement(new QName("doc"));
        OMElement idField = factory.createOMElement(new QName("field"));
        idField.addAttribute("name", "id", null);
        String id = this.id + "";
        idField.setText(id);

        OMElement contentField = factory.createOMElement(new QName("field"));
        contentField.addAttribute("name", "content", null);
        contentField.setText(new QName(word));
        
        doc.addChild(idField);
        doc.addChild(contentField);
        add.addChild(doc);
        
        ++this.id;
    }
    
    void writeToFile() throws FileNotFoundException, XMLStreamException {
        root.addChild(add);
        OutputStream out = new FileOutputStream("/home/lahiru/Desktop/temp.xml");
        XMLStreamWriter writer = XMLOutputFactory2.newInstance().createXMLStreamWriter(out);
        root.serialize(writer);
        writer.flush();
    }
    
    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        WildCardWordListCreator x = new WildCardWordListCreator();
        x.addWord("hello");
        x.addWord("hi");
        x.addWord(":D");
        x.writeToFile();
    }
}
