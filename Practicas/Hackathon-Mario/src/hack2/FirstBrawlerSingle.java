/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hack2;

import helloworld.AgentLARVAFull;
import jade.lang.acl.ACLMessage;
import glossary.Dictionary;
import jade.core.AID;
import java.util.ArrayList;



/**
 *
 * @author carlo
 */
public class FirstBrawlerSingle extends AgentLARVAFull {
    
    
    
   protected ACLMessage outgoing,
           outcoming;
    protected int wordWidth = 8,
            nWordsGame = 2;
    protected Dictionary dict;
    protected boolean Interactive = false,
            recruitDone = false;
    
    @Override
    public void setup() {
        this.enableDeepLARVAMonitoring();
        super.setup();
        usePerformatives = true;
        this.activateSequenceDiagrams();
        logger.onEcho();
        logger.onTabular();
        this.openRemote();
        dict = new Dictionary();
        dict.load("config/ES.words");
        myStatus = Status.START;
        problem = "SingleBrosBrawl";
    }
    
    @Override
     public Status MyOpenProblem() {
        // Look i the DF who is in charge of service PMANAGER
        if (this.DFGetAllProvidersOf(service).isEmpty()) {
            Error("Service PMANAGER is down");
            return Status.CHECKOUT;
        }
        problemManager = this.DFGetAllProvidersOf(service).get(0);
        Info("Found problem manager " + problemManager);
        
        // Send it a message to open a problem instance
        this.outbox = new ACLMessage();
        outbox.setSender(getAID());
        outbox.addReceiver(new AID(problemManager, AID.ISLOCALNAME));
        outbox.setContent("Request open " + problem);
        this.LARVAsend(outbox);
        Info("Request opening problem " + problem + " to " + problemManager);
        
        // There will be arriving two messages, one coming from the
        // Problem Manager and the other from the brand new Session Manager
        open = LARVAblockingReceive();
        Info(problemManager + " says: " + open.getContent());
        content = open.getContent();
        contentTokens = content.split(" ");
        if (contentTokens[0].toUpperCase().equals("AGREE")) {
            sessionKey = contentTokens[4];
            return Status.SOLVEPROBLEM;
        } else {
            Error(content);
            return Status.CHECKOUT;
        }
    }
    
   @Override
    public Status MySolveProblem(){
        if(!recruitDone){
              return Recruit();
        }else{
            return Brawls();
        }
    }
    
    public Status Recruit(){
        String recruitMark = "Recruit";
        String wordSent;
        
        Info("Starting the game");
        ArrayList<String> names = this.DFGetAllProvidersOf("Brawl " + sessionKey);
        if(names.isEmpty()){
            Alert("No other rivals are found");
            return Status.CHECKOUT; 
        }
        Info("All rivals found: " + names.toString());
        
        for(int i = 0; i<names.size();i++){
            String s= names.get(i);
            outbox = new ACLMessage(ACLMessage.REQUEST);
            outbox.setSender(getAID());
            outbox.addReceiver(new AID(s, AID.ISLOCALNAME));
            outbox.setConversationId(sessionKey);
            outbox.setContent(recruitMark);
            outbox.setReplyWith(outbox.getContent());
            LARVAsend(outbox);
            session =LARVAblockingReceive();
            
            if(session.getPerformative()== ACLMessage.AGREE){
                sessionManager = s;
                recruitDone = true;
                break;
            }
        }
        wordSent = selectWord(dict, null);
        outbox = session.createReply();
        outbox.setPerformative(ACLMessage.QUERY_IF);
        outbox.setContent(wordSent);
        outbox.setReplyWith(outbox.getContent());
        outbox.setInReplyTo("");
        
        outbox.setProtocol("*");
        
        outgoing = outbox;
        
        LARVAsend(outbox);
        
        return myStatus;
    }
    
    public Status Brawls(){
        String wordSent;
        
        session = LARVAblockingReceive();
        
        if(session.getPerformative() ==ACLMessage.QUERY_IF){
            Info("Answering to " +session.getContent());
            wordSent = selectWord(dict, session.getContent());
            if(wordSent == null){
                Info("Sorry I dont know how to continue"+session.getContent());
                return Status.EXIT;
            }
            
            if(session.getProtocol().startsWith("*")){
                outgoing = session.createReply();
                
                if(session.getProtocol().length() ==nWordsGame){
                    outgoing.setPerformative(ACLMessage.INFORM);
                }else{
                    outgoing.setPerformative(ACLMessage.QUERY_IF);
                }
                
                outgoing.setProtocol(session.getProtocol() + "*");
                
                
                
                outgoing.setContent(wordSent);
                outgoing.setReplyWith(outgoing.getContent());
                this.LARVAsend(outgoing);
            }else{
                outcoming = session.createReply();
                outcoming.setPerformative(ACLMessage.QUERY_IF);
                outcoming.setContent(wordSent);
                outcoming.setReplyWith(outcoming.getContent());
                this.LARVAsend(outcoming);
                
            }
        }else if(session.getPerformative() == ACLMessage.NOT_UNDERSTOOD){
            Message("I lost the game");
            return Status.CLOSEPROBLEM;
        }else if(session.getPerformative() == ACLMessage.CANCEL){
             Message("I win the game");
            return Status.CLOSEPROBLEM;
        }
        
        return myStatus;
    }
    
    public String selectWord (Dictionary dict, String previous){
        String w;
        if(previous ==null|| previous.length()==0){
            if (Interactive){
                w=inputLine("SEND A WORD\n\n\nPlease intro a word in Spanish");
            }else{
                w=dict.findFirstWord(wordWidth);
            }
        }else{
             if (Interactive){
                w=inputLine("ANSWER A WORD\n\n\nPlease intro a word in Spanish to answer");
            }else{
                w=dict.findNextWord(previous, wordWidth);
            }
        }
        return w;
    }
        
    
}