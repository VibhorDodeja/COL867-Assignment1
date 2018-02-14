// Node.java
import java.util.*;
import java.util.Map.Entry;

public class Node {
	public int ID;
	public int numPeers;
	public boolean isFast;

	public int meanBlkTime;
	public int meanTxnTime;
	public int nextTxnTime;
	public int nextBlkTime;

	public HashSet<Node> peerList;
	public HashMap<Integer, TxnUnit> txnUnspent;
	public HashMap<Integer, TxnUnit> txnSpent;
	public HashMap<Integer, Integer> balance;

	public Blockchain chain;

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

	public void setMeanBlkTime(int t) {
		meanBlkTime = t;
		nextBlkTime = (int) Simulator.genExpRandom(t);
	}

	public void setMeanTxnTime(int t) {
		meanTxnTime = t;
		nextTxnTime = (int) Simulator.genExpRandom(t);
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

		// DEBUG: Print TXN received.
		// System.out.println("Node ID: "+String.valueOf(this.ID)+"; Txn ID: "+String.valueOf(txn.ID));

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

		/* DEBUG: Print BLK received. *
		if(receivedFrom == null)
			System.out.println("Node ID: "+String.valueOf(this.ID)+"; Block ID: "+String.valueOf(blk.ID)+"; Parent ID: "+String.valueOf(blk.prevBlock.ID)+"; time "+receivedTime+" generated; numTxns: "+String.valueOf(blk.txnList.size()));
		else
			System.out.println("Node ID: "+String.valueOf(this.ID)+" received"+"; time "+receivedTime);
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
		if ( receivedTime <= nextBlkTime ) {
			if ( unit.getDepth() > lastBlock.getDepth() || (unit.getDepth() == lastBlock.getDepth() && unit.getBlock().creationTime < lastBlock.getBlock().creationTime) ) {
				//lastBlock = unit;
				if(unit.getParentID() == lastBlock.getBlock().ID) {
					shiftToNextBlock(unit);
				} else {
					Collection<TxnUnit> spentTxns = txnSpent.values();
					for(TxnUnit tu : spentTxns) {
						txnUnspent.put(tu.txn.ID, tu);
					}
					txnSpent.clear();
					shiftToNewChain(unit);
				}
				lastBlock = unit;
				Simulator.blockQ.remove(nextBlkTime);
				nextBlkTime = lastBlock.getTime() + ((int)Simulator.genExpRandom(meanBlkTime));
				Simulator.blockQ.put(nextBlkTime, ID);
			}
		}

		// Broadcast to peers.
		for (Node node : peerList) {
			if ( node == receivedFrom ) continue;
			else node.broadcastBlock(blk, this, broadcastTime + latency);
		}
	}

	private void shiftToNextBlock(BlockchainUnit nextBlock) {
		execBlockTxns(nextBlock.getBlock());
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
			execTxn(t);
		}

		/* DEBUG: Print Balances: *
		System.out.println(this.ID+" balances:");
		for(int id : balance.keySet()) {
			System.out.println(id+" "+balance.get(id));
		}
		System.out.println();
		*/
	}

	private void execTxn(Txn txn) {
		if(txn.coinbase || !txnSpent.containsKey(txn.ID)){
			if(!txn.coinbase) {
				balance.put(txn.payer.ID, balance.get(txn.payer.ID) - txn.amount);
				txnSpent.put(txn.ID, txnUnspent.get(txn.ID));
				txnUnspent.remove(txn.ID);
			}
			balance.put(txn.receiver.ID, balance.get(txn.receiver.ID) + txn.amount);
		}
	}

	public void generateBlock(int id) {
		Block blk = new Block(id, nextBlkTime,this);
		blk.prevBlock = lastBlock.getBlock();
		int count = 0;

		Txn coinbase = new Txn(0, null, this, 50, true);
		blk.addTxn(coinbase);

		HashMap<Integer, Integer> balCopy = new HashMap<>();

		for(int key: balance.keySet()) {
			balCopy.put(key, balance.get(key));
		}

		Iterator<Entry<Integer, TxnUnit>> unspentTxns = txnUnspent.entrySet().iterator();
		while(unspentTxns.hasNext()) {
			Entry<Integer, TxnUnit> entry = unspentTxns.next();
			TxnUnit tu = entry.getValue();
			if(tu.txn.amount <= balCopy.get(tu.txn.payer.ID) && tu.receivedTime < nextBlkTime) {
				//unspentTxns.remove();
				//txnSpent.put(tu.txn.ID, tu);
				blk.addTxn(tu.txn);
				balCopy.put(tu.txn.payer.ID, balCopy.get(tu.txn.payer.ID) - tu.txn.amount);
				count++;
			}
			if(count == 20)
				break;
		}
		broadcastBlock(blk, null, nextBlkTime);
	}

	public void generateTxn(int id, Node rcvr) {
		if (balance.get(ID) > 1) {
			Txn txn = new Txn(id, this, rcvr, Simulator.genIntRandom(1,balance.get(this.ID)));
			broadcastTxn(txn, null, nextTxnTime);
		}
		nextTxnTime += Simulator.genExpRandom(meanTxnTime);
	}
}
