package hackathon1;

import appboot.LARVABoot;

/**
 *
 * @author carlo
 */
public class Hackathon1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LARVABoot boot = new LARVABoot();
        boot.Boot("isg2.ugr.es", 1099);
        boot.launchAgent("Piccolo", Piccolo.class);
        boot.WaitToShutDown();
    }
    
}
