package cp1.helloworld;
import jade.core.Agent;
/**
 *
 * @author carlo
 */
public class AgentJADE extends Agent{
    @Override
    public void setup(){
        System.out.println("Hello my name is "+this.getLocalName());
        doDelete();
    }
}
