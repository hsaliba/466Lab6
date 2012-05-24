import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

public class ir {

   private static List<String> words; //holds all words

   private static List<Map<String, Integer>> docs; //sample docs

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
            double num = 1.0;
            freqs.add((doc.get(s).doubleValue()/max) * log2(docs.size()/num));
         }
         else 
           freqs.add(0.0); 
      }
      return freqs;
   }

   private static double log2(double x) {
      return Math.log(x)/Math.log(2);
   }

   public static void createBogusInfo() {
      words = new ArrayList<String>();
      docs = new ArrayList<Map<String, Integer>>();
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

   public static void main(String[] args) {

      Scanner sc = null;
      String delims = "[ ]+";
      try {
         sc = new Scanner(System.in);
      }
      catch (Exception e) {
         System.out.println("Caught: "+e);
      }

      while (sc.hasNextLine()) {
         String[] line = sc.nextLine().split(delims);

         if (line[0].compareTo("READ") == 0 && line.length == 2) {
            System.out.println("   read");
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
            System.out.println("   show");
            createBogusInfo();
            System.out.println(getTermFreq(docs.get(0)));
            System.out.println(getTermFreq(docs.get(1)));
         }
         else if (line[0].compareTo("SIM") == 0) {
            System.out.println("   sim");
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
      }
   }
}
