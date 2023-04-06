
package cp2.helloworld;

import agents.LARVAFirstAgent;


public class AgentLARVA extends LARVAFirstAgent {
    
    @Override
    public void setup(){
        super.setup();
        logger.onTabular();
        Info("Configuring...");
    }
    
    @Override
    public void Execute(){
       Info("Executing...");
       doExit();
    }
    
    @Override
    public void takeDown(){
       Info("Taking down...");
       super.takeDown();
    }
    
}
