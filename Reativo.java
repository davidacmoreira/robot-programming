package myrobots;

import robocode.Robot;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.HitByBulletEvent;
import robocode.BulletHitEvent;

import java.awt.*;


/**
 * Reativo - a robot by Grupo1
 * 
 */
public class Reativo extends Robot {
	private int wall = 0;
	
	public void run() {
		setBodyColor(Color.black);
		setGunColor(Color.black);
		setRadarColor(Color.green);
		setBulletColor(Color.white);
		setScanColor(Color.white);
		
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
		fire(bulletPower);		
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
		double energy = getEnergy();
		double bulletPower = calcFire(energy);
		fire(bulletPower);
	}
	
	/**
	 * calcFire: Calcular a intensidade do tiro
	 */
	public double calcFire(double energy) {
		double bulletPower = 3.0;
		
		if(energy < 50) {
			if(energy < 20) {
				bulletPower = bulletPower * 0.5;
			}
			else {
				bulletPower = bulletPower * 0.8;
			}
		}
		
		bulletPower = Math.min(bulletPower, energy);
		
		return bulletPower;
	}
}
