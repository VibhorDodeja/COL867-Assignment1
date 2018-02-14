import java.util.*;

public class Block {
      public int ID;
      public Block prevBlock;
      public ArrayList<Txn> txnList;

      public int creationTime;
      public Node createdBy;

      Block(int id, int creationTime, Node god) {
            ID = id;
            prevBlock = null;
            txnList = new ArrayList<>();
            this.creationTime = creationTime;
            createdBy = god;
      }

      public void addTxn(Txn txn) {
    	  txnList.add(txn);
      }
}
