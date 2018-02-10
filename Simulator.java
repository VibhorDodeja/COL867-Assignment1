import java.util.*;

public class Simulator {
      private static Network network;

      public static int NUM_PEERS;

      public static void main(String[] args){
            // Take input parameters
            Scanner in = new Scanner(System.in);

            System.out.print("Default config(d) or Manual config(m): ");
            if ( in.nextLine().equalsIgnoreCase("d") ) {
                                                                                                                  /* FIXME: DEFAULT VALUES? */
                  NUM_PEERS = 5;
            }
            else {
                  System.out.print("Please enter the number of peers in the network: ");
                  NUM_PEERS = Integer.parseInt(in.nextLine());
            }

            // Initialising network.
            network = new Network();
            for (int i = 0; i < NUM_PEERS; i++) {

            }

            /*
            while (true) {                                                                                        /* FIXME: Khatam Kab karna hai? *
                  // Run Simulation
            }
            */
      }
}
