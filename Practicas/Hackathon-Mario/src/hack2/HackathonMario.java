/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hack2;

import appboot.LARVABoot;
import static crypto.Keygen.getHexaKey;

/**
 *
 * @author carlo
 */
public class HackathonMario {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String name;
        LARVABoot boot = new LARVABoot();
        boot.Boot("isg2.ugr.es",1099);
        name = "Carlos"+getHexaKey(3);
        if(name != null && name.length()>0){
           //boot.loadAgent("F"+name, FirstBrawlerSingle.class);
          //  boot.loadAgent("D"+name, DialogBrawlerSingle.class);
            boot.loadAgent("FS"+name, FirstBrawlerSmash.class);
          //  boot.loadAgent("DS"+name, DialogBrawlerSmash.class);
            boot.WaitToShutDown();
        }
       
    }
    
}
