package myrobots;

import robocode.Robot;
import robocode.util.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.ScannedRobotEvent;
import robocode.RobotDeathEvent;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;


/**
 * Deliberativo - a robot by Grupo1
 * 
 */
public class Deliberativo extends Robot {
	private double safeDistance = 71.5;
	private int safeEnemies = 2;
	private double maxDistance;
	private int nEnemies, nq1, nq2, nq3, nq4, safeq;
	private Rectangle2D q1, q2, q3, q4; // Quadrant (q1-UL, q2-UR, q3-DL, q4-DR)
	private Point2D q1Point, q2Point, q3Point, q4Point; // Quadrant initial point
	private HashMap<String, EnemyInfo> enemies;
	
	int count = 0;
	double gunTurnAmt = 10;
	String trackName = null;
	
	public void run() {
		setBodyColor(Color.black);
		setGunColor(Color.black);
		setRadarColor(Color.blue);
		setBulletColor(Color.white);
		setScanColor(Color.white);
		
		turnRadarRight(360);
		
		maxDistance = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		nEnemies = nq1 = nq2 = nq3 = nq4 = safeq = 0;
		q1 = new Rectangle2D.Double(0, getBattleFieldHeight()/2, getBattleFieldWidth()/2, getBattleFieldHeight()/2);
		q2 = new Rectangle2D.Double(getBattleFieldWidth()/2, getBattleFieldHeight()/2, getBattleFieldWidth()/2, getBattleFieldHeight()/2);
		q3 = new Rectangle2D.Double(0, 0, getBattleFieldWidth()/2, getBattleFieldHeight()/2);
		q4 = new Rectangle2D.Double(getBattleFieldWidth()/2, 0, getBattleFieldWidth()/2, getBattleFieldHeight()/2);		
		q1Point = new Point2D.Double(q1.getMinX() + safeDistance, q1.getMaxY() - safeDistance);
		q2Point = new Point2D.Double(q2.getMaxX() - safeDistance, q2.getMaxY() - safeDistance);
		q3Point = new Point2D.Double(q3.getMinX() + safeDistance, q3.getMinY() + safeDistance);
		q4Point = new Point2D.Double(q4.getMaxX() - safeDistance, q4.getMinY() + safeDistance);
		
		enemies = new HashMap<String, EnemyInfo>();		

//		setAdjustGunForRobotTurn(true);
//		setAdjustRadarForGunTurn(true);
		
		while(true) {
			if(nEnemies == 1) {
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
			else {
				turnRadarRight(Double.POSITIVE_INFINITY);
			}
		}
	}
	
	/**
	 * onScannedRobot: Registar e atualizar toda a informacao dos inimigos para atacar
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// TODO: politica de controlo de disparo (energia e distancia)
		
		
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
			predictFire(enemy);
		}
		else{
			nEnemies++;
			enemy = new EnemyInfo(e.getName(), e.getEnergy(), e.getBearing(), e.getDistance(), e.getHeading(), e.getVelocity(), enemyCoords);
			enemies.put(e.getName(), enemy);
		}
		
		if(nEnemies == 1) {
			if (trackName != null && !e.getName().equals(trackName)) {
				return;
			}
			if (trackName == null) {
				trackName = e.getName();
			}
			count = 0;
			if (e.getDistance() > 150) {
				gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
				turnGunRight(gunTurnAmt);
				turnRight(e.getBearing());
				ahead(e.getDistance() - 140);
				return;
			}

			gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
			turnGunRight(gunTurnAmt);
			predictFire(enemy);

			if (e.getDistance() < 100) {
				if (e.getBearing() > -90 && e.getBearing() <= 90) {
					back(40);
				} else {
					ahead(40);
				}
			}
			
			scan();
		}
		else if(nEnemies > safeEnemies) {
			predictFire(enemy);
			
			// Find where the enemy is located (quadrant)
			nq1 = nq2 = nq3 = nq4 = safeq = 0;
			for(Map.Entry<String, EnemyInfo> map : enemies.entrySet()) {
				if(q1.contains(map.getValue().getCoords())) {
					nq1++;
				}
				else if(q2.contains(map.getValue().getCoords())) {
					nq2++;
				}
				else if(q3.contains(map.getValue().getCoords())) {
					nq3++;
				}
				else if(q4.contains(map.getValue().getCoords())) {
					nq4++;
				}
			}
			
			// Recalculate the safest quadrant
			int alt = 0;
			if(nq1 + nq2 <= nq3 + nq4) {
				if(nq1 + nq3 <= nq2 + nq4) {
					safeq = 1;
					alt = 1;
				}
				else {
					safeq = 2;
					alt = 1;
				}
			}
			else {
				if(nq1 + nq3 <= nq2 + nq4) {
					safeq = 3;
					alt = 1;
				}
				else {
					safeq = 4;
					alt = 1;
				}
			}
			
			// Move to the safest quadrant
			if(alt == 1) {
				if(safeq == 1) {
					moveTo(q1Point);
				}
				else if(safeq == 2) {
					moveTo(q2Point);
				}
				else if(safeq == 3) {
					moveTo(q3Point);
				}
				else if(safeq == 4) {
					moveTo(q4Point);
				}
				alt = 0;
			}
		}
		else if(nEnemies <= safeEnemies) {
			predictFire(enemy);	
			
			// Find where the enemy is located (quadrant)
			nq1 = nq2 = nq3 = nq4 = safeq = 0;
			for(Map.Entry<String, EnemyInfo> map : enemies.entrySet()) {
				if(q1.contains(map.getValue().getCoords())) {
					nq1++;
				}
				else if(q2.contains(map.getValue().getCoords())) {
					nq2++;
				}
				else if(q3.contains(map.getValue().getCoords())) {
					nq3++;
				}
				else if(q4.contains(map.getValue().getCoords())) {
					nq4++;
				}
			}
			
			// Recalculate the most populated quadrant
			int alt = 0;
			if(nq1 + nq2 >= nq3 + nq4) {
				if(nq1 + nq3 >= nq2 + nq4) {
					safeq = 1;
					alt = 1;
				}
				else {
					safeq = 2;
					alt = 1;
				}
			}
			else {
				if(nq1 + nq3 >= nq2 + nq4) {
					safeq = 3;
					alt = 1;
				}
				else {
					safeq = 4;
					alt = 1;
				}
			}
			
			// Move to the safest quadrant
			if(alt == 1) {
				if(safeq == 1) {
					moveTo(q1Point);
				}
				else if(safeq == 2) {
					moveTo(q2Point);
				}
				else if(safeq == 3) {
					moveTo(q3Point);
				}
				else if(safeq == 4) {
					moveTo(q4Point);
				}
				alt = 0;
			}
		}
	}
	
	/**
	 * onRobotDeath: Registar morte dos inimigos
	 */
	public void onRobotDeath(RobotDeathEvent e) {
		nEnemies--;
		if(enemies.containsKey(e.getName())) {
			EnemyInfo enemy = enemies.get(e.getName());
			Point2D enemyCoords = enemy.getCoords();
			
			if(q1.contains(enemyCoords)) {
				nq1--;
			}
			else if(q2.contains(enemyCoords)) {
				nq2--;
			}
			else if(q3.contains(enemyCoords)) {
				nq3--;
			}
			else if(q4.contains(enemyCoords)) {
				nq4--;
			}
			
			enemyCoords.setLocation(-1, -1);
			enemy.setCoords(enemyCoords);
			enemies.put(e.getName(), enemy);			
		}
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
