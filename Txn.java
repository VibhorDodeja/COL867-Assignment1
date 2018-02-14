public class Txn {
	public int ID;
	public Node payer;
	public Node receiver;
	public int amount;
	public boolean coinbase;

	Txn(int id, Node payer, Node receiver, int amount) {
		this.ID = id;
		this.payer = payer;
		this.receiver = receiver;
		this.amount = amount;
		this.coinbase = false;
	}
	Txn(int id, Node payer, Node receiver, int amount, boolean coinbase) {
		this.ID = id;
		this.payer = payer;
		this.receiver = receiver;
		this.amount = amount;
		this.coinbase = coinbase;
	}
}
