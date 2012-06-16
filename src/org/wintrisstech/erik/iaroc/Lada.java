package org.wintrisstech.erik.iaroc;

import android.os.SystemClock;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;
import org.wintrisstech.sensors.UltraSonicSensors;

/**
 * A Lada is an implementation of the IRobotCreateInterface, inspired by Vic's
 * awesome API. It is entirely event driven.
 *
 * @author Erik
 */
public class Lada extends IRobotCreateAdapter
{
    private static final String TAG = "Lada";
    private final Dashboard dashboard;
    public UltraSonicSensors sonar;
    private int oldDistance = 0;
    private int newDistance = 0;
    private int deltaDistance = 0;
    private int speed = 50;
    private int distance = 0;
    private int heading = 0;
    private boolean leftCorner = false;
    private int leftCornerDistance = 0;
    private int howFarHaveWeGone = 0;
    private int howFarHaveWeTurned = 0;
    private int deltaTurn;

    public Lada(IOIO ioio, IRobotCreateInterface create, Dashboard dashboard) throws ConnectionLostException
    {
        super(create);
        sonar = new UltraSonicSensors(ioio);
        this.dashboard = dashboard;
        song(0, new int[]
                {
                    58, 10
                });
    }

    public void initialize() throws ConnectionLostException
    {
        dashboard.log("===========Start RoboCamp===========");
        dashboard.log("Battery Charge = " + getBatteryCharge() + ", 3,000 = Full charge");
        readSensors(SENSORS_GROUP_ID6);
    }

    public void loop() throws ConnectionLostException
    {
        //mANDm();
        lukeAndJenny();
    }

    public void lukeAndJenny()
    {
        dashboard.speak("hello luke. hello joey. hello jenny. what would you like me to draw?(Version 4)");
        goFoward170();
        turn90();
    }

    public void goFoward170()
    {
        dashboard.log("starting go forward");
        try
        {
            readSensors(SENSORS_GROUP_ID6);
            driveDirect(100, 100);
            while (true)
            {
                readSensors(SENSORS_GROUP_ID6);
                deltaDistance = getDistance();
                howFarHaveWeGone = deltaDistance + howFarHaveWeGone;
                dashboard.log(deltaDistance + "/" + howFarHaveWeGone);
                if (howFarHaveWeGone >= 170)
                {
                    driveDirect(0, 0);
                    howFarHaveWeGone = 0;
                    dashboard.log("bye  forwardleaving luke jenny and joey from g");
                    break;
                }
            }
        } catch (ConnectionLostException ex)
        {
            dashboard.log("hiccup");
        }
    }

    public void turn90()
    {
        dashboard.log("starting turn 90");
        try
        {
            readSensors(SENSORS_GROUP_ID6);
            driveDirect(100, 0);
            while (true)
            {
                readSensors(SENSORS_GROUP_ID6);
                deltaTurn = getAngle();
                howFarHaveWeTurned = deltaTurn + howFarHaveWeTurned;
                dashboard.log(deltaTurn + "/" + howFarHaveWeTurned);
                SystemClock.sleep(100);
                if (howFarHaveWeTurned >= 90)
                {
                    driveDirect(0, 0);
                    howFarHaveWeTurned = 0;
                    dashboard.log("here");
                    break;
                }
            }
        } catch (ConnectionLostException ex)
        {
            dashboard.log("hiccup");
        }
    }

    public void mANDm()
    {
        SystemClock.sleep(100);
        try
        {
            readSensors(SENSORS_GROUP_ID6);
            distance += getDistance();
            heading += getAngle();
            sonar.readUltrasonicSensors();
            oldDistance = newDistance;
            newDistance = sonar.getLeftDistance();
            deltaDistance = newDistance - oldDistance;
            if (deltaDistance < -50 || leftCorner)
            {
                leftCorner = true;
                dashboard.log("detected left corner");
                driveDirect(speed, speed);
                leftCornerDistance = distance;
                if ((distance - leftCornerDistance) > 400)
                {
                    leftCorner = false;
                }
                driveDirect(speed, 0);
                SystemClock.sleep(5000);
            } else if (deltaDistance < 0)//getting closer...turn right
            {
                driveDirect((int) (.9 * speed), speed);
                dashboard.log(deltaDistance + " getting closer...turning ight");
            }

            if (deltaDistance > 0)//getting further away...turn left
            {
                dashboard.log(deltaDistance + " getting further away...turning left");
                driveDirect(speed, (int) (.9 * speed));
            }



        } catch (Exception ex)
        {
        }
    }
}
