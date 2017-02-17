package shmup;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

public class enemy {
	private BufferedImage enemy, explosion;
	private boolean visible;
	private int width;
	private int height;
	private int health;
	private int explosionWidth, explosionHeight;
	private int x;
	private int y;
	private int speed;
	private int ai;
	private double increment;
	private double incrX, incrY;
	public double bulletAimX, bulletAimY;
	private List drops; //lists the drops of the enemy. SHould probably get rid of this and make drops objects of the panel class.
	private boolean bounds, dead;
	public boolean hit, remove; //kill booleans. For the explosion sequence.
	private boolean positive = true;
	private int frame, explosionFrame; //same as player in usage.
	private int explosionIncr = 0; //increment for explosion frame.
	private boolean addBullets;
	private boolean wantsPlayerPosition;
	public boolean AimBulletData;
	private double playerX, playerY; //tracks the player's X and Y slope, relative to a unit. Useful for suicide enemies.
	private int instruction; //this keeps track of the instruction number of the enemy.
	private int repeatInstruction; //if you want to repeat an instruction multiple times
	private int RNGMAX; //sets maximum degree (from straight down) the rng can generate. This is twice the actual max angle, as it counts negative and positive.
	public int bombDrops; //sets number of bomb pieces the enemy drops
	public int scoreDrops; //sets number of score pieces the enemy drops
	private preferredBulletMovement bulletAim;
	public double bulletSpeed;
	public int bulletAi;
	public String BulletSprite;
	private Random_Number_Generator RNG;
	private double angle; //THIS IS IN RADIANS
	private enum preferredBulletMovement{
		AIM, RANDOM, RANDOMAIM, DOWN
	}
	public enemy(String enemyImage, String explosionAnim, int x, int y, int health, int ai){
		try{
        	enemy = ImageIO.read(this.getClass().getResource("\\sprites\\" + enemyImage));
        	explosion = ImageIO.read(this.getClass().getResource("\\sprites\\" + explosionAnim));
        } catch(IOException e) {
        	e.printStackTrace();
    	}
		RNG = new Random_Number_Generator();
		explosionFrame = 0;
		width = enemy.getWidth() / 3;
		height = enemy.getHeight();
		explosionWidth = explosion.getWidth() / 8;
		explosionHeight = explosion.getHeight();
		visible = true;
		this.x = x - (width / 2);
		this.y = y - (height / 2);
		this.health = health;
		this.ai = ai;
		increment = 0;
		incrX = 0;
		incrY = 0;
		instruction = 0;
		speed = 0;
		addBullets = false;
		wantsPlayerPosition = false;
		playerX = 0;
		playerY = 0;
		angle = 0;
		AimBulletData = true; //this lets the panel object know that it needs to initialize the data for aiming its bullets.
		switch(ai){ //this sets the data for the enemy's bullets. Will need separate data for each ai type.
		
		}
		BulletSprite = "\\sprites\\testLongBullet.png"; //BulletSprite, bulletSpeed, RNGMAX, and bulletAi should all eventually be put in the switch above. same with preferredBulletMovement.
		bulletSpeed = 0;
		bulletAi = 5;
		RNGMAX = 90;
		bulletAim = preferredBulletMovement.AIM;
		bombDrops = 0; //both Drops vars will be set by the ai switch above. Bomb items will fill a charge meter, and release a "mini-bomb" that lasts the duration of the charge
		scoreDrops = 10; //should not have more than around 20 drops.
	}
	public boolean isVisible(){
		if(bounds || dead){
			visible = false;
		} else {
			visible = true;
		}
		return visible;
	}
	public int getCraftWidth(){
		return width;
	}
	public int getCraftHeight(){
		return height;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	public boolean addBullets(){
		return addBullets;
	}
	public BufferedImage getImage(){
		return getSprite();
	}
	public Rectangle getBounds(){
		return new Rectangle(x, y, width, height);
	}
	private BufferedImage getSprite(){
		BufferedImage bi;
		if(hit){
			bi = explosion.getSubimage(explosionFrame * explosionWidth, 0, explosionWidth, explosionHeight);
			explosionIncr++;
			if(explosionFrame >= 7){
				dead = true;
			}
		} else {
			bi = enemy.getSubimage(frame * width, 0, width, height);
		}
		if(explosionIncr == 60){
			explosionIncr = 0;
			++explosionFrame;
		}
		return bi;
	}
	public void inBounds(){
		if(y <= 800 && x <= 600 && x >= 0 - width && y >= 0 - height){
			bounds = false;
		} else {
			bounds = true;
		}
	}
	public void cycle(){
		addBullets = false;
		wantsPlayerPosition = false;
		if(!hit){
			switch(ai){
			case 0 :
				switch(instruction){ //both case 0 and 1 are the same. Both are examples of the curve. One has the "aim once each round" code, and the other has "constantly reset aim" code.
				case 0 :
					setCurve(0, false, 2);
					instruction++;
					break;
				case 1 :
					curve();
					if(increment % 90 == 0){
						instruction = 3; //sets the next increment to do instruction 3.
						return;
					}
					if(increment <= -360){
						instruction++;
					}
					break;
				case 2 :
					
					playerX = 0; //an example of overriding playerX and playerY to move in  a specific, pre-programmed manner.
					playerY = 1;
					movePlayerPosition(); //movePlayerPosition can be used just for moving straight too.
					break;
				case 3 : //this is an example of broken structure - this instruction is outside of the movement ones. it denotes a subroutine within the enemy's routine.
					if(repeatInstruction % 20 == 0){
						AimBulletData = true; //tells the panel class the enemy wants to reset the bullet aim. Since its inside the subroutine, it recalculates the aim for each bullet shot.
						addBullets = true;
					}
					if(repeatInstruction == 100){
						instruction = 1;
						repeatInstruction = 0;
						return;
					}
					repeatInstruction++;
					break;
				}
				break;
			case 1 :
				switch(instruction){
				case 0 :
					setCurve(0, false, 2);
					instruction++;
					break;
				case 1 :
					curve();
					if(increment % 90 == 0){
						AimBulletData = true; //tells the panel class the enemy wants to reset the bullet aim. It is only activated once per round.
						instruction = 3;
						return;
					}
					if(increment <= -360){
						instruction++;
					}
					break;
				case 2 :
					
					playerX = 0; //an example of overriding playerX and playerY to move in  a specific, pre-programmed manner.
					playerY = 1;
					movePlayerPosition(); //movePlayerPosition can be used just for moving straight too.
					break;
				case 3 : //this is an example of broken structure - this instruction is outside of the movement ones. it denotes a subroutine within the enemy's routine.
					if(repeatInstruction % 20 == 0){
						addBullets = true;
					}
					if(repeatInstruction == 100){
						instruction = 1;
						repeatInstruction = 0;
						return;
					}
					repeatInstruction++;
					break;
				}
				break;
			case 2 :
				wantsPlayerPosition = true;
				speed = 1;
				movePlayerPosition();
				break;
			case 3 :
				switch(instruction){
				case 0 :
					wantsPlayerPosition = true;
					instruction++;
					break;
				case 1 :
					speed = 1;
					movePlayerPosition();
					break;
				}
				break;
			}	
		} else {
			angle = 0;
		}
		inBounds();
	}
	public void isHit(int damage){
		if(health < 0){
			hit = true;
		} else {
			health -= damage;
		}
	}
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g; //not used now, but will be useful for rotating sprites later.
		g2.rotate(-angle, x + width / 2, y + height / 2);
		g2.drawImage(getImage(), x, y, null);
		g2.rotate(angle, x + width / 2, y + height / 2);
	}
	private void setCurve(int startIncr, boolean positive, int speed){ //sets variables for the curve: the starting increment, whether the curve is positive(counter-clockwise) or not, and the speed of the curve.
		this.positive = positive;
		increment = startIncr;
		this.speed = speed;
	}
	private void curve(){ //this generates a curve given a speed and a starting degree and rotation direction
		double sin = 0, cos = 0;
		sin = Math.sin(Math.toRadians(increment)); //the X speed, based on the angle. Works in degrees.
		cos = Math.cos(Math.toRadians(increment));
		angle = Math.toRadians(increment);
		incrX += speed*sin; //the X speed based on the speed multiplier. the speed is added the remaining velocity of IncrX to make the current velocity. This makes a smoother turn.
		incrY += speed*cos; //the Y speed based on the speed multiplier.
		if(positive){ //self explanatory.
			++increment;
		} else {
			--increment;
		}
		moveStraight();
	}
	private void moveStraight(){ //never call this class from cycle. It is only called from curve and movePlayerPosition.
		int xVel = 0, yVel = 0;
		while(Math.abs(incrX) >= 1){// when the X speed of this cycle is greater than one, this is activated.
			if(incrX >= 0){ //when speed is positive
				--incrX;
				++xVel;
			} else { //when speed is negative
				++incrX;
				--xVel;
			}
			
		}
		while(Math.abs(incrY) >= 1){ //the same, but for Y
			if(incrY >= 0){
				--incrY;
				++yVel;
			} else {
				++incrY;
				--yVel;
			}
		}
		x += xVel; //deltaX
		y += yVel; //deltaY
	}
	public boolean wantsPlayerPosition(){
		return wantsPlayerPosition;
	}
	public void setPlayerPosition(int x, int y){ //given the player's position, this class sets the unit speed for X and Y for an enemy homing into the player.
		int CraftXPos = this.x + (width / 2), CraftYPos = this.y + height;
		int distX = x - CraftXPos, distY = y - CraftYPos;
		double slope = Math.sqrt((distY*distY) + (distX*distX)); //sets hypotenuse
		double sin = distX/slope;
		double cos = distY/slope;
		playerX = sin;
		playerY = cos;
	}
	public void movePlayerPosition(){
		incrX += speed*playerX;
		incrY += speed*playerY;
		angle = Math.asin(playerX);
		if(playerY <= 0){
			angle = -angle + Math.toRadians(180);
		}
		moveStraight();
	}
	public void setAimData(int x, int y){ //given the player's position, this class sets the unit speed for X and Y for an enemy homing into the player.
		int CraftXPos = this.x + (width / 2), CraftYPos = this.y + height;
		int distX = x - CraftXPos, distY = y - CraftYPos;
		double slope = Math.sqrt((distY*distY) + (distX*distX)); //sets hypotenuse
		int randAim;
		double sin, cos;
		switch (bulletAim) {
		default:
			break;
		case AIM :
			sin = distX/slope;
			cos = distY/slope;
			bulletAimX = sin;
			bulletAimY = cos;
			break;
		case RANDOM :
			randAim = RNG.RNGINT(RNGMAX);
			bulletAimX = Math.cos(Math.toRadians(randAim));
			bulletAimY = Math.sin(Math.toRadians(randAim));
			break;
		case RANDOMAIM :
			randAim = RNG.RNGINT(RNGMAX) - (RNGMAX / 2);
			double aimAngle = Math.toDegrees(Math.asin(distY/slope));
			if(distX < 0){
				aimAngle = -aimAngle + 180;
			}
			double aim = aimAngle + randAim;
			bulletAimX = Math.cos(Math.toRadians(aim));
			bulletAimY = Math.sin(Math.toRadians(aim));
			break;
		case DOWN :
			bulletAimX = 0;
			bulletAimY = 1;
			break;
		}
		AimBulletData = false;
		
	}
	
}
