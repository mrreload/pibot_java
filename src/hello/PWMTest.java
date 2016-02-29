/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello;

import java.io.IOException;

//import se.hirt.pi.adafruit.pwm.PWMDevice;
//import se.hirt.pi.adafruit.pwm.PWMDevice.PWMChannel;
/**
 *
 * @author mrreload
 */
public class PWMTest {
	// The internetz says 50Hz is the standard PWM frequency for operating RC servos.  
	private final static int SERVO_FREQUENCY = 50;
	// Literature on RC servos says a 1ms pulse is minimum, 1,5ms is centered and 2ms is max. 
	private final static int SERVO_MIN = calculatePulseWidth(.5, SERVO_FREQUENCY);
	private final static int SERVO_CENTERED = calculatePulseWidth(1.5, SERVO_FREQUENCY);
	private final static int SERVO_MAX = calculatePulseWidth(2, SERVO_FREQUENCY);

	private final static int MOTOR_MIN = 0;
	private final static int MOTOR_MEDIUM = 2048;
	private final static int MOTOR_MAX = 4095;


	public static void main(String[] args) throws IOException,
			InterruptedException {
		System.out.println("Creating device...");
		PWMDevice device = new PWMDevice();
		device.setPWMFreqency(SERVO_FREQUENCY);
		PWMDevice.PWMChannel servo0 = device.getChannel(0);
		PWMDevice.PWMChannel servo1 = device.getChannel(1);
		//PWMChannel motor0 = device.getChannel(2);
		//PWMChannel motor1 = device.getChannel(3);
		

		System.out.println("Setting start conditions...");
		servo0.setPWM(0, SERVO_CENTERED);
		servo1.setPWM(0, SERVO_CENTERED);
		//motor0.setPWM(0, MOTOR_MIN);
		//motor1.setPWM(0, MOTOR_MIN);

		System.out.println("Running perpetual loop...");
		while (true) {
			//servo0.setPWM(0, SERVO_MIN);
			servo1.setPWM(0, SERVO_MIN);
			//motor0.setPWM(0, MOTOR_MEDIUM);
			//motor1.setPWM(0, MOTOR_MEDIUM);
			Thread.sleep(1500);
			//servo0.setPWM(0, SERVO_MAX);
			servo1.setPWM(0, SERVO_MAX);
			//motor0.setPWM(0, MOTOR_MAX);
			//motor1.setPWM(0, MOTOR_MAX);
			Thread.sleep(1500);
			//servo0.setPWM(0, SERVO_CENTERED);
			servo1.setPWM(0, SERVO_CENTERED);
			//motor0.setPWM(0, MOTOR_MIN);
			//motor1.setPWM(0, MOTOR_MIN);
			Thread.sleep(1500);
		}
	}


	private static int calculatePulseWidth(double millis, int frequency) {
		return (int) (Math.round(4096 * millis * frequency/1000));
	}

}
