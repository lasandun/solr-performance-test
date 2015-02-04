/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package corpus.sinhala.solr.performance.test;

//import solrwildcardsearch.UploadTimeTest;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lahiru
 */
public class SolrPerformanceTest {
    
    public static void main(String[] args) {
        if(args == null) {
            System.exit(0);
        }
        
        if(args.length > 0) {
            if(args[0].equals("parse_xml") && args[1] == null) {
                XMLParser parser = new XMLParser();
                parser.parseXMLFiles();
                System.exit(0);
            }
            else if(args[0].equals("parse_xml") && args[1] != null && args[2] != null) {
                XMLParser parser = new XMLParser(args[1], args[2]);
                parser.parseXMLFiles();
                System.exit(0);
            }
            else if(args[0].equals("upload_time")) {
                try {
                    UploadTimeTest test = null;
                    if(args.length == 1)      test = new UploadTimeTest();
                    else if(args.length == 2) test = new UploadTimeTest(args[1]);
                    
                    test.testUploadTime();
                } catch (IOException ex) {
                        Logger.getLogger(UploadTimeTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            }
            else if(args[0].equals("word_count")){
                WordCount x = new WordCount(args[1], args[2]);
                x.getWordCount();
            }
            else if(args[0].equals("word_count_and_parse")) {
                WordCount x = new WordCount(args[1], args[2]);
                x.parseXMLFiles();
            }
            else if(args[0].equals("parsed_word_count")) {
                WordCount x = new WordCount(args[1], "-");
                x.countFromAllParsedFiles(args[1]);
            }
            else if(args[0].equals("query_time")) {
                SolrQueryTimeTest x = new SolrQueryTimeTest();
                x.searchAllWords(args[1]);
            }
            else if(args[0].equals("query_time2")) {
                SolrQueryTimeTest x = new SolrQueryTimeTest();
                x.searchAllWordsInGivenPeriods(args[1], false);
            } else if(args[0].equals("query_time3")) {
                SolrQueryTimeTest x = new SolrQueryTimeTest();
                x.searchAllWordsInGivenPeriods(args[1], true);
            }
            else if(args[0].equals("delete_data")) {
                try {
                    URL query = new URL("http://192.248.15.239:8983/solr/update?stream.body=%3Cdelete%3E%3Cquery%3E*:*%3C/query%3E%3C/delete%3E&commit=true");
                    URLConnection connection = query.openConnection();
                } catch(Exception ex) {
                    Logger.getLogger(SolrPerformanceTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                System.out.println("Unknown operation : " + args[0]);
                System.exit(1);
            }
        }
        else {
            System.out.println("No operation specified");
            System.exit(1);
        }
    }
    
//    public static void main(String[] args) {
//        try {
//            UploadTimeTest test = new UploadTimeTest();
//            test.testUploadTime();
//        } catch (IOException ex) {
//            Logger.getLogger(UploadTimeTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
