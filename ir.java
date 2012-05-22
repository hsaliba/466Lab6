import java.util.*;
import java.lang.*;
import java.io.*;

public class ir {


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
