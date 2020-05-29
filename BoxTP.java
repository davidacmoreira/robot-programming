package myrobots;

import robocode.Robot;
import robocode.util.*;

import java.awt.*;
import java.awt.geom.*;


/**
 * BoxTP - a robot by Grupo1
 * 
 */
public class BoxTP extends Robot {
	private final int TLCORNER = 1;
	private final int TRCORNER = 2;
	private final int DRCORNER = 3;
	private final int DLCORNER = 4;
	private final int TWALL = 5;
	private final int RWALL = 6;
	private final int DWALL = 7;
	private final int LWALL = 8;
	
	private double distanceWall = 70;
	private int currentCorner;
	private int currentWall;
	
	
	public void run() {
		setBodyColor(Color.white);
		setGunColor(Color.white);
		setRadarColor(Color.white);
		setScanColor(Color.white);
		setBulletColor(Color.white);
		
		
		placeWall();
		
		while (true) {
			turnRadarRight(Double.POSITIVE_INFINITY);
		}
	}
	

	
	
	/**
	 *  Place wall
	 */
	public void placeWall() {
		double posX = getX();
		double posY = getY();
		double diffX;
		double diffY;
		
		if(posX > (getBattleFieldWidth() / 2)) {
			if(posY > (getBattleFieldHeight() / 2)) {
				currentCorner = TRCORNER;
				diffX = getBattleFieldWidth() - posX;
				diffY = getBattleFieldHeight() - posY;
			}
			else {
				currentCorner = DRCORNER;
				diffX = getBattleFieldWidth() - posX;
				diffY = posY;
			}
		}
		else {
			if(posY > (getBattleFieldHeight() / 2)) {
				currentCorner = TLCORNER;
				diffX = posX;
				diffY = getBattleFieldHeight() - posY;
			}
			else {
				currentCorner = DLCORNER;
				diffX = posX;
				diffY = posY;
			}
		}
		
		double angleRadians;
		double distance;
		
		if(diffX < diffY) {
			if(currentCorner == TRCORNER || currentCorner == DRCORNER) {
				currentWall = RWALL;
				angleRadians = Math.atan2(0, getBattleFieldWidth() - posX);
			}
			else {
				currentWall = LWALL;
				angleRadians = Math.atan2(0, 0 - posX);
			}
			
			distance = diffX;
		}
		else {
			if(currentCorner == TRCORNER || currentCorner == TLCORNER) {
				currentWall = TWALL;
				angleRadians = Math.atan2(getBattleFieldHeight() - posY, 0);
			}
			else {
				currentWall = DWALL;
				angleRadians = Math.atan2(0 - posY, 0);
			}
			
			distance = diffY;
		}
		
		double angleDegrees = Math.toDegrees(angleRadians);
		double currentHeading = getHeading();
		
		if(currentHeading == 180)
			turnRight(90 - (angleDegrees - currentHeading));
		else if(currentHeading == 270)
			turnRight(90);
		else
			turnRight(90 - angleDegrees - currentHeading);
		
		ahead(distance - distanceWall);
		turnRight(90);
	}
}
