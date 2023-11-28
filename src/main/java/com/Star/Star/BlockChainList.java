package com.Star.Star;

import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Star.Star.services.RSAService;

import static com.Star.Star.services.ValidationService.validate;

/**
 * Data structure that represents single blockchain
 */
public class BlockChainList extends PeerToPeer implements List {

	Block block;
	Map<String, Block> blockChain;
	String difficultyStr = "";
	int difficultyNum;
	PublicKey pk;
	PrivateKey sk;
	int size;
	private List<String> recievedTransactionHashes;
	private List<String> recievedBlockHashes;
	private ServerAddress peer;
	private String ip;
	private int port;

	public BlockChainList(PrivateKey sk, PublicKey pk, int difficulty, String ip, int port, ServerAddress peer,
			int maxTpChunckSize)
			throws Exception {
		super(ip, port, peer, maxTpChunckSize);
		block = new Block(pk, "000000000000000");
		blockChain = Collections.synchronizedMap(new HashMap<String, Block>());
		recievedTransactionHashes = Collections.synchronizedList(new ArrayList<String>());
		recievedBlockHashes = Collections.synchronizedList(new ArrayList<String>());
		for (int i = 0; i < difficulty; i++)
			this.difficultyStr += "0";
		this.difficultyNum = difficulty;
		this.pk = pk;
		this.sk = sk;
		this.size = 0;
		this.peer = peer;
		this.ip = ip;
		this.port = port;
		System.out.println();
		System.out.println("Miners Public Key ->" + RSAService.pkToString(pk));
		System.out.println();
	}

	public List<TransactionPackage> getTransactions() {
		List<TransactionPackage> ts = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		for (int i = 0; i < block.getTransactions().size(); i++) {
			ts.add(block.getTransactions().get(i));
		}

		for (Entry<String, Block> b : blockChain.entrySet()) {
			for (TransactionPackage t : b.getValue().getTransactions()) {
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
		// synchronized (this) {

		if (object instanceof TransactionPackage) {
			TransactionPackage transactionPackage = (TransactionPackage) object;
			String tpHash = transactionPackage.getHash();
			try {
				if (!this.recievedTransactionHashes.contains(tpHash)) {
					recievedTransactionHashes.add(tpHash);
					this.size++;
					block.addTransaction(transactionPackage);
					block.signBlock(sk);
					if (block.getHash().substring(0, this.difficultyNum).equals(this.difficultyStr) && !recievedBlockHashes.contains(block.getHash())) {
						Block solvedBlock = block;
						block = new Block(pk, block.getHash());
						recievedBlockHashes.add(solvedBlock.getHash());
						blockChain.put(solvedBlock.blockBody.getPrevBlockHash(), solvedBlock);
						if (peer != null) {
							addToSend(solvedBlock);
						}
					} else {
						if (peer != null) {
							addToSend(transactionPackage);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("## " + e.getMessage());
				return false;
			}
			return true;
		} else {
			try {
				Block recBlock = (Block) object;
				String recBlockHash = recBlock.getHash();
				if (!recievedBlockHashes.contains(recBlockHash)) {
					recievedBlockHashes.add(recBlockHash);
					if (recBlock.getBlockBody().getPrevBlockHash().equals(block.getBlockBody().getPrevBlockHash())) {
						Block remainderCurBlock = new Block(pk, recBlock.getHash());
						List<TransactionPackage> curBlockTransactions = block.getTransactions();
						List<TransactionPackage> recBlockTransactions = recBlock.getTransactions();
						for (int i = 0; i < curBlockTransactions.size(); i++) {
							TransactionPackage tp = curBlockTransactions.get(i);
							if (!recBlockTransactions.contains(tp)) {
								remainderCurBlock.addTransaction(tp);
							}
						}
						for (int i = 0; i < recBlockTransactions.size(); i++) {
							TransactionPackage tp = recBlockTransactions.get(i);
							recievedTransactionHashes.add(tp.getHash());
						}
						this.size = this.size - block.getTransactions().size() + recBlockTransactions.size()
								+ remainderCurBlock.getTransactions().size();
						blockChain.put(recBlock.blockBody.getPrevBlockHash(), recBlock);
						block = remainderCurBlock;
						if (peer != null)
							addToSend(recBlock);
					} else {
						System.out.println("Prev Hash Does Not Match! ->" + recBlock.getBlockBody().getPrevBlockHash() + " " + recBlock.getHash() + " | "
								+ block.getBlockBody().getPrevBlockHash() + " " + block.getHash());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	// }
	}

	public int getHeight() {
		return blockChain.size();
	}

	public ArrayList<Block> getBlockChainList() throws Exception {
		ArrayList<Block> blockList = new ArrayList<Block>();
		String prevHash = "000000000000000";
		while(this.blockChain.containsKey(prevHash)) {
			Block curBlock = this.blockChain.get(prevHash);
			prevHash = curBlock.getHash();
			blockList.add(curBlock);
		}
		return blockList;
	}

	public ArrayList<String> getBlockChainBlockHashes() throws Exception {
		ArrayList<String> hashes = new ArrayList<String>();
		hashes.add(this.block.getHash());
		String prevHash = "000000000000000";
		while(this.blockChain.containsKey(prevHash)) {
			Block curBlock = this.blockChain.get(prevHash);
			prevHash = curBlock.getHash();
			hashes.add(prevHash);
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

	public JSONArray getJson() throws Exception {
		JSONArray json = new JSONArray();
		List<Block> blocks = this.getBlockChainList();
		for (int i = 0; i < blocks.size(); i++) {
			json.put(blocks.get(i).getJson());
		}
		return json;
	}

	public void writeToFile(String outputFilePath) throws Exception {
		JSONArray json = this.getJson();
		FileWriter fw = new FileWriter(outputFilePath);
		json.write(fw, 4, 0);
		fw.close();
	}

	@Override
	public void onRecieveMessage(Object msg) throws Exception {
		this.add(msg);
	}
}
