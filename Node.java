// Node.java
import java.util.*;

public class Node {
      public int ID;
      public boolean isFast;
      public int meanBlkTime;
	public int nextBlkTime;

      public HashSet<Node> peerList;
      public HashSet<Integer> txnBCasted;
      public HashMap<Node, Integer> balance;

      public Blockchain chain;

  	// Block structure public
  	public BlockchainUnit lastBlock;

      Node(int id, boolean fast) {
            this.ID = id;
            this.isFast = fast;

            peerList = new HashSet<Node>();
            txnBCasted = new HashSet<Integer>();
            balance = new HashMap<Node,Integer>();

            chain = new Blockchain();
            lastBlock = chain.getUnit(0);
      }

      public void broadcastTxn(Txn txn, Node receivedFrom, int broadcastTime) {
            if (txnBCasted.contains(txn.ID)) return;
            txnBCasted.add(txn.ID);

            // Print TXN received.
            System.out.println("Node ID: "+String.valueOf(this.ID)+"; Txn ID: "+String.valueOf(txn.ID));

            int latency = Simulator.minLatency;
            if (receivedFrom == null) latency = 0;
            else if ( receivedFrom.isFast && this.isFast) latency += 1;	// Queueing delay = 0.96 ms
            else { latency += 19; }							// Queueing delay = 19.2 ms

            for (Node node : peerList) {
                  if ( node == receivedFrom ) continue;
                  else node.broadcastTxn(txn, this, broadcastTime + latency);
            }
      }

      public void broadcastBlock(Block blk, Node receivedFrom, int broadcastTime) {
        	// If block already in tree, return
            if (chain.contains(blk.ID)) return;

            // Print BLK received.
            System.out.println("Node ID: "+String.valueOf(this.ID)+"; Block ID: "+String.valueOf(blk.ID)+"; Parent ID: "+String.valueOf(blk.prevBlock.ID));

        	// Compute latency
        	int latency = Simulator.minLatency;
        	if (receivedFrom == null) latency = 0;
        	else if (receivedFrom.isFast && this.isFast) latency += 1 + 80; // Queueing delay (0.96ms) + Transmission Delay (80 ms)
            else latency += 19 + 1600; 						    // Queueing delay (19.2ms) + Transmission Delay (1.6 s)

        	// Insert block in tree with time = broadcastTime + latency
            BlockchainUnit unit = new BlockchainUnit(blk, broadcastTime + latency);
            chain.addBlock(unit);

        	// If new node, Last node of tree:
          	//	Update lastBlock;
        	//	Generate nextBlkTime;
            if ( unit.getDepth() > lastBlock.getDepth() || (unit.getDepth() == lastBlock.getDepth() && unit.getTime() < lastBlock.getTime()) ) {
            	lastBlock = unit;
        		nextBlkTime = lastBlock.getTime() + ((int)Simulator.genExpRandom(meanBlkTime));
            }

        	// Broadcast to peers.
            for (Node node : peerList) {
            	if ( node == receivedFrom ) continue;
              	else node.broadcastBlock(blk, this, broadcastTime + latency);
            }
      }

  	public void generateBlock(int id, Block prev, int time) {
        	Block blk = new Block(id);
            blk.prevBlock = prev;
        	broadcastBlock(blk, null, time);
  	}
}
