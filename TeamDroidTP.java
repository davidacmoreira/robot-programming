package myrobots;

import robocode.TeamRobot;
import robocode.Droid;
import robocode.MessageEvent;
import robocode.HitRobotEvent;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.geom.*;


/**
 * TeamDroidTP - a robot by Grupo1
 * 
 */
public class TeamDroidTP extends TeamRobot implements Droid {
	
	private double distanceWall;
	
	private final int TLCORNER = 1;
	private final int TRCORNER = 2;
	private final int DRCORNER = 3;
	private final int DLCORNER = 4;
	private final int TWALL = 5;
	private final int RWALL = 6;
	private final int DWALL = 7;
	private final int LWALL = 8;
	
	private int currentCorner;
	private int currentWall;
	
	public void run() {
		setAdjustGunForRobotTurn(true);
		
		placeWall();
		
		while(true) {
			moveWall();
		}
	}

	/**
	 * onMessageReceived: 
	 */
	public void onMessageReceived(MessageEvent e) {
		if(e.getMessage() instanceof RobotColors) {
			RobotColors c = (RobotColors) e.getMessage();

			setBodyColor(c.bodyColor);
			setGunColor(c.gunColor);
			setRadarColor(c.radarColor);
			setScanColor(c.scanColor);
			setBulletColor(c.bulletColor);
		}
		else if(e.getMessage() instanceof String) {
			String[] message = ((String) e.getMessage()).split("_");
			if(message[0].equals("atack")) {
				double x = Double.parseDouble(message[1]) - getX();
				double y = Double.parseDouble(message[2]) - getY();
				double theta = Math.toDegrees(Math.atan2(x, y));
				turnGunRight(normalRelativeAngleDegrees(theta - getGunHeading()));
				fire(3);
			}
			else if(message[0].equals("distance")) {
				distanceWall = Double.parseDouble(message[1]);
				
			}
			else if(message[0].equals("move")) {
				double x = Double.parseDouble(message[1]);
				double y = Double.parseDouble(message[2]);
				Point2D.Double coords = new Point2D.Double(x, y);
				moveTo(coords);
			}
		}
	}
	
	/**
	 * onHitRobot: 
	 */
	public void onHitRobot(HitRobotEvent e) {
		if(!isTeammate(e.getName()) && !e.getName().contains("BoxTP")) {
			turnGunRight(getHeading() - getGunHeading() + e.getBearing());
			double energy = getEnergy();
			double bulletPower = calcFire(energy);
			fire(bulletPower);
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
	
	/**
	 *  Move wall
	 */
	public void moveWall() {
		double distance = 0;
		
		if(currentWall == TWALL)
			distance = getBattleFieldWidth() - getX();
		else if(currentWall == RWALL)
			distance = getY();
		else if(currentWall == DWALL)
			distance = getX();
		else if(currentWall == LWALL)
			distance = getBattleFieldHeight() - getY();
		
		currentWall++;
		if(currentWall > 8)
			currentWall = 5;
		
		ahead(distance - distanceWall);
		turnRight(90);
	}
	
	/**
	 * Move robot to a position
	 */
	public void moveTo(Point2D.Double coords) {
		double bearing = Math.PI/2 - Math.atan2(coords.getY() - getY(), coords.getX() - getX());
		bearing = bearing - Math.toRadians(getHeading());
		while (bearing <= -Math.PI) bearing += 2 * Math.PI;
		while (Math.PI < bearing) bearing -= 2 * Math.PI;
		bearing = Math.toDegrees(bearing);
		turnRight(bearing);
		double distance = Math.sqrt(Math.pow((getX() - coords.getX()), 2) + Math.pow((getY() - coords.getY()), 2));
		ahead(distance);
	}
	
	/**
	 * calcFire: 
	 */
	public double calcFire(double energy) {
		double bulletPower = 3.0;
		
		return bulletPower;
	}
}
