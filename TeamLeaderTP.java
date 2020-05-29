package myrobots;

import robocode.TeamRobot;
import robocode.util.*;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;

import java.awt.*;
import java.awt.geom.*;

import java.util.HashMap;
import java.util.Map;


import java.io.IOException;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * TeamLeaderTP - a robot by Grupo1
 * 
 */
public class TeamLeaderTP extends TeamRobot {
	private String[] team;
	private HashMap<String, RobotInfo> teammates;
	private HashMap<String, RobotInfo> enemies;
	private HashMap<String, Point2D.Double> boxes;

	private double maxDistance;
	private double distanceWall = 61.5; // 36 + 23.5
	private int nTeammates, nEnemies, nBoxes, nq1, nq2, nq3, nq4, safeq;
	private Point2D.Double p1Box, p2Box, p3Box;
	
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

	private boolean initScan = false;
	
	int count = 0;
	double gunTurnAmt = 10;
	String trackName = null;
	
	public void run() {
		setBodyColor(Color.orange);
		setGunColor(Color.orange);
		setRadarColor(Color.orange);
		setScanColor(Color.black);
		setBulletColor(Color.orange);
		
		sendColors();
		
		team = getTeammates();
		sendDistance(team[0], distanceWall * 2);
		sendDistance(team[1], distanceWall * 3);
		
		teammates = new HashMap<String, RobotInfo>();
		enemies = new HashMap<String, RobotInfo>();
		boxes = new HashMap<String, Point2D.Double>();

		maxDistance = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		nTeammates = nEnemies = nBoxes = nq1 = nq2 = nq3 = nq4 = safeq = 0;

		p1Box = new Point2D.Double();
		p2Box = new Point2D.Double();
		p3Box = new Point2D.Double();		

		
		
		placeWall();
		
//		turnRadarRight(360);
//		initScan = true;
		
		while (true) {
			if(nTeammates != 0) {
				setTurnRadarRight(Double.POSITIVE_INFINITY);
				moveWall();
			}
			else {
				turnGunRight(gunTurnAmt);
				count++;
				if (count > 2) {
					gunTurnAmt = -10;
				}
				if (count > 5) {
					gunTurnAmt = 10;
				}
				if (count > 11) {
					trackName = null;
				}
			}
		}
	}
	

	/**
	 * onScannedRobot: 
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		Point2D.Double robotCoords = calcCoords(e.getBearing(), e.getDistance());
		
		if(isTeammate(e.getName())) {
			if(teammates.containsKey(e.getName())) {
				RobotInfo robot = teammates.get(e.getName());
				robot.setEnergy(e.getEnergy());
				robot.setBearing(e.getBearing());
				robot.setDistance(e.getDistance());
				robot.setHeading(e.getHeading());
				robot.setVelocity(e.getVelocity());
				robot.setCoords(robotCoords);
				teammates.put(e.getName(), robot);
			}
			else{
				nTeammates++;
				RobotInfo robot = new RobotInfo(e.getName(), e.getEnergy(), e.getBearing(), e.getDistance(), e.getHeading(), e.getVelocity(), robotCoords);
				teammates.put(e.getName(), robot);
			}
			
		}
		else if(e.getName().contains("BoxTP")) {
			if(!boxes.containsKey(e.getName()) && e.getVelocity() == 0) {
				nBoxes++;
				boxes.put(e.getName(), robotCoords);
				
				
				if(currentWall == TWALL) {
					p1Box = new Point2D.Double(robotCoords.getX()-50, robotCoords.getY()+50);
					p2Box = new Point2D.Double(robotCoords.getX()+50, robotCoords.getY()+50);
					p3Box = new Point2D.Double(robotCoords.getX()+50, robotCoords.getY());
				}
				else if(currentWall == RWALL) {
					p1Box = new Point2D.Double(robotCoords.getX()+50, robotCoords.getY()+50);
					p2Box = new Point2D.Double(robotCoords.getX()+50, robotCoords.getY()-50);
					p3Box = new Point2D.Double(robotCoords.getX(), robotCoords.getY()-50);
				}
				else if(currentWall == DWALL) {
					p1Box = new Point2D.Double(robotCoords.getX()+50, robotCoords.getY()-50);
					p2Box = new Point2D.Double(robotCoords.getX()-50, robotCoords.getY()-50);
					p3Box = new Point2D.Double(robotCoords.getX()-50, robotCoords.getY());
				}
				else if(currentWall == LWALL) {
					p1Box = new Point2D.Double(robotCoords.getX()-50, robotCoords.getY()-50);
					p2Box = new Point2D.Double(robotCoords.getX()-50, robotCoords.getY()+50);
					p3Box = new Point2D.Double(robotCoords.getX(), robotCoords.getY()+50);
				}
			}
			
			if(nEnemies == 0) {
				sendAtack(robotCoords);
			}
		}
		else {
			if(nTeammates != 0) {
				if(enemies.containsKey(e.getName())) {
					RobotInfo robot = enemies.get(e.getName());
					robot.setEnergy(e.getEnergy());
					robot.setBearing(e.getBearing());
					robot.setDistance(e.getDistance());
					robot.setHeading(e.getHeading());
					robot.setVelocity(e.getVelocity());
					robot.setCoords(robotCoords);
					enemies.put(e.getName(), robot);
				}
				else{
					nEnemies++;
					RobotInfo robot = new RobotInfo(e.getName(), e.getEnergy(), e.getBearing(), e.getDistance(), e.getHeading(), e.getVelocity(), robotCoords);
					enemies.put(e.getName(), robot);
				}
				
				sendAtack(robotCoords);
			}
			else {
				RobotInfo robot = enemies.get(e.getName());
//				predictFire(robot);
				
				if (trackName != null && !e.getName().equals(trackName)) {
					return;
				}
				if (trackName == null) {
					trackName = e.getName();
				}
				count = 0;
				if (e.getDistance() > 150) {
					gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
					setTurnGunRight(gunTurnAmt);
					turnRight(e.getBearing());
					ahead(e.getDistance() - 140);
					return;
				}

				gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
				turnGunRight(gunTurnAmt);
				fire(3.0);

				if (e.getDistance() < 100) {
					if (e.getBearing() > -90 && e.getBearing() <= 90) {
						back(40);
					} else {
						ahead(40);
					}
				}
				
				scan();
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
		else if(e.getName().contains("BoxTP")) {
			back(23.5);
			moveTo(p1Box);
			moveTo(p2Box);
			moveTo(p3Box);
			turnLeft(90);
//			moveWall();
		}
	}
	
	/**
	 * onRobotDeath: 
	 */
	public void onRobotDeath(RobotDeathEvent e) {
		if(isTeammate(e.getName())) {
			nTeammates--;
			if(teammates.containsKey(e.getName())) {
				RobotInfo robot = teammates.get(e.getName());
				Point2D.Double robotCoords = robot.getCoords();
				
				robotCoords.setLocation(-1, -1);
				robot.setCoords(robotCoords);
				teammates.put(e.getName(), robot);			
			}
			
			if(nTeammates == 0) {
				trackName = null; // Initialize to not tracking anyone
				setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
				gunTurnAmt = 10; // Initialize gunTurn to 10
			}
		}
		else if(e.getName().contains("BoxTP")) {
			nBoxes--;
			if(boxes.containsKey(e.getName())) {
				Point2D.Double robotCoords = boxes.get(e.getName());
				
				robotCoords.setLocation(-1, -1);
				boxes.put(e.getName(), robotCoords);			
			}
		}
		else {
			nEnemies--;
			if(enemies.containsKey(e.getName())) {
				RobotInfo robot = enemies.get(e.getName());
				Point2D.Double robotCoords = robot.getCoords();
				
				robotCoords.setLocation(-1, -1);
				robot.setCoords(robotCoords);
				enemies.put(e.getName(), robot);			
			}
		}
	}
	
	/**
	 * Send colors to Droids
	 */
	public void sendColors() {
		RobotColors c = new RobotColors();
		c.bodyColor = Color.green;
		c.gunColor = Color.green;
		c.radarColor = Color.green;
		c.scanColor = Color.black;
		c.bulletColor = Color.green;
	
		try {
			broadcastMessage(c);
		} catch(IOException ex) {}
	}
	
	/**
	 * Send atack to Droids
	 */
	public void sendAtack(Point2D.Double coords) {
		try {
			broadcastMessage("atack_" + coords.getX() + "_" + coords.getY());
		} catch (IOException ex) {}
	}
	
	/**
	 * Send move to Droids
	 */
	public void sendMove(String droid, Point2D.Double coords) {
		try {
			sendMessage(droid, "move_" + coords.getX() + "_" + coords.getY());
		} catch (IOException ex) {}
	}
	
	/**
	 * Send safe distance to Droids
	 */
	public void sendDistance(String droid, double distance) {
		try {
			sendMessage(droid, "distance_" + distance);
		} catch (IOException ex) {}
	}
	
	/**
	 * Calculate coordinates
	 */
	public Point2D.Double calcCoords(double enemyBearing, double enemyDistance) {
        double angle = Math.toRadians(getHeading() + (enemyBearing % 360));
        double enemyX = Math.round(getX() + Math.sin(angle) * enemyDistance);
        double enemyY = Math.round(getY() + Math.cos(angle) * enemyDistance);
		Point2D.Double enemyCoords = new Point2D.Double(enemyX, enemyY);		
		return enemyCoords;
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
		double destX = 0;
		double destY = 0;
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
	 * predictFire: Prever e calcular intensidade do tiro
	 */
	public void predictFire(RobotInfo enemy) {
		double bulletPower = 3.0;
		
		if(enemy.getDistance() > maxDistance) {
			if(enemy.getDistance() > (maxDistance * 0.75)) {
				bulletPower = bulletPower * 0.75;
			}
			else {
				bulletPower = bulletPower * 0.5;
			}
		}
		
		bulletPower = Math.min(bulletPower, enemy.getEnergy());
		
		double absoluteBearing =  Math.toRadians(getHeading()) + Math.toRadians(enemy.getBearing());
		double enemyX = getX() + enemy.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + enemy.getDistance() * Math.cos(absoluteBearing);
		double enemyHeading = Math.toRadians(enemy.getHeading());
		double enemyVelocity = enemy.getVelocity();
		
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight();
		double battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX;
		double predictedY = enemyY;
		
		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance(getX(), getY(), predictedX, predictedY)) {		
			predictedX += Math.sin(enemyHeading) * enemyVelocity;	
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			if(	predictedX < 18.0 
				|| predictedY < 18.0
				|| predictedX > battleFieldWidth - 18.0
				|| predictedY > battleFieldHeight - 18.0) {
				predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
				break;
			}
		}
		
		double theta = Utils.normalAbsoluteAngle(Math.atan2( predictedX - getX(), predictedY - getY()));
		 
		turnRadarRight(Math.toDegrees(Utils.normalRelativeAngle(absoluteBearing - Math.toRadians(getRadarHeading()))));
		turnGunRight(Math.toDegrees(Utils.normalRelativeAngle(theta - Math.toRadians(getGunHeading()))));
		fire(bulletPower);
	}
	
	/**
	 * calcFire: 
	 */
	public double calcFire(double energy) {
		double bulletPower = 3.0;		
		return bulletPower;
	}
	
	/**
	 * RobotInfo - Class to store all the robots information
	 * 
	 */
	public class RobotInfo {
		private String name;
		private double energy;
		private double bearing;
		private double distance;
		private double heading;
		private double velocity;
		private Point2D.Double coords;
		
		public RobotInfo(String name, double energy, double bearing, double distance, double heading, double velocity, Point2D.Double coords) {
			this.name = name;
			this.energy = energy;
			this.bearing = bearing;
			this.distance = distance;
			this.heading = heading;
			this.velocity = velocity;
			this.coords = coords;
		}
		
		public String getName() {
			return name;
		}
		public double getEnergy() {
			return energy;
		}
		public double getBearing() {
			return bearing;
		}
		public double getDistance() {
			return distance;
		}
		public double getHeading() {
			return heading;
		}
		public double getVelocity() {
			return velocity;
		}
		public Point2D.Double getCoords() {
			return coords;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		public void setEnergy(double energy) {
			this.energy = energy;
		}
		public void setBearing(double bearing) {
			this.bearing = bearing;
		}
		public void setDistance(double distance) {
			this.distance = distance;
		}
		public void setHeading(double heading) {
			this.heading = heading;
		}
		public void setVelocity(double velocity) {
			this.velocity = velocity;
		}
		public void setCoords(Point2D.Double coords) {
			this.coords = coords;
		}
	}
}
