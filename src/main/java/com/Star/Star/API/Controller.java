package com.Star.Star.API;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/test")
public class Controller {

  BlockChainList history;

  public Controller() throws Exception {
    KeyPair kp = new RSAService().generateKeyPair();
    history = new BlockChainList("Miner", kp.getPrivate(), kp.getPublic(), 1, "127.0.0.1", 42069,
        null, 1);
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