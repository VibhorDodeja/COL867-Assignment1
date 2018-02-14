import java.util.*;

public class Blockchain {

	private BlockchainUnit genesis;
	public HashMap<Integer, BlockchainUnit> blockMap;
	private ArrayList<BlockchainUnit> sideBlocks;

	public Blockchain() {
		genesis = new BlockchainUnit(new Block(0,0,null), 0);
		sideBlocks = new ArrayList<BlockchainUnit>();
		blockMap = new HashMap<Integer,BlockchainUnit>();
		blockMap.put(0, genesis);
	}

	public void addBlock(BlockchainUnit blockUnit) {
		if(blockMap.containsKey(blockUnit.getParentID())) {
			BlockchainUnit parent = blockMap.get(blockUnit.getParentID());
                  parent.addChild(blockUnit);
                  blockUnit.setDepth(parent.getDepth() + 1);
			blockMap.put(blockUnit.getBlock().ID, blockUnit);
		} else {
			sideBlocks.add(blockUnit);
		}

		for(BlockchainUnit block : sideBlocks) {
			if(blockUnit.getBlock().ID == block.getParentID()) {
				sideBlocks.remove(block);
				addBlock(block);
			}
		}
	}

      public boolean contains(int id) {
            return blockMap.containsKey(id);
      }

      public BlockchainUnit getUnit(int id) {
            return blockMap.get(id);
      }

}
