/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactoryProviderRaspberry;
import java.io.IOException;

/**
 *
 * @author mrreload
 */
public class Compass {
    
    int COMPASS_ADDRESS = 0x1e;
    int x_offset = -9;
    int y_offset = -139;
    double scale = 0.92;
    byte samples815 = 0b01110000;
    byte gaingauss = 0b00100000;
    byte contsamp = 0b00000000;
    I2CBus cbus;
    I2CDevice compass;
    I2CFactoryProviderRaspberry rbus;
    
    Compass() {
        System.out.println("Starting INIT for device :" + COMPASS_ADDRESS);
        try {
            // get I2C bus instance
            cbus = I2CFactory.getInstance(I2CBus.BUS_1);
            I2CFactory.setFactory(rbus);
            compass = cbus.getDevice(COMPASS_ADDRESS);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void get_readings(int it, boolean dbg) throws InterruptedException{
        for (int i = 0; i < it; i++) {
            try {
                double bearing = get_bearing();
                String bngsz = Double.toString(bearing);
                System.out.println(String.format("Compass reading: %s, Heading: %s", bearing, bearingToCardinal(bearing)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
    
    void compass_init(boolean dbg) throws IOException {
        write_byte(0, samples815);  // Set to 8 samples @  15Hz 
        write_byte(1, gaingauss);  // 1.3 gain LSb / Gauss 1090 (default)
        write_byte(2, contsamp);  // Continuous sampling
    }
    
    int read_byte(int adr) throws IOException {
        //return bus.read_byte_data(self.address, adr);
        return compass.read(adr);
    }
    
    int read_word(int adr) throws IOException {
        int high = compass.read(adr);
        int low = compass.read(adr + 1);
        int val = (high << 8) + low;
        return val;
    }
    
    int read_word_2c(int adr) throws IOException {
        int val = read_word(adr);
        if (val >= 0x8000) {
            return -((65535 - val) + 1);
        } else {
            return val;
        }
        
    }
    
    void write_byte(int adr, byte value) throws IOException {
        //self.bus.write_byte_data(self.address, adr, value);
        compass.write(adr, value);
    }
    
    double get_bearing() throws IOException {
//        write_byte(0, samples815);  // Set to 8 samples @  15Hz 
//        write_byte(1, gaingauss);  // 1.3 gain LSb / Gauss 1090 (default)
//        write_byte(2, contsamp);  // Continuous sampling

        double x_out = (read_word_2c(3) - x_offset) * scale;
        double y_out = (read_word_2c(7) - y_offset) * scale;
        double z_out = (read_word_2c(5)) * scale;

        //bearing = math.atan2(y_out, x_out);
        double bearing = Math.atan2(y_out, x_out);
        if (bearing < 0) {
            bearing += 2 * Math.PI;
        }

        //System.out.println(String.format("Bearing: %s", Double.toString(Math.toDegrees(bearing))));
        return Math.toDegrees(bearing);
    }
    
    String bearingToCardinal(double x) {
        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        return directions[(int) Math.round((((double) x % 360) / 45))];
    }
}
