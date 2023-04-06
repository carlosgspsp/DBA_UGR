/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hackathon1;

import agents.LARVAFirstAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author carlo
 */
public class Piccolo extends LARVAFirstAgent {

    // The execution on any agent might be seen as a finite state automaton
    // whose states are these
    enum Status {
        START, // Begin execution
        CHECKIN, // Send passport to Identity Manager
        OPENPROBLEM, // ASks Problem Manager to open an instance of a problem
        SOLVEPROBLEM, // Really do this!
        CLOSEPROBLEM, // After that, ask Problem Manager to close the problem
        CHECKOUT, // ASk Identity Manager to leave out
        EXIT
    }
    Status myStatus;    // The current state
    String service = "PMANAGER", // How to find Problem Manager in DF
            problem = "AnswerToSongoanda", // Name of the problem to solve
            problemManager = "", // Name of the Problem Manager, when it woudl be knwon
            sessionManager, // Same for the Session Manager
            content, // Content of messages
            sessionKey; // The key for each work session 
    ACLMessage open, session; // Backup of relevant messages
    String[] contentTokens; // To parse the content

    // This is the firs method executed by any agent, right before its creation
    @Override
    public void setup() {
        // Deep monitoring of the execution of each agent. It is a kind of
        // guardrail only for small problems. In large projects it is preferrable
        // comment it since it consumes many messages and time
        // this must be called before the super.setup();
        this.enableDeepLARVAMonitoring();
        
        // The constructor of the superclass
        super.setup();

        // This feature allows the automatic generation of sequence diagrams for the running program
        // It is very costly in terms of executoin time, so it must be used carefully in large problems
        this.activateSequenceDiagrams();
        //this.deactivateSequenceDiagrams();
        
        // This feature enables a logger of all the activity of the agent.
        // When it is ON, all Info( ) messages are displayed on screen
        // When it is OFF, it executes quietly, without showing any screen output
        logger.onEcho();
        //logger.offEcho();
        
        // ACtivates a tabular-like output of agents
        logger.onTabular();
                
        // First status of execution
        myStatus = Status.START;
    }

    // Main execution body andter the executoin of setup( ). It executes continuously until 
    // the exact execution of doExit() after which it executes
    // takeDown()
    @Override
    public void Execute() {
        Info("Status: " + myStatus.name());
        switch (myStatus) {
            case START:
                myStatus = Status.CHECKIN;
                break;
            case CHECKIN:
                // The execution of a state (as a method) returns
                // the next state
                myStatus = MyCheckin();
                break;
            case OPENPROBLEM:
                myStatus = MyOpenProblem();
                break;
            case SOLVEPROBLEM:
                myStatus = MySolveProblem();
                break;
            case CLOSEPROBLEM:
                myStatus = MyCloseProblem();
                break;
            case CHECKOUT:
                myStatus = MyCheckout();
                break;
            case EXIT:
            default:
                doExit();
                break;
        }
    }

    // It only executes when the agent dies programmatically, that is, under control
    @Override
    public void takeDown() {
        Info("Taking down...");
        // Save the Sequence Diagram
        this.saveSequenceDiagram("./" + getLocalName() + ".seqd");
        super.takeDown();
    }

    // It loads the passport selected in the GUI and send it to the Identity manager
    public Status MyCheckin() {
        Info("Loading passport and checking-in to LARVA");
        // It loads the passport specified in the GUI, but otherwise
        // it might load any other passpor manually (uncomment)
        //this.loadMyPassport("config/ANATOLI_GRISHENKO.passport");
        
        // If checkin works, then continue, else exti
        if (!doLARVACheckin()) {
            Error("Unable to checkin");
            return Status.EXIT;
        }
        return Status.OPENPROBLEM;
    }

    // Says good by to the Identity Manager and leaves LARVA
    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

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
            session = LARVAblockingReceive();
            sessionManager = session.getSender().getLocalName();
            Info(sessionManager + " says: " + session.getContent());
            return Status.SOLVEPROBLEM;
        } else {
            Error(content);
            return Status.CHECKOUT;
        }
    }

    public Status MySolveProblem() {
        return AnswerToSongoanda();
    }
    
    public Status AnswerToSongoanda() {
        String key, counterkey;
        outbox = session.createReply();
        outbox.setContent("Hello");
        this.LARVAsend(outbox);
        inbox = this.LARVAblockingReceive();
        key = inbox.getContent();
        Info("Songoanda ha dicho: "+key);
        counterkey = new StringBuilder(key).reverse().toString();
        outbox = inbox.createReply();
        outbox.setContent(counterkey);
        this.LARVAsend(outbox);
        return Status.CLOSEPROBLEM;
    }
    

    public Status MyCloseProblem() {
        // AFter all, it is mandatory closing the problem
        // by replying to the backup message
        outbox = open.createReply();
        outbox.setContent("Cancel session " + sessionKey);
        Info("Closing problem Helloworld, session " + sessionKey);
        this.LARVAsend(outbox);
        inbox = LARVAblockingReceive();
        Info(problemManager + " says: " + inbox.getContent());
        return Status.CHECKOUT;
    }

}