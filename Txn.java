public class Txn {
      public int ID;
      public Node payer;
      public Node receiver;
      public int amount;

      Txn(int id, Node payer, Node receiver, int amount) {
            this.ID = id;
            this.payer = payer;
            this.receiver = receiver;
            this.amount = amount;
      }
}
