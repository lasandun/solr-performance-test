package corpus.sinhala.solr.performance.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;

/**
 *
 * @author lahiru
 */
public class SolrQueryTimeTest {
    
    String serverUrl;
    String collection;
    final int maxRows;

    public SolrQueryTimeTest() {
        serverUrl = SysProperty.getProperty("solrServerURL");
        collection = SysProperty.getProperty("solrCollection");
        maxRows = 10000;
    }
    
    int searchWordQuery(String word) {
        String query = "select?q=content:" + word + "&fl=,fl:termfreq(content," + word + ")";
        return execQuery(query);
    }
    
    LinkedList<String> getSearchingWords() {
        LinkedList<String> wordList = new LinkedList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(
                        SysProperty.getProperty("solrSearchQueryWordList"))), "UTF-8"));
            String line = null;
            while(true) {
                line = br.readLine();
                if(line == null) {
                    break;
                } else {
                    //System.out.println("************" + line + "*************");
                    wordList.addLast(URLEncoder.encode(line, "UTF-8"));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return wordList;
    }
    
    void searchAllWords(String path) {
        LinkedList<String> wordList = getSearchingWords();
        int totalTime = 0;
        for(String word : wordList) {
            //System.out.println(word);
            int time = searchWordQuery(word);
            totalTime += time;
            System.out.println(word + ": " +time);
            System.out.println("total time = " + totalTime);
            
            File summaryFile = new File(Util.addBackSlash(path) + "solrSearchWordTimeTestResultSummary.txt");
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter(summaryFile);
                printWriter.println("total time = " + totalTime + " ms");
                //printWriter.println("total time including everything = " + totalTime + " ms");
                printWriter.flush();
                printWriter.close ();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    int wildcardSearch(String word) {
        String query = "select?q=content_permuterm:" + encordeWildcardSyntaxToTURL(word) + "&fl=id&rows=100000";
        int time = execQuery2(query);
        //System.out.println(time + "   " + query);
        return time;
    }
    
    void searchAllWildcards() {
//        String words[] = {"*", "*ම*", "මහින්?", "මහින්*", "??න්ද", "*න්ද", "ම*ද"};
        String words[] = {"ස?"};
        System.out.println("-----------------------------");
        for(String word : words) {
            float time = (float) wildcardSearch(word) / 1000;
            System.out.println(time);
        }
        System.out.println("-----------------------------");
    }
    
    int searchWordInGivenPeriodQuery(String word, String date1, String date2, boolean retrieveDocs) {
        //2012-01-01T23:59:59.999Z
        //
        String query;
        if(!retrieveDocs)
            query = "select?q=date:[" + date1 + "%20TO%20" + date2 + "]&content:" + word + "&fl=,fl:termfreq(content," + word + ")";
        else
            query = "select?q=date:[" + date1 + "%20TO%20" + date2 + "]&content:" + word + 
                    "&fl=content,fl:termfreq(content," + word + ")&rows=" + maxRows;
        return execQuery(query);
    }
    
    LinkedList<String> getSearchingWordsAndPeriods(LinkedList<String> wordList, LinkedList<String> date1List, LinkedList<String> date2List) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(
                        SysProperty.getProperty("solrSearchQueryWordList2"))), "UTF-8"));
            String line = null;
            while(true) {
                line = br.readLine();
                if(line == null) {
                    break;
                } else {
                    //System.out.println("************" + line + "*************");
                    wordList.addLast(URLEncoder.encode(line, "UTF-8"));
                    date1List.addLast(br.readLine());
                    date2List.addLast(br.readLine());
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return wordList;
    }
    
    void searchAllWordsInGivenPeriods(String path, boolean retrieveDocs) {
        LinkedList<String> wordList, date1List, date2List;
        wordList = new LinkedList<String>();
        date1List = new LinkedList<String>();
        date2List = new LinkedList<String>();
        getSearchingWordsAndPeriods(wordList, date1List, date2List);
        int totalTime = 0;
        String word, date1, date2;
        for(int i=0; i < wordList.size(); ++i) {
            word = wordList.get(i);
            date1 = date1List.get(i);
            date2 = date2List.get(i);
            //System.out.println(word);
            int time = searchWordInGivenPeriodQuery(word, date1, date2, retrieveDocs);
            totalTime += time;
            System.out.println(word + ": " +time);
            System.out.println("total time = " + totalTime);
            
            File summaryFile;
            if(!retrieveDocs)   summaryFile = new File(Util.addBackSlash(path) + "solrSearchWordTimeTestResultSummary2.txt");
            else                summaryFile = new File(Util.addBackSlash(path) + "solrSearchWordTimeTestResultSummary3.txt");
            PrintWriter printWriter;
            try {
                printWriter = new PrintWriter(summaryFile);
                printWriter.println("total time = " + totalTime + " ms");
                //printWriter.println("total time including everything = " + totalTime + " ms");
                printWriter.flush();
                printWriter.close ();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    int execQuery(String q) {
        long time = -1;
        try {
            // create connection and query to Solr Server
            URL query = new URL(serverUrl + "solr/" + collection + "/" + q);
            //System.out.println(serverUrl + "solr/" + collection + "/" + q);
            time = System.nanoTime();
            URLConnection connection = query.openConnection();
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            String content = "";
            // read the result to a string
            while ((line = inputStream.readLine()) != null) {
                content += line;
                System.out.println(line);
            }
            //System.out.println(content);
            inputStream.close();
            time = System.nanoTime() - time;
            // read the query time from the xml file
//            OMElement documentElement = AXIOMUtil.stringToOM(content);
//            OMElement innerDoc = documentElement.getFirstChildWithName(new QName("lst"));
//            Iterator childElem = innerDoc.getChildElements();
//            OMElement status  = (OMElement)childElem.next();
//            OMElement element = (OMElement)childElem.next();

            // result query status shows an error
//            time = Integer.parseInt(element.getText());
//            if(Integer.parseInt(status.getText()) != 0) {
//                return -100;
//            }
        } catch(MalformedURLException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
//        catch(XMLStreamException ex) {
//            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return (int)(time / 1000000);
    }
    
    int execQuery2(String q) {
        long time = -1;
        try {
            // create connection and query to Solr Server
            URL query = new URL(serverUrl + "solr/" + collection + "/" + q);
            //System.out.println(serverUrl + "solr/" + collection + "/" + q);
            time = System.nanoTime();
            URLConnection connection = query.openConnection();
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            String content = "";
            // read the result to a string
            while ((line = inputStream.readLine()) != null) {
                content += line;
            }
            inputStream.close();
            time = System.nanoTime() - time;
            // read the query time from the xml file
            OMElement documentElement = AXIOMUtil.stringToOM(content);
            OMElement resultDoc = documentElement.getFirstChildWithName(new QName("result"));
            Iterator childElem = resultDoc.getChildElements();
            int count = 0;
            while(childElem.hasNext()) {
                childElem.next();
                ++count;
            }
            System.out.println("count = " + count);
            
            //OMElement status  = (OMElement)childElem.next();
            //OMElement element = (OMElement)childElem.next();

        } catch (XMLStreamException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch(MalformedURLException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
//        catch(XMLStreamException ex) {
//            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return (int)(time / 1000000);
    }
    
    static String unicodeToASCII(String s) {
        try {
            String s1 = Normalizer.normalize(s, Normalizer.Form.NFKD);
            String regex = Pattern.quote("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
            String s2 = new String(s1.replaceAll(regex, "").getBytes("ascii"), "ascii");
            return s2;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    void testDirectory(String path) throws Exception {
//        solrperformancetest.Util.clearSolrDataAndIndexes();
//        // posting xml files to server at given directory
//        String postJar = SysProperty.getProperty("solrPostJarPath");
//        String command = "java -jar " + postJar + " " + path + "*.xml";
//        InputStream is = null;
//        try {
//            is = solrperformancetest.Util.runCommand(command);
//        } catch (IOException ex) {
//            System.out.println("Error while posting xml files at " + path);
//            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        // wait until posting is finished
//        BufferedReader solrStreamReader = new BufferedReader(new InputStreamReader(is));
//        try {
//            String line = "";
//            while((line = solrStreamReader.readLine()) != null) {
//                System.out.println(line);
//            }
//            is.close();
//        } catch (IOException ex) {
//            Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("finished.............................");
        
        
    }
    
    String encordeWildcardSyntaxToTURL(String word) {
        String parts[] = word.split("\\?");
        if(parts.length == 1) {
            if(parts[0].length() == word.length()) {
                try {
                    return URLEncoder.encode(parts[0], "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                try {
                    return (URLEncoder.encode(parts[0], "UTF-8") + "?");
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        }
        
        String converted = "";
        for(int i = 0; i < parts.length; ++i) {
            try {
                converted += URLEncoder.encode(parts[i], "UTF-8") + "?";
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(SolrQueryTimeTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        converted = converted.substring(0, converted.length() - 1);
        return converted;
    }
    
    
    public static void main(String[] args) throws Exception {
        
        SolrQueryTimeTest x = new SolrQueryTimeTest();
        x.searchAllWildcards();
        
        
        //System.out.println(x.encordeWildcardSyntaxToTURL("මහි?න්ද*මහින්ද"));
        
        //System.out.println(URLEncoder.encode("?", "UTF-8"));
        //System.out.println(URLEncoder.encode("මහින්ද*මහින්ද", "UTF-8"));
        
    }
}
