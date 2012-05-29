import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

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
   private static String stopWordFile = "stopwords.txt";
   private static Set<String> stopWords = new HashSet<String> ();
   
   public static List<Double> getTermFreq(Map<String, Integer> doc) {
      Set<String> keys = doc.keySet();
      List<Double> freqs = new ArrayList<Double>();
      int max = 0;

      for (String s : doc.keySet().toArray(new String[0])) {
         if (doc.get(s).intValue() > max) 
            max = doc.get(s).intValue();
      }

      for (String s : words) {
         if (keys.contains(s)) {
            //need to calculate how many docs word occurs in 
            int num = 0;
            for (int i = 0; i < docs.size(); i++) 
               if (docs.get(i).containsKey(s))
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
/*
   public static void createBogusInfo() {
      HashMap<String, Integer> doc = new HashMap<String, Integer>();
      words.add("a");
      words.add("i");
      words.add("in");
      words.add("to");
      words.add("cat");
      words.add("dog");  

      doc.put("a", 1);
      doc.put("dog", 10);
      doc.put("to", 3);
      docs.add(doc);
      doc =  new HashMap<String, Integer>();
      doc.put("i", 5);
      doc.put("to", 3);
      doc.put("cat", 8);
      docs.add(doc);
   } 
*/ 
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


   public static void main(String[] args) {
      try {
         readStopWords();
      } catch (FileNotFoundException e1) {
         e1.printStackTrace();
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
         if (line[0].compareTo("READ") == 0) {
            if (line.length == 2) {
               try {
                  if (line[1].endsWith(".xml")) {
                     readXML(line[1]);
                  } else {
                     readText(line[1]);
                  }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            } else if(line.length == 3 && line[1].compareTo("LIST") == 0) {
               try {
                  readList(line[2]);
               } catch (FileNotFoundException e) {
                  e.printStackTrace();
               }
            }
         }
         else if (line[0].compareTo("LIST") == 0) {

            System.out.println("   list");
         }
         else if (line[0].compareTo("CLEAR") == 0) {
            System.out.println("   clear");
         }
         else if (line[0].compareTo("PRINT") == 0) {
            System.out.println("   print");
         }
         else if (line[0].compareTo("SHOW") == 0) {
            //createBogusInfo();
            List<Double> vect = getTermFreq(docs.get(line[1]));
            printVect(vect);
         }
         else if (line[0].compareTo("SIM") == 0) {
            List<Double> v1 = getTermFreq(docs.get(line[2]));
            List<Double> v2 = getTermFreq(docs.get(line[3]));
            if (line[1].compareTo("COS") == 0) 
               System.out.println("Cosine similarity between "
                +line[2]+" and "+line[3]+" is "+cosineSim(v1, v2));

         }
         else if (line[0].compareTo("SEARCH") == 0)  {
            System.out.println("   search");
         }
         else if (line[0].compareTo("QUIT") == 0) {
            break;
         }
         else {
            System.out.println("   invalid command");
         }
         System.out.print("IR> ");
      }
   }

   private static void readXML(String file) throws ParserConfigurationException, SAXException, IOException{
      File fXmlFile = new File(file);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(fXmlFile);
      doc.getDocumentElement().normalize();
 
      NodeList nList = doc.getElementsByTagName("document");
      int j = 1;
      for (int i = 0; i < nList.getLength(); i++) {
 
         Node nNode = nList.item(i);
         if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
            Element eElement = (Element) nNode;
            String name = file + "-" + j++;
            if(documents.containsKey(name)) {
               System.out.println("Error! Document with same ID already exists");
               return;
            }
            HashMap<String, Integer> toAdd = new HashMap<String, Integer> ();
            Scanner scan = new Scanner(eElement.getTextContent());
            String temp = null;
            String[] split = null;
            StringBuilder docText = new StringBuilder();
            while(scan.hasNextLine()) {
               temp = scan.nextLine();
               docText.append(temp);
               docText.append("\n");
               split = temp.split(" [.!?,(){}\":;<>/\\-]");
               for(String word : split) {
                  if(!stopWords.contains(word)) {
                     if(!words.contains(word)) {
                        words.add(word);
                     }
                     if(!toAdd.containsKey(word)) {
                        toAdd.put(word, 0);
                     } else {
                        toAdd.put(word, toAdd.get(word) + 1);
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
         System.out.println("Error! Document with same ID already exists");
         return;
      }
      Scanner scan = new Scanner(new File(file));
      String temp = null;
      String[] split = null;
      StringBuilder docText = new StringBuilder();
      HashMap<String, Integer> toAdd = new HashMap<String, Integer> ();
      while(scan.hasNextLine()) {
         temp = scan.nextLine();
         docText.append(temp);
         docText.append("\n");
         split = temp.split(" [.!?,(){}\":;<>/\\-]");
         for(String word : split) {
            if(!stopWords.contains(word)) {
               if(!words.contains(word)) {
                  words.add(word);
               }
               if(!toAdd.containsKey(word)) {
                  toAdd.put(word, 0);
               } else {
                  toAdd.put(word, toAdd.get(word) + 1);
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
