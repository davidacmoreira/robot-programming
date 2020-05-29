package myrobots;

import robocode.Robot;
import robocode.util.*;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.HitByBulletEvent;
import robocode.BulletHitEvent;
import robocode.WinEvent;
import robocode.DeathEvent;
import robocode.BulletMissedEvent;

import java.awt.*;
import java.awt.geom.*;


/**
 * Emotivo - a robot by Grupo1
 * 
 */
public class Emotivo extends Robot {
	private static boolean win, death;
	private int wall = 0;
	private int misses = 0;
	private int hits = 0;
	private int place = 0;
	private int rotate = 0;
	private double maxDistance;
	
	public void run() {
		color();
		maxDistance = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		
		while(true) {
			turnGunRight(360);
		}
	}
	
	/**
	 * onScannedRobot: Disparar contra o inimigo
	 */
	public void onScannedRobot(ScannedRobotEvent e) {		
		double energy = getEnergy();
		double bulletPower = calcFire(energy);
		if(misses < 5) {
			fire(bulletPower);
		}
		else {
			if(misses % 2 == 0) {
				fire(bulletPower);
			}
			else {
				misses++;
			}
		}
	}
	
	/**
	 * onHitRobot: Mover o canhao para o inimigo, disparar e desviar a trajetoria
	 */
	public void onHitRobot(HitRobotEvent e) {		
		turnGunRight(getHeading() - getGunHeading() + e.getBearing());
		double energy = getEnergy();
		double bulletPower = calcFire(energy);
		fire(bulletPower);
		turnLeft(90);
        ahead(50);
	}
	
	/**
	 * onHitWall: Posicionar na parede de modo a conseguir deslocar se e contornar os cantos
	 */
	public void onHitWall(HitWallEvent e) {		
		if(wall == 0) {
			wall = 1;
			
			if(getHeading() == 180)
				turnRight(90 - getHeading());
			else if(getHeading() == 270)
				turnRight(90);
			else
				turnRight(90 - getHeading());
		}
		else {
			turnLeft(90);
		}
		
		ahead(50);
	}
	
	/**
	 * onHitByBullet: Mover o canhao para o inimigo, disparar e desviar a trajetoria
	 */
	public void onHitByBullet(HitByBulletEvent e) {
        turnGunRight(getHeading() - getGunHeading() + e.getBearing());
        double energy = getEnergy();
		double bulletPower = calcFire(energy);
		fire(bulletPower);
        ahead(50);
	}
	
	/**
	 * onBulletHit: Disparar para a mesma posicao
	 */
	public void onBulletHit(BulletHitEvent e) {
		hits++;
		misses = 0;
		if(hits >= 5) {
			setBodyColor(Color.white);
		}
		double energy = getEnergy();
		double bulletPower = calcFire(energy);
		fire(bulletPower);
	}
	
	/**
	 * onWin: Registar vitória
	 */
	public void onWin(WinEvent e) {
		win = true;
		death = false;
	}
	
	/**
	 * onDeath: Registar morte
	 */
	public void onDeath(DeathEvent e) {
		death = true;
		win = false;
	}
	
	/**
	 * onBulletMissed: 
	 */
	public void onBulletMissed(BulletMissedEvent e) {
		misses++;
		hits = 0;
		if(misses >= 5) {
			setBodyColor(Color.black);
		}
	}
	
	/**
	 * color: 
	 */
	public void color() {
		if(win) {
			setBodyColor(Color.green);
			setGunColor(Color.green);
			setRadarColor(Color.green);
			setBulletColor(Color.white);
			setScanColor(Color.white);
		}
		else if(death) {
			setBodyColor(Color.red);
			setGunColor(Color.red);
			setRadarColor(Color.red);
			setBulletColor(Color.white);
			setScanColor(Color.white);
		}
		else {
			setBodyColor(Color.yellow);
			setGunColor(Color.yellow);
			setRadarColor(Color.yellow);
			setBulletColor(Color.white);
			setScanColor(Color.white);
		}
	}
	
	/**
	 * calcFire: 
	 */
	public double calcFire(double energy) {
		double bulletPower = 3.0;
		
		if(hits < 5) {
			if(death && (energy < 50)) {
				if(energy < 20) {
					bulletPower = bulletPower * 0.5;
				}
				else {
					bulletPower = bulletPower * 0.8;
				}
			}
		}
		
		bulletPower = Math.min(bulletPower, energy);
		
		return bulletPower;
	}
}
