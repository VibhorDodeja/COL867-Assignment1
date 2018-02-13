// Simulator.java
import java.io.*;
import java.lang.Math.*;
import java.util.*;

public class Simulator {
	private static Network network;
	public static int TIME;                   // 1 unit = 1 ms;

	private static int txnCount;
	private static int blkCount;
	private static int maxTxn;
	public static int minLatency;

	public static float fracFast;
	public static int meanTxnTime;
	public static int numPeers;
	public static int simTime;

	public static double genExpRandom(double mean) {
		Random rnd = new Random();
		double uni = rnd.nextDouble();
		return ((double) Math.log(1-uni)) * (-mean);
	}

	public static int genIntRandom(int min, int max) {
		Random rnd = new Random();
		return rnd.nextInt(max-min+1) + min;
	}

	public static void main(String[] args){
		// Take input parameters
		Scanner in = new Scanner(System.in);

		System.out.print("Default config(d) or Manual config(m): ");
		if ( in.nextLine().equalsIgnoreCase("d") ) {                                                         /* FIXME: DEFAULT VALUES? */
			fracFast = 0.1f;
			meanTxnTime = 60*1000;
			numPeers = 10;
			simTime = 1;
		}
		else {
			System.out.print("Please enter the number of peers in the network: ");
			numPeers = Integer.parseInt(in.nextLine());

			System.out.print("Please enter the fraction of fast nodes: ");
			fracFast = Float.parseFloat(in.nextLine());

			System.out.print("Please enter the mean interarrival time for Txns: ");
			meanTxnTime = Integer.parseInt(in.nextLine());

			System.out.print("Please enter the simulation time (in hrs): ");
			simTime = Integer.parseInt(in.nextLine());
		}

		// Initialising variables/constants.
		txnCount = 1;
		blkCount = 1;
		maxTxn = 100;
		minLatency = genIntRandom(10,500);
		int numFast = (int) (fracFast*numPeers);

		// Initialising network.
		network = new Network();
		TIME = 0;

		// Creating Nodes.
		for (int i = 0; i < numPeers; i++) {
			Node tmp = new Node(i, false, numPeers);
/*			if ( i < numFast ) {
				tmp.isFast = true;
				if ( genIntRandom(1,10) < 9 ) tmp.setMeanTime(genIntRandom(8*meanTxnTime*numPeers, 10*meanTxnTime*numPeers));
				else tmp.setMeanTime(genIntRandom(12*meanTxnTime*numPeers, 15*meanTxnTime*numPeers));
			} else {
				if (genIntRandom(1,10) > 9 ) tmp.setMeanTime(genIntRandom(8*meanTxnTime*numPeers, 10*meanTxnTime*numPeers));
				else tmp.setMeanTime(genIntRandom(12*meanTxnTime*numPeers, 15*meanTxnTime*numPeers));
			}
*/                  tmp.setMeanTime(meanTxnTime);
			network.addNode(tmp);
		}

		// Creating peers for "Mario" nodes
		for (int i = 0; i < numFast; i++) {
			Node tmp = network.getNode(i);
			for(int j = 1 ; j < 0.4*numFast; j++) {
				Node tmp2 = network.getNode((i+j)%numFast);
				tmp.peerList.add(tmp2);
				tmp2.peerList.add(tmp);
			}
		}

		// Creating peers for "Luigi" nodes
		for(int i = numFast; i < numPeers; i++) {
			Node tmp = network.getNode(i);

			int numFastPeers = genIntRandom(1,3);
			for (int j = 0; j < numFastPeers; j++) {
				Node tmp2 = network.getNode(genIntRandom(0,numFast-1));
				tmp.peerList.add(tmp2);
				tmp2.peerList.add(tmp);
			}

			int numSlowPeers = genIntRandom(0, numPeers - numFast);
			for (int j = 0; j < numSlowPeers; j++) {
				Node tmp2 = network.getNode(genIntRandom(numFast, numPeers-1));
				tmp.peerList.add(tmp2);
				tmp2.peerList.add(tmp);
			}
		}

		/* Check peers... */
            for (int i = 0; i< numPeers; i++) {
                  Node tmp = network.getNode(i);
                  System.out.print("Node "+String.valueOf(tmp.ID)+"; Peers: ");

                  for ( Node tmp2 : tmp.peerList) {
                        System.out.print(String.valueOf(tmp2.ID)+", ");
                  }
                  System.out.print("\n");
            }

		while (TIME < simTime*3600*1000) {
			// Generate a transaction.
			int delta = (int) genExpRandom(meanTxnTime);
			int a = genIntRandom(0,numPeers-1);
			int b;
			do {
				b = genIntRandom(0,numPeers-1);
			} while ( b != a );

			Txn txn = new Txn(txnCount, network.getNode(a), network.getNode(b), genIntRandom(0, maxTxn));   // Update upper limit to transmitter's balance.

			// See if any node is ready with a block within TIME and TIME + delta.
			// If yes, broadcast the block -> Say node A broadcasts at time t, it reaches B by time t', but B was ready to braodcast by time t" (<t'),
			// then broadcast B's block also.

			// Since nextBlkTime and lastBlock might get updated in the generation process, saving it before.
			ArrayList<Integer> blkTimes = new ArrayList<Integer>();
			ArrayList<Block> lastBlocks = new ArrayList<Block>();

			for (int i = 0; i < numPeers; i++ ) {
				blkTimes.add(network.getNode(i).nextBlkTime);
				lastBlocks.add(network.getNode(i).lastBlock.getBlock());
			}
			for (int i = 0; i < numPeers; i++ ) {
				//System.out.print( blkTimes.get(i) +"," );
				//System.out.println( TIME+delta );
				if ( blkTimes.get(i) < TIME+delta ) {
					network.getNode(i).generateBlock(blkCount, lastBlocks.get(i), blkTimes.get(i));
					blkCount++;
				}
			}

			network.getNode(a).broadcastTxn(txn, null, TIME);
			txnCount++;
			TIME += delta;
		}

            // Printing output files.
            for (int i = 0; i < numPeers; i++ ) {
                  HashMap<Integer,BlockchainUnit> chain = network.getNode(i).chain.blockMap;

                  // Opening file handles
                  try {
                        //File tree = new File()
                        PrintWriter treeOut = new PrintWriter("Outputs/Tree_Node_"+String.valueOf(i)+".txt","UTF-8");
                        PrintWriter timeOut = new PrintWriter("Outputs/Time_Node_"+String.valueOf(i)+".txt","UTF-8");

                        // Writing Tree to file
                        for (Map.Entry<Integer,BlockchainUnit> entry : chain.entrySet()) {
                              int id = entry.getKey();
                              Block blk = entry.getValue().getBlock();
                              if ( id == 0 ) continue;
                              treeOut.println(String.valueOf(blk.prevBlock.ID)+"->"+String.valueOf(id));
                        }

                        // Writing received times to file

                        // Closing file handles.
                        treeOut.close();
                        timeOut.close();
                  } catch(Exception e) {
                        e.printStackTrace();
                  }
            }
	}
}
