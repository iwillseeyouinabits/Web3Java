package com.Star.Star;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.Star.Star.services.ValidationService.validate;

/**
 * Data structure that represents single blockchain
 */
public class BlockChainList extends PeerToPeer implements List {

	Block block;
	List<Block> blockChain;
	String difficultyStr = "";
	int difficultyNum;
	PublicKey pk;
	PrivateKey sk;
	int size;
	private List<String> recievedTransactionHashes;
	private ArrayList<String> recievedBlockHashes = new ArrayList<String>();
	private ServerAddress peer;
	private String ip;
	private int port;

	public BlockChainList(PrivateKey sk, PublicKey pk, int difficulty, String ip, int port, ServerAddress peer,
			int maxTpChunckSize)
			throws Exception {
		super(ip, port, peer, maxTpChunckSize);
		block = new Block(sk, pk, "000000000000000");
		blockChain = Collections.synchronizedList(new ArrayList<Block>());
		recievedTransactionHashes = Collections.synchronizedList(new ArrayList<String>());
		for (int i = 0; i < difficulty; i++)
			this.difficultyStr += "0";
		this.difficultyNum = difficulty;
		this.pk = pk;
		this.sk = sk;
		this.size = 0;
		this.peer = peer;
		this.ip = ip;
		this.port = port;
	}

	public List<TransactionPackage> getTransactions() {
		List<TransactionPackage> ts = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		for (int i = 0; i < block.getTransactions().size(); i++) {
			ts.add(block.getTransactions().get(i));
		}

		for (Block b : blockChain) {
			for (TransactionPackage t : b.getTransactions()) {
				ts.add(t);
			}
		}

		return ts;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean contains(Object o) {
		return this.getTransactions().contains(o);
	}

	public Iterator<TransactionPackage> iterator() {
		Iterator<TransactionPackage> it = new Iterator<TransactionPackage>() {
			Block curBlock = block;
			int blockI = 0;
			int blockChainI = -1;

			public boolean hasNext() {
				return blockI + 1 < curBlock.getTransactions().size() || (blockChain.size() > blockChainI + 1
						&& blockChain.get(blockChainI + 1).getTransactions().size() > 0);
			}

			public TransactionPackage next() {
				if (!this.hasNext()) {
					throw new IndexOutOfBoundsException("Ran out of bounds");
				} else if (blockI + 1 < curBlock.getTransactions().size()) {
					blockI++;
					return curBlock.getTransactions().get(blockI);
				} else {
					blockChainI++;
					blockI = 0;
					block = blockChain.get(blockChainI);
					return curBlock.getTransactions().get(blockI);
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return it;
	}

	public TransactionPackage[] toArray() {
		TransactionPackage[] array = new TransactionPackage[size];
		Iterator<TransactionPackage> tpIterator = this.iterator();
		for (int i = 0; i < size; i++) {
			array[i] = tpIterator.next();
		}
		return array;
	}

	public TransactionPackage[] toArray(Object[] a) {
		TransactionPackage[] array = new TransactionPackage[size];
		Iterator<TransactionPackage> tpIterator = this.iterator();
		for (int i = 0; i < size; i++) {
			array[i] = tpIterator.next();
		}
		return array;
	}

	@Override
	public boolean add(Object object) {
		if (object instanceof TransactionPackage) {
			TransactionPackage transactionPackage = (TransactionPackage) object;
			String tpHash = transactionPackage.getHash();
			try {
				if (!this.recievedTransactionHashes.contains(tpHash)) {
					recievedTransactionHashes.add(tpHash);
					// todo add verification here
					// if(!validate(transactionPackage)) {
					// throw new Exception("Validation Failed");
					// };
					block.addTransaction(transactionPackage);
					if (block.getHash().substring(0, this.difficultyNum).equals(this.difficultyStr)) {
						block.signBlock();
						blockChain.add(block);
						block = new Block(sk, pk, block.getHash());
					}
					this.size++;
					if (peer != null)
						addToSend(transactionPackage);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return false;
			}
			return true;
		} else
			return this.addBlock((Block) object);
	}

	public boolean addTransactionPackage(TransactionPackage transactionPackage) {
		try {
			recievedTransactionHashes.add(transactionPackage.getHash());
			// todo add verification here
			// if(!validate(transactionPackage)) {
			// throw new Exception("Validation Failed");
			// };
			block.addTransaction(transactionPackage);
			if (block.getHash().substring(0, this.difficultyNum).equals(this.difficultyStr)) {
				block.signBlock();
				blockChain.add(block);
				block = new Block(sk, pk, block.getHash());
			}
			this.size++;
			if (peer != null)
				addToSend(transactionPackage);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}

	public boolean addBlock(Block block) {
		try {
			validate(blockChain.get(blockChain.size()), block);
			recievedBlockHashes.add(block.getHash());
			if (block.getHash().substring(0, this.difficultyNum).equals(this.difficultyStr)
					&& block.blockBody.prevBlockHash.equals(blockChain.get(blockChain.size() - 1).getHash())) {
				blockChain.add(block);
				this.block = new Block(sk, pk, block.getHash());
			}

			this.size += block.getTransactions().size();
			if (peer != null)
				addToSend(block);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}

	public int getHeight() {
		return blockChain.size();
	}

	public ArrayList<String> getBlockChainBlockHashes() throws Exception {
		ArrayList<String> hashes = new ArrayList<String>();
		hashes.add(this.block.getHash());
		for (Block b : this.blockChain) {
			hashes.add(b.getHash());
		}
		return hashes;
	}

	public boolean remove(Object o) {
		return false;
	}

	public boolean containsAll(Collection c) {
		return false;
	}

	public boolean addAll(Collection c) {
		return false;
	}

	public boolean addAll(int index, Collection c) {
		return false;
	}

	public boolean removeAll(Collection c) {
		return false;
	}

	public boolean retainAll(Collection c) {
		return false;
	}

	public void clear() {

	}

	public Object get(int index) {
		List<TransactionPackage> tps = getTransactions();
		return tps.get(index);
	}

	public Object set(int index, Object element) {
		return null;
	}

	public void add(int index, Object element) {
		return;
	}

	public Object remove(int index) {
		return null;
	}

	public int indexOf(Object o) {
		return toSend.size();
	}

	public int lastIndexOf(Object o) {
		return 0;
	}

	public ListIterator listIterator() {
		return null;
	}

	public ListIterator listIterator(int index) {
		return null;
	}

	public List subList(int fromIndex, int toIndex) {
		return null;
	}

	@Override
	public void onRecieveMessage(Object msg) throws Exception {
		try {
			boolean add;
			String hash;
			TransactionPackage tp = (TransactionPackage) msg;
			this.add(tp);
		} catch (ClassCastException e) {
			try {
				boolean add;
				Block block = (Block) msg;
				String hash = ((Block) msg).getHash();
				if (this.recievedBlockHashes.contains(hash)) {
					add = false;
				} else {
					add = true;
				}

				if (add) {
					this.add(block);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
