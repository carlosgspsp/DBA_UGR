
package starwars;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;

/**
 *
 * @author carlo
 */
public class AT_ST_DIRECTDRIVE extends AT_ST {
   
    @Override
    public void setup()
    {
        super.setup();
        A = new DecisionSet();
        A.addChoice(new Choice("MOVE"))
                .addChoice(new Choice("LEFT"))
                .addChoice(new Choice("RIGHT"));
        problem = "SandboxTesting";
        
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
        this.openRemote();
        this.MyReadPerceptions();
        return Status.SOLVEPROBLEM;

    }
     
    @Override
     public Status MySolveProblem(){
         System.out.println("hols");
         if (G(E)){
             Message("Problem "+problem+" has been solved");
             return Status.CLOSEPROBLEM;
         }
         System.out.println("Hola2");
         if (!Ve(E)) {
             Alert("Sorry, but the agent has crashed!");
             return Status.CLOSEPROBLEM;
         }
         Choice a = this.Ag(E, A);
         if (a == null){
             Alert("Sorry, no action found!");
             return Status.CLOSEPROBLEM;
         }
         Info("Try to execute "+A);
         this.MyExecuteAction(a.getName());
         this.MyReadPerceptions();
         return Status.SOLVEPROBLEM;
     }
     
     
     public double goAhead(Environment E, Choice a){
         if (a.getName().equals("MOVE")){
             return U(S(E,a));
         } else {
             return U(S(E,a), new Choice ("MOVE"));
         }
     }
     
    @Override
     protected double U(Environment E, Choice a){
         return goAhead(E,a);
     }
     
    @Override
     public String easyPrintPerceptions(){
         return super.easyPrintPerceptions()+"\n";//+
 //                this.Prioritize(E, A).toString();
     }
}
