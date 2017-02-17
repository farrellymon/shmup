
package shmup;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
public class bullet {
	private BufferedImage image;
    private int width, height;
    private int x,y;
    private int instruction;
    private int repeatInstruction; //does the same as instruction, but is used specifically for keeping track of recursive instructions.
    private int increment; //used for keeping track of curves
    private int incrementIncr; //determines how much increment increases by each time it is increased.
    private double xCount, yCount; //Counter Variables: used for converting double variables into integers without loss of information.
    private double xMult,yMult; //these Mult variables determine the movement of the bullet; these are determined by a trig function in the parent class.
    private double speed;
    private double angle; //used in PaintComponent. In Radians.
    private boolean visible, grazable; //determines whether the bullet is active; if not, it is cleared from memory.
    private boolean enemybullet; // determines whether the bullet harms the player or enemies.
    private boolean positive;
    private int dx, dy; //same as in other classes; movement delta
    private int ai; //determines the movement path of the bullet. This can only be changed within the bullet class.
    private double increase; //rate which the bullet's speed increases. it is set in ai.
    public boolean resetAim; //this tells the panel class to reset the aim of the xMult and yMult Vars when set as true.
    private boolean bounce; //BULLETS CAN BOUNCE OFF WALLS NOW

	public bullet(int x, int y, boolean enemy, double xMult, double yMult, double speed, int ai, String Sprite){
		try{
        	image = ImageIO.read(this.getClass().getResource(Sprite));
        } catch(IOException e) {
        	e.printStackTrace();
    	}
		width = image.getWidth();
        height = image.getHeight();
		visible = true;
		this.x = x - width/2;
		this.y = y - width/2;
		this.xMult = xMult;
		this.yMult = yMult;
		this.speed = speed;
		enemybullet = enemy;
		this.ai = ai;
		enemybullet = enemy;
		xCount = 0;
		yCount = 0;
		grazable = true;
		instruction = 0;
		repeatInstruction = 0;
		increment = 0;
		positive = true;
		incrementIncr = 1;
		angle = 0;
		resetAim = false; //when true, the aim of the bullet is reset. Probably should not call this more than a few times.
		bounce = false; //when true, the bullet will bounce.
	}
    public int getX() { //returns x
        return x;
    }

    public int getY() { // returns y
        return y;
    }
    public double getAngle(){
    	return angle;
    }
    public boolean isVisible() { // returns the visibility of the bullet.
        return visible;
    }
    
    public boolean getEnemyBullet(){ //returns whether enemy or not
    	return enemybullet;
    }
    
    public void cycle(){//there should be multiple ais. Should probably make the movement a separate function.
    	int startIncr = (int) Math.toDegrees(Math.atan(xMult/yMult));
    	increase = 0;
    	switch (ai){
    	case 0 :
    		switch (instruction){
    		case 0 :
    			move(xMult, yMult);
    			break;
    		}
    		break;
    	case 1 :
    		switch (instruction) {
    		case 0 :
    			setCurve(-90, true, 1);
    			instruction++;
    			break;
    		case 1 :
    			curve();
    			if(increment >= 90){
    				setCurve(increment, false, 1);
    				instruction++;
    			}
    			break;
    		case 2 :
    			curve();
    			if(increment <= -90){
    				setCurve(increment, true, 1);
    				instruction--;
    			}
    			break;
    		}
    		break;
    	case 2 :
    		if(yMult < 0){
    			startIncr = startIncr + 180;
    		}
    		switch(instruction){
    		case 0 :
    			setCurve(startIncr - 45, true, 5);
    			instruction++;
    			break;
    		case 1 :
    			curve();
    			if(increment >= 45 + startIncr){
    				setCurve(increment, false, 5);
    				instruction++;
    			}
    			break;
    		case 2 :
    			curve();
    			if(increment <= -45 + startIncr){
    				setCurve(increment, true, 5);
    				instruction--;
    			}
    			break;
    		}
    		break;
    	case 3 : 
    		if(yMult < 0){
    			startIncr = startIncr + 180;
    		}
    		switch(instruction){
    		case 0 :
    			setCurve(startIncr + 45, false, 5);
    			instruction++;
    			break;
    		case 1 :
    			curve();
    			if(increment <= -45 + startIncr){
    				setCurve(increment, true, 5);
    				instruction++;
    			}
    			break;
    		case 2 :
    			curve();
    			if(increment >= 45 + startIncr){
    				setCurve(increment, false, 5);
    				instruction--;
    			}
    			break;
    		}
    		break;
    	case 4 : //this changes the aim of the bullet every 100 frames.
    		move(xMult, yMult);
			if(repeatInstruction >= 60) {
				repeatInstruction = 0;
				resetAim = true;
			} else {
				repeatInstruction++;
			}
			break;
    	case 5 : //example using the increase variable to increase the bullet's speed
    		increase += 100;
    		move(xMult, yMult);
    		break;
    	}
    	
    	if(y >= 800 || x >= 600 || x <= 0 - width || y <= 0 - height){
			if(bounce){
				if( x >= 600){
					xMult = -Math.abs(xMult);
				}
				if(y <= 0 - height){
					yMult = Math.abs(yMult);
				}
				if(x <= 0 - width){
					xMult = Math.abs(xMult);
				}
				if(y >= 800){
					yMult = -Math.abs(yMult);
				}
			} else {
				visible = false;
			}
		}
    	speed += increase;
    }
    public void move(double xMult, double yMult){ //this takes arguments so that xMult and yMult don't interfere with the curve method (where xMult and yMult are used to decide the initial curve when aimed). It usually takes xMult and yMult anyway, unless its a curve.
    	dx = 0;
		dy = 0;
		angle = Math.asin(xMult);
		if(yMult <= 0){
			angle = -angle + Math.toRadians(180);
		}
    	xCount += xMult*speed;
    	yCount += yMult*speed;
    	while(Math.abs(xCount) > 1){
    		if(xCount >= 0){
    			++dx;
    			--xCount;
    		} else {
    			--dx;
    			++xCount;
    		}
    		
    	}
    	while(Math.abs(yCount) > 1){
    		if(yCount >= 0){
    			++dy;
    			--yCount;
    		} else {
    			--dy;
    			++yCount;
    		}
    		
    	}
    	x += dx;
    	y += dy;
    }
    public Rectangle getBounds(){
		return new Rectangle(x, y, width, height);
	}
	public void isHit(){
		visible = false;
	}
	public int getWidth(){
		return width;
	}
	public int getHeight(){
		return height;
	}
	public void cannotGraze(){
		grazable = false;
	}
	public boolean getGraze(){
		return grazable;
	}
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		g2.rotate(-angle, x + width / 2, y + height / 2);
		g2.drawImage(image, x, y, null);
		g2.rotate(angle, x + width / 2, y + height / 2);
	}
	private void setCurve(int startIncr, boolean positive, int incr){ //sets variables for the curve: the starting increment, whether the curve is positive(counter-clockwise) or not, whether the increment is in radians or not, and increment of increment. Speed is not needed because it is set in the constructor.
		this.positive = positive;
		increment = startIncr;
		incrementIncr = incr;
	}
	private void curve(){ //this generates a curve given a speed and a starting degree and rotation direction. Move method is used automatically.
		double sin = 0, cos = 0;
		sin = Math.sin(Math.toRadians(increment)); //the X speed, based on the angle. Works in degrees.
		cos = Math.cos(Math.toRadians(increment)); //the Y speed, based on the same.
		if(positive){ //self explanatory.
			increment += incrementIncr;
		} else {
			increment -= incrementIncr;
		}
		move(sin, cos);
	}
	public void resetMultVars(int x, int y){
		int CraftXPos = this.x + (width / 2), CraftYPos = this.y + height;
		int distX = x - CraftXPos, distY = y - CraftYPos;
		double slope = Math.sqrt((distY*distY) + (distX*distX)); //sets hypotenuse
		xMult = distX/slope;
		yMult = distY/slope;
		resetAim = false;
	}
}
