/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello;

import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author mrreload
 */
public class Hello {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        LiDar pili = new LiDar();
        Compass comp = new Compass();
        try
        {
           //pili.test(20, 10, false); 
            comp.compass_init(false);
            comp.get_readings(30, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
