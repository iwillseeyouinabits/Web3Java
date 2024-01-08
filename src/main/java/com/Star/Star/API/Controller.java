package com.Star.Star.API;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.Star.Star.BlockChainList.BlockChainList;
import com.Star.Star.BlockChainList.BlockChainListParts.CurrencyTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.DockerTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.HttpTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.ServerAddress;
import com.Star.Star.BlockChainList.BlockChainListParts.Transaction;
import com.Star.Star.BlockChainList.BlockChainListParts.TransactionPackage;
import com.Star.Star.BlockChainList.services.RSAService;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/API")
public class Controller {

  BlockChainList history;

  public Controller() throws Exception {
    // Enter data using BufferReader
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("Would you like to provide your own Public Key and Private Key? Y/n?");
    String responseKP = reader.readLine();
    PublicKey pk;
    PrivateKey sk;
    if (responseKP.toLowerCase().equals("y")) {
      System.out.println("Public Key: ");
      pk = RSAService.stringToPk(reader.readLine());
      System.out.println("Private Key: ");
      sk = RSAService.stringToSk(reader.readLine());
    } else if (responseKP.toLowerCase().equals("n")) {
      KeyPair kp = new RSAService().generateKeyPair();
      pk = kp.getPublic();
      sk = kp.getPrivate();
      System.out.println("Public Key => " + RSAService.pkToString(pk));
      System.out.println("Private Key => " + RSAService.skToString(sk));
    } else {
      throw new Exception("Not A Valid Response");
    }
    
    System.out.println("Port To Run Block Chain On: ");
    int bcPort = Integer.parseInt(reader.readLine());
    
    System.out.println("Number Of Peers: ");
    int numPeers = Integer.parseInt(reader.readLine());
    ServerAddress[] peers = null;
    if (numPeers > 0) {
      peers = new ServerAddress[numPeers];
      for (int i = 0; i < numPeers; i++) {
        System.out.println("Port of peer #" + i + " : ");
        int peerPort = Integer.parseInt(reader.readLine());
        System.out.println("Public Key of peer #" + i + " : ");
        PublicKey peerPK = RSAService.stringToPk(reader.readLine());
        System.out.println("Is the peer on a local host (Y/n) : ");
        String localHostPeerResponse = (reader.readLine());
        String peerAddress = "127.0.0.1";
        if (localHostPeerResponse.toLowerCase().equals("n")) {
          System.out.println("IP of peer: ");
          peerAddress = (reader.readLine());
        }
        peers[i] = new ServerAddress(peerAddress, peerPort, peerPK);
      }
    }
    
    history = new BlockChainList("Miner", sk, pk, 1, "127.0.0.1", bcPort,
        peers, 1);

    if (numPeers > 0) {
      System.out.println("Press any key to connect BlockChain to Peers");
      reader.readLine();
      history.connectToPeer();
    }
    reader.close();
  }

  @RequestMapping(value = "/getKeyPair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getKeyPair() {
    try {
      KeyPair kp = new RSAService().generateKeyPair();
      return new ResponseEntity<>("{\"pk\":\"" + RSAService.pkToString(kp.getPublic()) + "\", \"sk\":\""
          + RSAService.skToString(kp.getPrivate()) + "\"}", HttpStatus.OK);
    } catch (NoSuchAlgorithmException e) {
      return new ResponseEntity<>("{\"Failed To Make Keys\":\"" + e.getMessage() + "\"}", HttpStatus.CONFLICT);
    }
  }

  @RequestMapping(value = "/addHTTPTransaction", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> addHTTPTransaction(@RequestParam("clientAdr") String clientAdr,
      @RequestParam("clientSk") String clientSk, @RequestParam("websiteAdr") String websiteAdr, 
      @RequestParam("hostAdr") String hostAdr, @RequestParam("postJson") String postJson) {
    try {
      Transaction transaction = new HttpTransaction(RSAService.stringToPk(clientAdr),
          RSAService.stringToPk(websiteAdr), RSAService.stringToPk(hostAdr), postJson,
          UUID.randomUUID().toString());
      TransactionPackage tp = new TransactionPackage(transaction, RSAService.stringToSk(clientSk));
      history.add(tp);
      return new ResponseEntity<>("{\"Success\": " + postJson + "}", HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("{\"Failed To Add HTTP Request\":\"" + e.getMessage() + "\"}", HttpStatus.CONFLICT);
    }
  }

  @RequestMapping(value = "/addDockerTransaction", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> addDockerTransaction(@RequestParam("websiteAdr") String websiteAdr,
      @RequestParam("websiteSk") String websiteSk, @RequestParam("dockerFile") String dockerFile,
      @RequestParam("archiveBase64") String archiveBase64) {
    try {
      Transaction transaction = new DockerTransaction(RSAService.stringToPk(websiteAdr),
          dockerFile, archiveBase64,
          UUID.randomUUID().toString());
      TransactionPackage tp = new TransactionPackage(transaction, RSAService.stringToSk(websiteSk));
      history.add(tp);
      return new ResponseEntity<>("{\"Success\":\"Sent Website\"}", HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("{\"Failed To Add Website\":\"" + e.getMessage() + "\"}", HttpStatus.CONFLICT);
    }
  }

  @RequestMapping(value = "/addCurrencyTransaction", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> addCurrencyTransaction(@RequestParam("senderPk") String senderPk,
      @RequestParam("senderSk") String senderSk, @RequestParam("recipientPk") String recipientPk,
      @RequestParam("tokens") int tokens) {
    try {
      Transaction transaction = new CurrencyTransaction(RSAService.stringToPk(senderPk),
          RSAService.stringToPk(recipientPk), tokens,
          UUID.randomUUID().toString());
      TransactionPackage tp = new TransactionPackage(transaction, RSAService.stringToSk(senderSk));
      history.add(tp);
      return new ResponseEntity<>("{\"Success\":\"Sent " + tokens + " tokens\"}", HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("{\"Failed To Add Currency\":\"" + e.getMessage() + "\"}", HttpStatus.CONFLICT);
    }
  }

  @RequestMapping(value = "/getTransactions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getTransactions() {
    List<TransactionPackage> tps = history.getTransactions();
    List<String> out = new ArrayList<String>();
    for (TransactionPackage tp : tps) {
      out.add(tp.toString());
    }
    return new ResponseEntity<>(out.toString(), HttpStatus.OK);
  }

  @RequestMapping(value = "/getVerifiedBlocks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ConcurrentHashMap<String, Boolean> > getVerifiedBlocks() {
    return new ResponseEntity<>(history.getVerifiedBlocks(), HttpStatus.OK);
  }

  @RequestMapping(value = "/getBlockChain", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getBlockChain() {
    try {
      return new ResponseEntity<>(history.getJson().toString(), HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("{\"Failed To Get BlockChain\":\"" + e.getMessage() + "\"}", HttpStatus.CONFLICT);
    }
  }

}