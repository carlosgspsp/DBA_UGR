package starwars;

import Environment.Environment;
import ai.Choice;

/**
 *
 * @author carlo
 */
public class AT_ST_BASICAVOID extends AT_ST_DIRECTDRIVE {

    @Override
    public void setup() {
        super.setup();
        problem = "Halfmoon1";
    }

    public boolean isFreeFront() {
        int slopeFront, visualFront, visualHere;
        
        visualHere = E.getPolarVisual()[2][1];
        visualFront = E.getPolarVisual()[0][0];
        slopeFront = E.getPolarLidar()[2][1];
        
        return slopeFront >= -E.getMaxslope() && slopeFront <= E.getMaxslope()
                && visualFront >= E.getMinlevel()
                && visualFront <= E.getMaxlevel();
    }
    
    public double goAvoid(Environment E, Choice a){
        if (a.getName().equals("RIGHT")){
            return Choice.ANY_VALUE;
        } else {
            return Choice.MAX_UTILITY;
        }
    }    
    
    @Override
    protected double U(Environment E, Choice a) {
        if (isFreeFront())
        {
          return goAhead(E, a);  
        } else {
            return goAvoid(E,a);
        }
        
    }
}
