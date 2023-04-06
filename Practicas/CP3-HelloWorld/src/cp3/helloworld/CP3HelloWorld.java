
package cp3.helloworld;
import appboot.JADEBoot;
import appboot.LARVABoot;

/**
 *
 * @author carlo
 */
public class CP3HelloWorld {

  
    public static void main(String[] args) {
      LARVABoot boot = new LARVABoot();
      boot.Boot("isg2.ugr.es", 1099);
      boot.launchAgent("Charly", AgentLARVAFull.class);
      boot.WaitToShutDown();
    }
    
}
