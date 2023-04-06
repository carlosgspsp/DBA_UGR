package starwars;

import agents.LARVAFirstAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import tools.emojis;
import world.Perceptor;

/**
 *
 * @author carlo
 */
public class AT_ST extends LARVAFirstAgent {

    // The execution on any agent might be seen as a finite state automaton
    // whose states are these
    enum Status {
        START, // Begin execution
        CHECKIN, // Send passport to Identity Manager
        OPENPROBLEM, // ASks Problem Manager to open an instance of a problem
        SOLVEPROBLEM, // Really do this!
        CLOSEPROBLEM, // After that, ask Problem Manager to close the problem
        CHECKOUT, // ASk Identity Manager to leave out
        JOINSESSION,
        EXIT
    }
    Status myStatus;    // The current state
    String service = "PMANAGER", // How to find Problem Manager in DF
            problem = "SandboxTesting", // Name of the problem to solve
            problemManager = "", // Name of the Problem Manager, when it woudl be knwon
            sessionManager, // Same for the Session Manager
            content, // Content of messages
            sessionKey,// The key for each work session 
            action,
            preplan;
    ACLMessage open, session; // Backup of relevant messages
    int iplan = 0 , myEnergy = 0;
    boolean showPerceptions = false;
    String[] contentTokens,
            plan;// = new String[]{"RIGHT", "MOVE", "MOVE", "MOVE", "MOVE", "MOVE", "MOVE", "EXIT"}; // To parse the content
    String problems[]={ "SandboxTesting",
            "FlatNorth",
            "FlatNorthWest",
            "FlatSouth",
            "Bumpy0",
            "Bumpy1",
            "Bumpy2",
            "Bumpy3",
            "Bumpy4",
            "Halfmoon1",
            "Halfmoon3"};

    // This is the firs method executed by any agent, right before its creation
    @Override
    public void setup() {
        this.enableDeepLARVAMonitoring();
        super.setup();
        logger.onTabular();
        myStatus = Status.START;  
        this.setupEnvironment();
        
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
            case JOINSESSION:
                myStatus = MyJoinSession();
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
        // Selector of the problem to solve
        problem = this.inputSelect("Please select problem to solve", problems, problem);
        if (problem == null) {
            return Status.CHECKOUT;
        }

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
            return Status.JOINSESSION;
        } else {
            Error(content);
            return Status.CHECKOUT;
        }
    }

    public Status MyJoinSession() {
        this.DFAddMyServices(new String[]{"TYPE AT_ST"});
        outbox = session.createReply();
        outbox.setContent("Request join session " + sessionKey);
        this.LARVAsend(outbox);
        session = this.LARVAblockingReceive();
        if (!session.getContent().startsWith("Confirm")) {
            Error("Could not join session " + sessionKey + " due " + session.getContent());
            return Status.CLOSEPROBLEM;
        }
        
        return Status.SOLVEPROBLEM;

    }

    public Status MySolveProblem() {
        this.MyReadPerceptions();
        if (plan == null) {
            action = this.inputSelect("Please select action", new String[]{"MOVE", "LEFT", "RIGHT", "EXIT"}, action);
            preplan += "\""+action+"\","; 
        } else {
            action = plan[iplan++];
        }
        if (action == null || action.equals("EXIT")) {
            return Status.CLOSEPROBLEM;
        }
        if (!MyExecuteAction(action)) {
            return Status.CLOSEPROBLEM;
        }
        return Status.SOLVEPROBLEM;
    }

    public boolean MyExecuteAction(String action) {
        Info("Executing action " + action);
        outbox = session.createReply();
        outbox.setContent("Request execute " + action + " session " + sessionKey);
        this.LARVAsend(outbox);
        session = this.LARVAblockingReceive();
        if (!session.getContent().startsWith("Inform")) {
            Error("Unable to execute action " + action + " due to " + session.getContent());
            return false;
        }
        return true;
    }
    
    public boolean MyReadPerceptions() {
        Info("Reading perceptions... ");
        outbox = session.createReply();
        outbox.setContent("Query sensors session " + sessionKey);
        this.LARVAsend(outbox);
        session = this.LARVAblockingReceive();
        if (session.getContent().startsWith("Failure")) {
            Error("Unable to read perceptions due to " + session.getContent());
            return false;
        }
        this.getEnvironment().setExternalPerceptions(session.getContent());
        Info(this.easyPrintPerceptions());
        return true;
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

     public String easyPrintPerceptions() {
        String res;
        int matrix[][];
        return "";
        /*
        
        if (!logger.isEcho()) {
            return "";
        }
        if (getEnvironment() == null) {
            Error("Environment is unacessible, please setupEnvironment() first");
            return "";
        }
        if (!showPerceptions) {
            return "";
        }
        res = "\n\nReading of sensors\n";
        if (getEnvironment().getName() == null) {
            res += emojis.WARNING + " UNKNOWN AGENT";
            return res;
        } else {
            res += emojis.ROBOT + " " + getEnvironment().getName();
        }
        res += "\n";
        res += String.format("%10s: %05d W %05d W %05d W\n", "ENERGY",
                getEnvironment().getEnergy(), getEnvironment().getEnergyburnt(), myEnergy);
        res += String.format("%10s: %15s\n", "POSITION", getEnvironment().getGPS().toString());
//        res += "PAYLOAD "+getEnvironment().getPayload()+" m"+"\n";
        res += String.format("%10s: %05d m\n", "X", getEnvironment().getGPS().getXInt())
                + String.format("%10s: %05d m\n", "Y", getEnvironment().getGPS().getYInt())
                + String.format("%10s: %05d m\n", "Z", getEnvironment().getGPS().getZInt())
                + String.format("%10s: %05d m\n", "MAXLEVEL", getEnvironment().getMaxlevel())
                + String.format("%10s: %05d m\n", "MAXSLOPE", getEnvironment().getMaxslope());
        res += String.format("%10s: %05d m\n", "GROUND", getEnvironment().getGround());
        res += String.format("%10s: %05d º\n", "COMPASS", getEnvironment().getCompass());
        if (getEnvironment().getTarget() == null) {
            res += String.format("%10s: " + "!", "TARGET");
        } else {
            res += String.format("%10s: %05.2f m\n", "DISTANCE", getEnvironment().getDistance());
            res += String.format("%10s: %05.2f º\n", "ABS ALPHA", getEnvironment().getAngular());
            res += String.format("%10s: %05.2f º\n", "REL ALPHA", getEnvironment().getRelativeAngular());
        }
//        res += "\nVISUAL ABSOLUTE\n";
//        matrix = getEnvironment().getAbsoluteVisual();
//        for (int y = 0; y < matrix[0].length; y++) {
//            for (int x = 0; x < matrix.length; x++) {
//                res += printValue(matrix[x][y]);
//            }
//            res += "\n";
//        }
//        for (int x = 0; x < matrix.length; x++) {
//            if (x != matrix.length / 2) {
//                res += "----";
//            } else {
//                res += "[  ]-";
//            }
//        }
        res += "\nVISUAL RELATIVE\n";
        matrix = getEnvironment().getRelativeVisual();
        for (int y = 0; y < matrix[0].length; y++) {
            for (int x = 0; x < matrix.length; x++) {
                res += printValue(matrix[x][y]);
            }
            res += "\n";
        }
        for (int x = 0; x < matrix.length; x++) {
            if (x != matrix.length / 2) {
                res += "----";
            } else {
                res += "[  ]-";
            }
        }
//        res += "VISUAL POLAR\n";
//        matrix = getEnvironment().getPolarVisual();
//        for (int y = 0; y < matrix[0].length; y++) {
//            for (int x = 0; x < matrix.length; x++) {
//                res += printValue(matrix[x][y]);
//            }
//            res += "\n";
//        }
//        res += "\n";
        res += "LIDAR RELATIVE\n";
        matrix = getEnvironment().getRelativeLidar();
        for (int y = 0; y < matrix[0].length; y++) {
            for (int x = 0; x < matrix.length; x++) {
                res += printValue(matrix[x][y]);
            }
            res += "\n";
        }
        for (int x = 0; x < matrix.length; x++) {
            if (x != matrix.length / 2) {
                res += "----";
            } else {
                res += "-^^-";
            }
        }
        res += "\n";
        return res;*/
    }

    protected String printValue(int v) {
        if (v == Perceptor.NULLREAD) {
            return "XXX ";
        } else {
            return String.format("%03d ", v);
        }
    }

    protected String printValue(double v) {
        if (v == Perceptor.NULLREAD) {
            return "XXX ";
        } else {
            return String.format("%05.2f ", v);
        }
    }
    
}
