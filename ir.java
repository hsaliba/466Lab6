import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ir {

   private static List<String> words = new ArrayList<String> (); //holds all words
   private static Map<String, String> documents = new HashMap<String, String> (); //doc ID hashed to actual document content
   private static Map<String, Map<String, Integer>> docs = new HashMap<String, Map<String, Integer>>(); //sample docs 
   private final static String stopWordFile = "stopwords.txt";
   private static Set<String> stopWords = new HashSet<String> ();
   private static final String saveFileName = "saveFile.sav";
   
   public static List<Double> getTermFreq(Map<String, Integer> doc) {
      Set<String> keys = doc.keySet();
      List<Double> freqs = new ArrayList<Double>();
      int max = 0;

      for (String s : doc.keySet()) 
         if (doc.get(s).intValue() > max) 
            max = doc.get(s).intValue();
      

      for (String s : words) {
         if (keys.contains(s)) {
            //need to calculate how many docs word occurs in 
            int num = 0;
            for (String k : docs.keySet())
               if (docs.get(k).containsKey(s))
                  num++;

            freqs.add((doc.get(s).doubleValue()/max) * log2((double)docs.size()/num));
         }
         else 
            freqs.add(0.0); 
      }
      return freqs;
   }

   private static double log2(double x) {
      return Math.log(x)/Math.log(2);
   }
 
   public static void printVect(List<Double> vect) {
      for (int i = 0; i < vect.size(); i++) 
         if (Double.compare(vect.get(i), 0) != 0)
            System.out.println(words.get(i) + " " + vect.get(i));
   }

   public static double cosineSim(List<Double> v1, List<Double> v2) {
      int size = (v1.size() > v2.size()) ? v1.size() : v2.size();      
      double top = 0.0, bot1 = 0.0, bot2 = 0.0;
      for (int i = 0; i < size; i++) {
         double val1 = 0.0, val2 = 0.0;
         if (i < v1.size())
            val1 = v1.get(i);
         if (i < v2.size())
            val2 = v2.get(i);

         top += val1*val2;
         bot1 += val1*val1;
         bot2 += val2*val2; 
      }
      return top/Math.sqrt(bot1*bot2);
   } 

   public static double okapi(String d1, String d2) {
      double k1 = 1.5, k2 = 1, b = .75; 
      double sum1 = 0.0, sum2 = 0.0, sum3 = 0.0, ans = 0.0; 
      List<Double> v1 = getTermFreq(docs.get(d1));
      List<Double> v2 = getTermFreq(docs.get(d2));
      int size = (v1.size() > v2.size()) ? v2.size() : v2.size();
      double avgDocLen = 0.0;

      for (String s : documents.keySet()) 
         avgDocLen += documents.get(s).length();
      avgDocLen = avgDocLen/documents.size();

      
      for (int i = 0; i < words.size(); i++) {
         int num = 0;
         for (String s : docs.keySet()) 
            if (docs.get(s).containsKey(words.get(i))) 
               num++;

         sum1 = Math.log((docs.size()-num+.5)/(num+.5));
         sum2 = ((k1+1)*v1.get(i)) / (k1*((1-b)+(b*documents.get(d1).length()/avgDocLen))+v1.get(i));
         sum3 = ((k2+1)*v2.get(i)) / (k2+v2.get(i));
         ans += sum1 * sum2 * sum3;
      }
   
      return ans;
   }

   public static List<Double> parseSearch(String[] search) {
      String delims = "[ .!?,(){}\":;<>/\\-]";
      Map<String, Integer> counts = new HashMap<String, Integer>();
      Stemmer stem = new Stemmer();

      for (int i = 1; i < search.length; i++) {
         String[] line = search[i].split(delims);
          
         for (String s : line) { 
            stem.add(s.toLowerCase().toCharArray(), s.length());
            stem.stem();
            String stemmedWord = stem.toString();
            if (!words.contains(stemmedWord) && stemmedWord.length() > 0)
            words.add(stemmedWord);
            if (counts.containsKey(stemmedWord)) 
               counts.put(stemmedWord, counts.get(stemmedWord)+1);
            else
               counts.put(stemmedWord, 1);
         }
      }
      
      return getTermFreq(counts); 
   }

   public static void genSearch(List<Double> query) {
      LinkedList<String> docNames = new LinkedList<String>();
      LinkedList<Double> docSim = new LinkedList<Double>();

      for (String k : docs.keySet()) {
         double sim = cosineSim(getTermFreq(docs.get(k)), query); 
         if (Double.compare(sim, 0.0) != 0 && Double.compare(sim, 1.0) != 0) {
            if (docSim.size() == 0) {
               docSim.add(sim);
               docNames.add(k);
            }
            else {
               for (int i = 0; i < docSim.size(); i++) {
                  if (Double.compare(docSim.get(i), sim) < 0) {
                     docSim.add(i,sim);
                     docNames.add(i, k);
                     break; 
                  }
                  else if (docSim.size() == i) {
                     docSim.addLast(sim);
                     docNames.addLast(k); 
                     break;
                  }
               }
            }
         }
      }

      System.out.println("Search Results: ");
      if (docNames.size() == 0)
         System.out.println("none found");
      else
         for (int i = 0; i < docSim.size(); i++) 
            System.out.println(i+") "+docNames.get(i)+" "+docSim.get(i)); 
   } 

   public static void main(String[] args) {
      try {
         readStopWords();
         readPersisted();
      } catch (FileNotFoundException e1) {
         e1.printStackTrace();
         System.exit(-1);
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(-1);
      }
      Scanner sc = null;
      String delims = "[ ]+";
      try {
         sc = new Scanner(System.in);
      }
      catch (Exception e) {
         System.out.println("Caught: "+e);
      }
      System.out.println("IR  System Version 1.0");
      System.out.print("IR> ");
      while (sc.hasNextLine()) {
         String[] line = sc.nextLine().split(delims);
         if (line[0].compareToIgnoreCase("READ") == 0) {
            if (line.length == 2) {
               try {
                  if (line[1].endsWith(".xml")) {
                     readXML(line[1]);
                  } else {
                     readText(line[1]);
                  }
               } catch (Exception e) {
                  System.out.println("   could not find file");
               }
            } else if(line.length == 3 && line[1].compareToIgnoreCase("LIST") == 0) {
               try {
                  readList(line[2]);
               } catch (FileNotFoundException e) {
                  e.printStackTrace();
               }
            }
         }
         else if (line[0].compareToIgnoreCase("LIST") == 0) {
            listDocIDs();
         }
         else if (line[0].compareToIgnoreCase("CLEAR") == 0) {
            clear();
         }
         else if (line[0].compareToIgnoreCase("PRINT") == 0 && line.length == 2) {
            if(documents.containsKey(line[1])) {
               System.out.println(documents.get(line[1]));
            } else {
               System.out.println("    Document not found.");
            }
         }
         else if (line[0].compareToIgnoreCase("SHOW") == 0) {
            if (line.length == 2) {
               Map<String, Integer> d = docs.get(line[1]);
               if (d == null)
                  System.out.println("   Invalid doc name");
               else {
                  List<Double> vect = getTermFreq(d);
                  printVect(vect);
               }
            }
            else
               System.out.println("   SHOW <docId>");
         }
         else if (line[0].compareToIgnoreCase("SIM") == 0) {
            if (line.length == 4) {
               Map<String, Integer> d1 = docs.get(line[2]);
               Map<String, Integer> d2 = docs.get(line[3]);
               if (d1 == null || d2 == null) 
                  System.out.println("   Invalid doc name");
               else {
                  List<Double> v1 = getTermFreq(d1);
                  List<Double> v2 = getTermFreq(d2);
                  if (line[1].compareToIgnoreCase("COS") == 0) 
                     System.out.println("   Cosine similarity between "
                      +line[2]+" and "+line[3]+" is "+cosineSim(v1, v2));
                  else if (line[1].compareToIgnoreCase("OKAPI") == 0) 
                     System.out.println("   Okapi similarity between "
                      +line[2]+" and "+line[3]+" is "+okapi(line[2], line[3]));
                  else
                     System.out.println("    Invalid similarity method");
               }
            }
            else {
               System.out.println("   SIM <COS | OKAPI> <doc1> <doc2>");
            }
         }
         else if (line[0].compareToIgnoreCase("SEARCH") == 0)  {
            if (line.length == 3 && line[1].compareToIgnoreCase("DOC") == 0) {
               genSearch(getTermFreq(docs.get(line[2])));
            }
            else { 
               genSearch(parseSearch(line));
            }
         }
         else if (line[0].compareToIgnoreCase("QUIT") == 0) {
            break;
         }
         else {
            System.out.println("   invalid command");
         }
         System.out.print("IR> ");
      }
      try {
         persistData();
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(-1);
      }
   }

   private static void persistData() throws IOException {
      FileOutputStream saveFile = new FileOutputStream(saveFileName);
      ObjectOutputStream save = new ObjectOutputStream(saveFile);
      save.writeObject(words);
      save.writeObject(documents);
      save.writeObject(docs);
      save.close();
   }

   @SuppressWarnings("unchecked")
   private static void readPersisted() throws IOException, ClassNotFoundException {
      FileInputStream saveFile;
      try {
         saveFile = new FileInputStream(saveFileName);
      } catch (FileNotFoundException e) {
         return;
      }
      ObjectInputStream restore = new ObjectInputStream(saveFile);
      words = (List<String>)restore.readObject();
      documents = (Map<String, String>)restore.readObject();
      docs = (Map<String, Map<String, Integer>>)restore.readObject();
      restore.close();
   }

   private static void clear() {
      words.clear();
      documents.clear();
      docs.clear();    
   }

   private static void debug(String string) {
      System.out.println(docs.get(string));
      
   }

   private static void listDocIDs() {
      List<String> list = new ArrayList<String>(documents.keySet());
      Collections.sort(list);
      for(String docID : list) {
         System.out.println(docID);
      }    
   }

   private static void readXML(String file) throws ParserConfigurationException, SAXException, IOException{
      File fXmlFile = new File(file);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(fXmlFile);
      doc.getDocumentElement().normalize();
      NodeList nList = doc.getElementsByTagName("joke");
      int j = 1;
      for (int i = 0; i < nList.getLength(); i++) {
         Node nNode = nList.item(i);
         if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            String name = file + "-" + j++;
            if(documents.containsKey(name)) {
               System.out.println("Error! Document with same ID already exists: " + name);
               return;
            }
            HashMap<String, Integer> toAdd = new HashMap<String, Integer> ();
            Scanner scan = new Scanner(eElement.getTextContent());
            String temp = null;
            String[] split = null;
            StringBuilder docText = new StringBuilder();
            Stemmer stem = new Stemmer();
            while(scan.hasNextLine()) {
               temp = scan.nextLine();
               docText.append(temp);
               docText.append("\n");
               split = temp.split("[ .!?,(){}\":;<>/\\-]");
               for(String word : split) {
                  stem.add(word.toLowerCase().toCharArray(), word.length());
                  stem.stem();
                  String stemmedWord = stem.toString();
                  if(!stemmedWord.isEmpty() && !stopWords.contains(stemmedWord)) {
                     if(!words.contains(stemmedWord)) {
                        words.add(stemmedWord);
                     }
                     if(!toAdd.containsKey(stemmedWord)) {
                        toAdd.put(stemmedWord, 1);
                     } else {
                        toAdd.put(stemmedWord, toAdd.get(stemmedWord) + 1);
                     }
                  }
               }
            }
            docs.put(name, toAdd);
            documents.put(name, docText.toString());
         }
      }
   }

   private static void readText(String file) throws FileNotFoundException {
      if(documents.containsKey(file)) {
         System.out.println("Error! Document with same ID already exists: " + file);
         return;
      }
      Scanner scan = new Scanner(new File(file));
      String temp = null;
      String[] split = null;
      StringBuilder docText = new StringBuilder();
      HashMap<String, Integer> toAdd = new HashMap<String, Integer> ();
      Stemmer stem = new Stemmer();
      while(scan.hasNextLine()) {
         temp = scan.nextLine();
         docText.append(temp);
         docText.append("\n");
         split = temp.split("[ .!?,(){}\":;<>/\\-]");
         for(String word : split) {
            stem.add(word.toLowerCase().toCharArray(), word.length());
            stem.stem();
            String stemmedWord = stem.toString();
            if(!stemmedWord.isEmpty() && !stopWords.contains(stemmedWord)) {
               if(!words.contains(stemmedWord)) {
                  words.add(stemmedWord);
               }
               if(!toAdd.containsKey(stemmedWord)) {
                  toAdd.put(stemmedWord, 1);
               } else {
                  toAdd.put(stemmedWord, toAdd.get(stemmedWord) + 1);
               }
            }
         }
      }
      docs.put(file, toAdd);
      documents.put(file, docText.toString());
   }
   
   private static void readStopWords() throws FileNotFoundException {
      Scanner scan = new Scanner(new File(stopWordFile));
      while(scan.hasNext()) {
         stopWords.add(scan.next());
      }
   }
   
   private static void readList(String file) throws FileNotFoundException {
      Scanner scan = new Scanner(new File(file));
      while (scan.hasNextLine()) {
         readText(scan.nextLine());
      }
   }
}
