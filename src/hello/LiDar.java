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
import com.pi4j.wiringpi.I2C;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 *
 * @author mrreload
 */
public class LiDar {

    I2CBus bus;
    I2CDevice lidar;
    I2CFactoryProviderRaspberry rbus;
    int BUSY_REG = 0x01;
    int MEASURE_INIT = 0x00;
    int MEASURE_CMD = 0x04;
    int DISTANCE_REG_HI = 0x0f;
    int DISTANCE_REG_LO = 0x10;
    int DISTANCE_REG_BOTH = 0x8f;
    int STATUS_REG = 0x47;
    int LIDAR_ADDRESS = 0x62;
    final int STAT_BUSY = 0x01;
    final int STAT_REF_OVER = 0x02;
    final int STAT_SIG_OVER = 0x04;
    final int STAT_PIN = 0x08;
    final int STAT_SECOND_PEAK = 0x10;
    final int STAT_TIME = 0x20;
    final int STAT_INVALID = 0x40;
    final int STAT_EYE = 0x80;
    final int ERROR_READ = -1;

    boolean _dbg = false;

    LiDar() {
        System.out.println("Starting INIT for device :" + LIDAR_ADDRESS);
        try {
            // get I2C bus instance
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            //I2CFactory.setFactory(rbus);
            lidar = bus.getDevice(LIDAR_ADDRESS);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void test(int it, int del, boolean dbg) throws Exception {
        System.out.println(LocalDateTime.now());
        int st, fd, res, i;
        char ver;
        double cm_to_inch = 2.54; // (0.39);
        float cm, inches;
        
        //init lidar
        fd = lidar_init(dbg);
        System.out.println(String.format("INIT Status: %d", fd));
        if (fd == -1) {
            System.out.println("initialization error");
        } else {
            for (i = 0; i < it; i++) {
                res = lidar_read(fd);
                st = lidar_status(fd);
                //ver = lidar_version(fd);
                double in = (double) res * 0.39;
                double feet = in / 12;
                double leftover = in % 12;
                //System.out.printf("%.1f cm = %d feet, %.1f inches\n", cm, feet, inches);
                String reading = String.format("Reading %d: Status %s, %.3f cm, %.3f in, OR %d FEET %.3f INCHES\n\r", i, st, (float) res, (float) in, (int) feet, (float) leftover);
                System.out.println(reading);

                lidar_status_print(st);

                Thread.sleep(del);
            }
            System.out.println(LocalDateTime.now());
        }
    }

//    void LidarGetReading() {
//        try {
//            // get I2C bus instance
//            //bus = I2CFactory.getInstance(I2CBus.BUS_1);
//            //lidar = bus.getDevice(0x62);
//            //init lidar get status
//            int fd = lidar.read(STATUS_REG);
//
//            if (fd == -1) {
//                System.out.println("Lidar Intialization ERROR:");
//            } else {
//                System.out.println("Lidar Status: " + String.valueOf(fd));
//                for (int i = 0; i < 10; i++) {
//                    int res = lidar_read(fd);
//                    Thread.sleep(50);
//                    fd = lidar.read(STATUS_REG);
//                    String st = lidar_status_code(fd);
//                    //ver = lidar_version(fd);
//                    double in = (double) res * 0.39;
//                    double feet = in / 12;
//                    double leftover = in % 12;
//                    String s = String.format("Reading %d: Status %s, %.3f cm, %.3f in, OR %d FEET %.3f INCHES\n\r", i, st, (float) res, (float) in, (int) feet, (float) leftover);
//                    //System.out.printf("%.3f cm, %.3f in, OR %d FEET %.3f INCHES\n\r", (float) res, (float) in, (int) feet, (float) leftover);
//                    System.out.println(s);
//                    //lidar_status_print(st);
//
//                    Thread.sleep(50);
//                }
//            }
//
//            //lidar.write(0x00, (byte) 0x04);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
    // Read distance in cm from LidarLite
    int lidar_read(int fd) throws Exception {
        int hiVal, loVal, i = 0;

        // send "measure" command
        hiVal = I2C.wiringPiI2CWriteReg8(fd, MEASURE_INIT, MEASURE_CMD);
        if (_dbg) {
            System.out.printf("write res=%d\n", hiVal);
        }
        Thread.sleep(20);

        // Read second byte and append with first 
        loVal = _read_byteNZ(fd, DISTANCE_REG_LO);
        if (_dbg) {
            System.out.printf(" Lo=%d\n", loVal);
        }

        // read first byte 
        hiVal = _read_byte(fd, DISTANCE_REG_HI);
        if (_dbg) {
            System.out.printf("Hi=%d ", hiVal);
        }

        return ((hiVal << 8) + loVal);
    }

    String lidar_status_code(int code) {
        String status = null;

        switch (code) {
            case (STAT_BUSY):
                status = "BUSY";
                break;
            case STAT_REF_OVER:
                status = "REFERENCE OVERFLOW";
                break;
            case STAT_SIG_OVER:
                status = "SIGNAL OVERFLOW";
                break;
            case STAT_PIN:
                status = "MODE SELECT PIN";
                break;
            case STAT_SECOND_PEAK:
                status = "SECOND PEAK";
                break;
            case STAT_TIME:
                status = "ACTIVE BETWEEN PAIRS";
                break;
            case STAT_INVALID:
                status = "NO SIGNAL";
                break;
            case STAT_EYE:
                status = "EYE SAFETY";
                break;
            default:
                status = String.valueOf(code);
                break;
        }
        return status;
    }

    int lidar_init(boolean dbg) {
        int fd = -1;
        _dbg = dbg;
        if (_dbg) {
            System.out.printf("LidarLite V0.1\n\n");
        }
        try {
            fd = I2C.wiringPiI2CSetup(LIDAR_ADDRESS);
            if (fd != -1) {
                lidar_status(fd);  // Dummy request to wake up device
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fd;
    }

    int lidar_status(int fd) {
        return I2C.wiringPiI2CReadReg8(fd, STATUS_REG);
    }

    void lidar_status_print(int status) {
        String statusmsg = null;
        if (status != 0) {
            statusmsg = String.format("STATUS BYTE: 0x%s; %s", Integer.toHexString(status), lidar_status_code(status));
            System.out.println(statusmsg);
        } else {
            //System.out.println(lidar_status_code(status));
        }

    }
    // Read a byte from I2C register.  Repeat if not ready

    int _read_byte(int fd, int reg) throws Exception {
        return _read_byte_raw(fd, reg, true);
    }

    // Read Lo byte from I2C register.  Repeat if not ready or zero
    int _read_byteNZ(int fd, int reg) throws Exception {
        return _read_byte_raw(fd, reg, false);
    }

    // Read byte from I2C register.  Special handling for zero value
    int _read_byte_raw(int fd, int reg, boolean allowZero) throws Exception {
        int i = 0;
        int val;
        Thread.sleep(1);
        while (true) {
            val = I2C.wiringPiI2CReadReg8(fd, reg);

            // Retry on error
            if (val == ERROR_READ || (val == 0 && !allowZero)) {
                Thread.sleep(20);		// ms
                // if (_dbg) printf(".");
                if (i++ > 50) {
                    // Timeout
                    System.out.printf("Timeout");
                    return ERROR_READ;
                }
            } else {
                return (val);
            }
        }
    }
}
