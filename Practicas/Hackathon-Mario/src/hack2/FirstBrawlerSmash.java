
package hack2;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author carlo
 */
public class FirstBrawlerSmash extends FirstBrawlerSingle {

    protected HashMap<String, ACLMessage> outgoingH = new HashMap(),
            outcomingH = new HashMap();
    protected int nRivals = 2;

    @Override
    public void setup() {
        super.setup();
        this.activateSequenceDiagrams();
        problem = "SmashBrosBrawl";
    }

    @Override
public Status Brawls(){ 
        String who, wordSent; 
         
        session = LARVAblockingReceive(); 
         
        who= session.getSender().getLocalName(); 
         
        if(session.getPerformative() ==ACLMessage.QUERY_IF){ 
            Info("Answering to " +session.getContent()); 
            wordSent = selectWord(dict, session.getContent()); 
            if(wordSent == null){ 
                Info("Sorry I dont know how to continue"+session.getContent()); 
                return Status.EXIT; 
            } 
             
            outbox = session.createReply(); 
            //Core smash 
            if(session.getProtocol().length() ==nWordsGame){ 
                    outbox.setPerformative(ACLMessage.INFORM); 
                }else{ 
                    outbox.setPerformative(ACLMessage.QUERY_IF); 
                } 
                 
                outbox.setProtocol(session.getProtocol() + "*"); 
                 
            outbox.setContent(wordSent); 
        outbox.setReplyWith(outbox.getContent()); 
        this.LARVAsend(outbox); 
        }else if(session.getPerformative() == ACLMessage.NOT_UNDERSTOOD){ 
            Message("I lost the game"); 
            return Status.CLOSEPROBLEM; 
        }else if(session.getPerformative() == ACLMessage.CANCEL){ 
             Message("I win the game"); 
            return Status.CLOSEPROBLEM; 
        } 
         
        return myStatus; 
    }
    
    @Override
    public Status Recruit() {
        String recruitMark = "Recruit";
        String wordSent;

        Info("Starting the game");
        ArrayList<String> names = this.DFGetAllProvidersOf("Brawl " + sessionKey);
        if (names.isEmpty()) {
            Alert("No other rivals are found");
            return Status.CHECKOUT;
        }
        Info("All rivals found: " + names.toString());
        outbox = new ACLMessage(ACLMessage.CFP);
        outbox.setSender(getAID());
        outbox.setConversationId(sessionKey);
        outbox.setReplyWith(recruitMark);
        outbox.setContent(recruitMark);

        for (int i = 0; i < names.size(); i++) {
            outbox.addReceiver(new AID(names.get(i), AID.ISLOCALNAME));
        }

        LARVAsend(outbox);

        for (int i = 0; i < names.size(); i++) {
            inbox = LARVAblockingReceive(MessageTemplate.MatchInReplyTo(recruitMark));
            if (inbox.getPerformative() == ACLMessage.AGREE) {
                if (nRivals > 0) {
                    outbox = inbox.createReply();
                    outbox.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    wordSent = selectWord(dict, null);
                    outbox.setContent(wordSent);
                    outbox.setReplyWith(outbox.getContent());
                    outbox.setInReplyTo("");
                    outbox.setProtocol("*");
                    LARVAsend(outbox);

                    nRivals--;
                } else {
                    outbox = inbox.createReply();
                    outbox.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    LARVAsend(outbox);

                }
            }
        }
        
        recruitDone = true;
        return myStatus;
    }
}
