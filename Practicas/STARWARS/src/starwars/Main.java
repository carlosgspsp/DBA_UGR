
package starwars;

import appboot.LARVABoot;

/**
 *
 * @author carlo
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LARVABoot boot = new LARVABoot();
      boot.Boot("isg2.ugr.es", 1099);
      boot.launchAgent("Charly", AT_ST_DELIBERATIVE.class);
      boot.WaitToShutDown();
    }
    
}
