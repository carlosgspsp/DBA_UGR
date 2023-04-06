
package cp2.helloworld;
import appboot.JADEBoot;
import appboot.LARVABoot;

/**
 *
 * @author carlo
 */
public class CP2HelloWorld {

  
    public static void main(String[] args) {
      LARVABoot boot = new LARVABoot();
      boot.Boot("isg2.ugr.es", 1099);
      boot.launchAgent("Charly", AgentLARVA.class);
      boot.WaitToShutDown();
    }
    
}
