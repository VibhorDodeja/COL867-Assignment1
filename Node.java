import java.util.*;

public class Node {
      public int ID;
      public boolean isFast;

      public HashSet<Node> peerList;
      public HashSet<Integer> txnBCasted;

      Node(int id, boolean fast) {
            this.ID = id;
            this.isFast = fast;

            peerList = new HashSet<Node>();
            txnBCasted = new HashSet<Integer>();
      }

      public void broadcastTxn(Txn txn, Node receivedFrom, int broadcastTime) {
            // Print TXN received.
            // System.out.println("Node ID: "+String.valueOf(this.ID)+"; Txn ID: "+String.valueOf(txn.ID));

            if (txnBCasted.contains(txn.ID)) return;
            txnBCasted.add(txn.ID);

            int latency = Simulator.minLatency;
            if (receivedFrom == null) latency = 0;
            else if ( receivedFrom.isFast && this.isFast) latency += 1;
            else { latency += 19; }

            for (Node node : peerList) {
                  if ( node == receivedFrom ) continue;
                  else node.broadcastTxn(txn, this, broadcastTime + latency);
            }
      }

      public void broadcastBlock(Block blk, Node receivedFrom, int broadcastTime) {
            // Print BLK received.
            // System.out.println("Node ID: "+String.valueOf(this.ID)+"; Block ID: "+String.valueOf(blk.ID));
      }
}
