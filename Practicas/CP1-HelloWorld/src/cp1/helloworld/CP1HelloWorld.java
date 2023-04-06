
package cp1.helloworld;
import appboot.JADEBoot;

/**
 *
 * @author carlo
 */
public class CP1HelloWorld {

  
    public static void main(String[] args) {
      JADEBoot boot = new JADEBoot();
      boot.Boot("isg2.ugr.es", 1099);
      boot.launchAgent("Charly", AgentJADE.class);
      boot.WaitToShutDown();
    }
    
}
