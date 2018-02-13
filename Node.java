// Node.java
import java.util.*;

public class Node {
	public int ID;
	public int numPeers;
	public boolean isFast;
	public int meanBlkTime;
	public int nextBlkTime;

  	private class TxnUnit {
    		Txn txn;
        	int receivedTime;

        	TxnUnit (Txn txn,int t) {
          		this.txn = txn;
              	this.receivedTime = t;
            }
      };

	public HashSet<Node> peerList;
	public HashMap<Integer, TxnUnit> txnUnspent;
	public HashMap<Integer, TxnUnit> txnSpent;
	public HashMap<Integer, Integer> balance;

	public Blockchain chain;

	// Block structure public
	public BlockchainUnit lastBlock;

	Node(int id, boolean fast, int numPeers) {
		this.ID = id;
		this.isFast = fast;
		this.numPeers = numPeers;

		peerList = new HashSet<Node>();
		txnUnspent = new HashMap<Integer, TxnUnit>();
        	txnSpent = new HashMap<Integer, TxnUnit>();
		balance = new HashMap<Integer, Integer>();
		for (int i = 0; i < numPeers; i++) {
			balance.put(i, 0);
		}

		chain = new Blockchain();
		lastBlock = chain.getUnit(0);
	}

      public void setMeanTime(int t) {
            meanBlkTime = t;
            nextBlkTime = (int) Simulator.genExpRandom(t);
      }
	public void broadcastTxn(Txn txn, Node receivedFrom, int broadcastTime) {
		int latency = Simulator.minLatency;
		if (receivedFrom == null) latency = 0;
		else if ( receivedFrom.isFast && this.isFast) latency += ((int) Simulator.genExpRandom(0.96));	// Queueing delay = 0.96 ms
		else { latency += ((int) Simulator.genExpRandom(19.2)); }							// Queueing delay = 19.2 ms

        	int receivedTime = broadcastTime + latency;
        	if (txnSpent.containsKey(txn.ID)) return;
        	if (txnUnspent.containsKey(txn.ID)) {
          		// Update time.
			return;
		}

		txnUnspent.put(txn.ID, new TxnUnit(txn, receivedTime));

		// Print TXN received.
		//System.out.println("Node ID: "+String.valueOf(this.ID)+"; Txn ID: "+String.valueOf(txn.ID));

		for (Node node : peerList) {
			if ( node == receivedFrom ) continue;
			else node.broadcastTxn(txn, this, broadcastTime + latency);
		}
	}

	public void broadcastBlock(Block blk, Node receivedFrom, int broadcastTime) {
		// Compute latency
            int latency = Simulator.minLatency;
            if (receivedFrom == null) latency = 0;
            else if (receivedFrom.isFast && this.isFast) latency += ((int) Simulator.genExpRandom(0.96)) + 80; // Queueing delay (0.96ms) + Transmission Delay (80 ms)
            else latency += ((int) Simulator.genExpRandom(19.2)) + 1600; 						    // Queueing delay (19.2ms) + Transmission Delay (1.6 s)

            int receivedTime = broadcastTime + latency;

            /* Print BLK received.
		System.out.print("Node ID: "+String.valueOf(this.ID)+"; Block ID: "+String.valueOf(blk.ID)+"; Parent ID: "+String.valueOf(blk.prevBlock.ID)+"; time "+receivedTime);
		if(receivedFrom == null)
			System.out.println(" generated");
		else
			System.out.println(" received");
            */

            // If block already in tree, return
		if (chain.contains(blk.ID)) {
			if (chain.getUnit(blk.ID).getTime() > receivedTime) {
				chain.getUnit(blk.ID).updateTime(receivedTime);
			}
			return;
		}

		// Insert block in tree with time = broadcastTime + latency
		BlockchainUnit unit = new BlockchainUnit(blk, broadcastTime + latency);
		chain.addBlock(unit);

		// If new node, Last node of tree:
		//	Update lastBlock;
		//	Generate nextBlkTime;
		if ( unit.getDepth() > lastBlock.getDepth() || (unit.getDepth() == lastBlock.getDepth() && unit.getTime() < lastBlock.getTime()) ) {
			//lastBlock = unit;
			if(unit.getParentID() == lastBlock.getBlock().ID) {
				shiftToNextBlock(unit);
			} else {
				txnSpent.clear();
				shiftToNewChain(unit);
			}
			nextBlkTime = lastBlock.getTime() + ((int)Simulator.genExpRandom(meanBlkTime));
		}

		// Broadcast to peers.
		for (Node node : peerList) {
			if ( node == receivedFrom ) continue;
			else node.broadcastBlock(blk, this, broadcastTime + latency);
		}
	}

	private void shiftToNextBlock(BlockchainUnit nextBlock) {
		execBlockTxns(nextBlock.getBlock());
		lastBlock = nextBlock;
	}

	private void shiftToNewChain(BlockchainUnit newBlock) {
		if(newBlock.getBlock().ID == 0) {
			balance = new HashMap<>();
			for (int i = 0; i < numPeers; i++) {
				balance.put(i, 0);
			}
			return;
		} else {
			shiftToNewChain(newBlock.getParent());
			execBlockTxns(newBlock.getBlock());
		}
	}

	private void execBlockTxns(Block block) {
		ArrayList<Txn> txnList = block.txnList;
		for (Txn t : txnList) {
			execTxn(t.ID);
		}
	}

	private void execTxn(int id) {
		if(!txnSpent.containsKey(id)){
                  TxnUnit txnUnit = txnUnspent.get(id);
                  Txn txn = txnUnit.txn;
			balance.put(txn.payer.ID, balance.get(txn.payer.ID) - txn.amount);
			balance.put(txn.receiver.ID, balance.get(txn.receiver.ID) + txn.amount);
			txnSpent.put(txn.ID, txnUnit);
		}
	}

	public void generateBlock(int id, Block prev, int time) {
		Block blk = new Block(id);
		blk.prevBlock = prev;
		broadcastBlock(blk, null, time);
	}
}
