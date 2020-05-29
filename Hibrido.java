package myrobots;

import robocode.Robot;
import robocode.util.*;
import robocode.ScannedRobotEvent;
import robocode.RobotDeathEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.HitByBulletEvent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;


/**
 * Hibrido - a robot by Grupo1
 * 
 */
public class Hibrido extends Robot {
	private int safeEnemies = 2;
	private double maxDistance;
	private int rotate, nEnemies;
	private HashMap<String, EnemyInfo> enemies;
	
	public void run() {
		setBodyColor(Color.black);
		setGunColor(Color.black);
		setRadarColor(Color.red);
		setBulletColor(Color.white);
		setScanColor(Color.white);
		
//		setAdjustGunForRobotTurn(true);
//		setAdjustRadarForGunTurn(true);
		
		maxDistance = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		rotate = nEnemies = 0;
		enemies = new HashMap<String, EnemyInfo>();		
		
		turnRadarRight(360);
		placeWall();
		
		while(true) {
			turnRadarRight(Double.POSITIVE_INFINITY);
		}
	}
	
	/**
	 * onScannedRobot: Registar e atualizar toda a informacao dos inimigos para atacar
	 */
	public void onScannedRobot(ScannedRobotEvent e) {		
		Point2D enemyCoords = calcCoords(e.getBearing(), e.getDistance());
		EnemyInfo enemy;
		
		if(enemies.containsKey(e.getName())) {
			enemy = enemies.get(e.getName());
			enemy.setEnergy(e.getEnergy());
			enemy.setBearing(e.getBearing());
			enemy.setDistance(e.getDistance());
			enemy.setHeading(e.getHeading());
			enemy.setVelocity(e.getVelocity());
			enemy.setCoords(enemyCoords);
			enemies.put(e.getName(), enemy);
		}
		else{
			nEnemies++;
			enemy = new EnemyInfo(e.getName(), e.getEnergy(), e.getBearing(), e.getDistance(), e.getHeading(), e.getVelocity(), enemyCoords);
			enemies.put(e.getName(), enemy);
		}
		
		predictFire(enemy);	
	}
	
	/**
	 * onRobotDeath: Registar morte dos inimigos
	 */
	public void onRobotDeath(RobotDeathEvent e) {
		nEnemies--;
	}
	
	/**
	 * onHitRobot: Mover o canhao para o inimigo e disparar
	 */
	public void onHitRobot(HitRobotEvent e) {
		turnGunRight(getHeading() - getGunHeading() + e.getBearing());
		fire(3.0);
	}
	
	/**
	 * onHitWall: Mudar a trajetória
	 */
	public void onHitWall(HitWallEvent e) {
		if(rotate == 1)
			turnRight(90);
		else
			turnLeft(90);
	}
	
	/**
	 * onHitByBullet: Desviar
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		ahead(50);
	}
	
	/**
	 * calcCoords: Calcular coordenadas
	 */
	public Point2D.Double calcCoords(double enemyBearing, double enemyDistance) {
        double angle = Math.toRadians(getHeading() + (enemyBearing % 360));
        double enemyX = Math.round(getX() + Math.sin(angle) * enemyDistance);
        double enemyY = Math.round(getY() + Math.cos(angle) * enemyDistance);
		Point2D.Double enemyCoords = new Point2D.Double(enemyX, enemyY);		
		return enemyCoords;
	}
	
	/**
	 * moveTo: Mover robo para uma posicao
	 */
	public void moveTo(Point2D coords) {
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
	 * predictFire: Prever e calcular intensidade do tiro
	 */
	public void predictFire(EnemyInfo enemy) {
		double bulletPower = 3.0;
		
		if(nEnemies > safeEnemies) {
			if(enemy.getDistance() > maxDistance) {
				if(enemy.getDistance() > (maxDistance * 0.75)) {
					bulletPower = bulletPower * 0.75;
				}
				else {
					bulletPower = bulletPower * 0.5;
				}
				
				if(getEnergy() < 50) {
					if(getEnergy() < 20) {
						bulletPower = bulletPower * 0.5;
					}
					else {
						bulletPower = bulletPower * 0.8;
					}
				}
			}
			
			bulletPower = Math.min(bulletPower, enemy.getEnergy());
		}
		
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
	 * Place wall
	 */
	void placeWall() {
		double x = getX();
		double y = getY();
		double diffX;
		double diffY;
		int q; // Quadrant (1-UL, 2-UR, 3-DL, 4-DR)
		int w; // Wall (1-U, 2-R, 3-D, 4-L)
		
		if(x > (getBattleFieldWidth() / 2)) {
			if(y > (getBattleFieldHeight() / 2)) {
				q = 2;
				diffX = getBattleFieldWidth() - x;
				diffY = getBattleFieldHeight() - y;
			}
			else {
				q = 4;
				diffX = getBattleFieldWidth() - x;
				diffY = y;
			}
		}
		else {
			if(y > (getBattleFieldHeight() / 2)) {
				q = 1;
				diffX = x;
				diffY = getBattleFieldHeight() - y;
			}
			else {
				q = 3;
				diffX = x;
				diffY = y;
			}
		}
		
		double angleRadians;

		if(diffX < diffY) {
			if(q == 2 || q == 4) {
				w = 2;
				angleRadians = Math.atan2(y - y, getBattleFieldWidth() - x);
			}
			else {
				w = 4;
				angleRadians = Math.atan2(y - y, 0 - x);
			}
		}
		else {
			if(q == 1 || q == 2) {
				w = 1;
				angleRadians = Math.atan2(getBattleFieldHeight() - y, x - x);
			}
			else {
				w = 3;
				angleRadians = Math.atan2(0 - y, x - x);
			}
		}
		
		double angleDegrees = Math.toDegrees(angleRadians);
		double currentHeading = getHeading();
		
		if(currentHeading == 180)
			turnRight(90-(angleDegrees - currentHeading));
		else if(currentHeading == 270)
			turnRight(90);
		else
			turnRight(90-angleDegrees - currentHeading);
		
		ahead(maxDistance);
		
		if((w == 1 && q == 2) || (w == 2 && q == 4) || (w == 3 && q == 3) || (w == 4 && q == 1)) {
			rotate = 1;
//			turnRight(90);
//			ahead(maxDistance);
		}
		else {
			rotate = 2;
//			turnLeft(90);
//			ahead(maxDistance);
		}
	}
	
	/**
	 * EnemyInfo - Classe para guardar informacoes do inimigo
	 * 
	 */
	public class EnemyInfo {
		private String name;
		private double energy;
		private double bearing;
		private double distance;
		private double heading;
		private double velocity;
		private Point2D coords;
		
		public EnemyInfo(String name, double energy, double bearing, double distance, double heading, double velocity, Point2D coords) {
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
		public Point2D getCoords() {
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
		public void setCoords(Point2D coords) {
			this.coords = coords;
		}
	}
}
