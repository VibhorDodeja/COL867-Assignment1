// Simulator.java
import java.io.*;
import java.lang.Math.*;
import java.util.*;

public class Simulator {
	private static Network network;
	public static int TIME;                   // 1 unit = 1 ms;

	public static int numPeers;
	public static float simTime;

	private static int txnCount;
	private static int blkCount;

	public static int minLatency;
	public static float fracFast;

	public static int meanTxnTime;
	public static int meanBlkTimeBig;
	public static int meanBlkTimeSmall;

	public static double genExpRandom(double mean) {
		Random rnd = new Random();
		double uni = rnd.nextDouble();
		return ((double) Math.log(1-uni)) * (-mean);
	}

	public static int genIntRandom(int min, int max) {
		Random rnd = new Random();
		return rnd.nextInt(max-min+1) + min;
	}

	public static double genRandom() {
		Random rnd = new Random();
		return rnd.nextDouble();
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
			meanBlkTimeBig = 9*60*1000;
			meanBlkTimeSmall = 15*60*1000;
		}
		else {
			System.out.print("Please enter the number of peers in the network: ");
			numPeers = Integer.parseInt(in.nextLine());

			System.out.print("Please enter the fraction of fast nodes: ");
			fracFast = Float.parseFloat(in.nextLine());

			System.out.print("Please enter the mean interarrival time for Txns (min): ");
			meanTxnTime = Integer.parseInt(in.nextLine());

			System.out.print("Please enter mean interarrival time for blocks by big miners (min): ");
			meanBlkTimeBig = Integer.parseInt(in.nextLine());

			System.out.print("Please enter mean interarrival time for blocks by small miners (min): ");
			meanBlkTimeSmall = Integer.parseInt(in.nextLine());

			System.out.print("Please enter the simulation time (in hrs): ");
			simTime = Float.parseFloat(in.nextLine());
		}

		System.out.println("Initialising...");

		// Initialising variables/constants.
		txnCount = 1;
		blkCount = 1;
		minLatency = genIntRandom(10,500);
		int numFast = (int) (fracFast*numPeers);

		// Initialising network.
		network = new Network();
		TIME = 0;

		// Creating Nodes.
		for (int i = 0; i < numPeers; i++) {
			Node tmp = new Node(i, false, numPeers);
			if ( i < numFast ) {
				tmp.isFast = true;
				if ( genRandom() < 0.9 ) tmp.setMeanBlkTime(genIntRandom((int) (0.9*meanBlkTimeBig), (int) (1.1*meanBlkTimeBig)));
				else tmp.setMeanBlkTime(genIntRandom((int) (0.9*meanBlkTimeSmall), (int) (1.1*meanBlkTimeSmall)));
			} else {
				if ( genRandom() < 0.1 ) tmp.setMeanBlkTime(genIntRandom((int) (0.9*meanBlkTimeBig), (int) (1.1*meanBlkTimeBig)));
				else tmp.setMeanBlkTime(genIntRandom((int) (0.9*meanBlkTimeSmall), (int) (1.1*meanBlkTimeSmall)));
			}

			tmp.setMeanTxnTime(genIntRandom((int) (0.9*meanTxnTime),(int) (1.1*meanTxnTime)));
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

		/* DEBUG: Check peers... *
		for (int i = 0; i< numPeers; i++) {
			Node tmp = network.getNode(i);
			System.out.print("Node "+String.valueOf(tmp.ID)+"; Peers: ");

			for ( Node tmp2 : tmp.peerList) {
				System.out.print(String.valueOf(tmp2.ID)+", ");
			}
			System.out.print("\n");
		}
		*/

		System.out.println("Running simulation...");
		while (TIME < simTime*3600*1000) {
			// Populate Block times:
			TreeMap<Integer, Integer> blockQ = new TreeMap<Integer, Integer>();	// nextBlkTime x Node ID
			for (int i = 0; i < numPeers; i++) {
				Node tmp = network.getNode(i);
				blockQ.put(tmp.nextBlkTime, i);
			}

			// Pop first block
			TIME = blockQ.firstKey();
			int genNode = blockQ.get(TIME);

			boolean isTxn;
			do {
				isTxn = false;
				for (int i = 0; i < numPeers; i++) {
					Node tmp = network.getNode(i);
					if ( tmp.nextTxnTime < TIME) {
						tmp.generateTxn(txnCount, network.getNode(genIntRandom(0,numPeers-1)));
						isTxn = true;
						txnCount++;
					}
				}
			} while (isTxn);

			network.getNode(genNode).generateBlock(blkCount);
			blkCount++;
		}

		// Printing output files.
		for (int i = 0; i < numPeers; i++ ) {
			HashMap<Integer,BlockchainUnit> chain = network.getNode(i).chain.blockMap;

			// Opening file handles
			try {
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
				for (Map.Entry<Integer,BlockchainUnit> entry : chain.entrySet()) {
					int id = entry.getKey();
					BlockchainUnit blkUnit = entry.getValue();
					if ( id == 0 ) continue;
					timeOut.println(String.valueOf(id)+" received at t="+String.valueOf(blkUnit.getTime()));
				}

				// Closing file handles.
				treeOut.close();
				timeOut.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		HashMap<Integer, BlockchainUnit> chain = network.getNode(0).chain.blockMap;

		for (Map.Entry<Integer,BlockchainUnit> entry: chain.entrySet()){
			int id = entry.getKey();
			Block blk = entry.getValue().getBlock();
			try {
				PrintWriter blkOut = new PrintWriter("Outputs/Block_"+String.valueOf(id)+".txt","UTF-8");

				if (blk.prevBlock == null) {
					blkOut.println("<Genesis Block>");
					blkOut.println("Parent Block ID: null");
					blkOut.println("Created by Node ID: null");
				} else {
					blkOut.println("Parent Block ID: "+String.valueOf(blk.prevBlock.ID));
					blkOut.println("Created by Node ID: "+String.valueOf(blk.createdBy.ID));
				}
				blkOut.println("Created at t="+String.valueOf(blk.creationTime));

				for (Txn txn: blk.txnList) {
					blkOut.print("Txn ID: "+String.valueOf(txn.ID));
					if (txn.payer == null) {
						blkOut.print("; God");
					} else {
						blkOut.print("; Node ID "+String.valueOf(txn.payer.ID));
					}
					blkOut.print(" pays Node ID "+String.valueOf(txn.receiver.ID));
					blkOut.print(" "+String.valueOf(txn.amount)+" coins");
				}

				blkOut.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
