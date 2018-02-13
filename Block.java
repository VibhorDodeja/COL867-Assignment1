import java.util.*;

public class Block {
      public int ID;
      public Block prevBlock;
      public ArrayList<Txn> txnList;

      Block(int id) {
            ID = id;
            prevBlock = null;
            txnList = new ArrayList<Txn>();
      }
}
