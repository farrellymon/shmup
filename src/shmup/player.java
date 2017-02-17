package shmup;



import java.awt.*;

import javax.imageio.ImageIO;

import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class player implements Runnable {
	private int screenWidth, screenHeight;
	private int craftWidth, explosionWidth, hitboxWidth;
    private int craftHeight, explosionHeight, hitboxHeight;
    private int invincibility; //sets how many frames until player can be hit again. This can be used at any time
    private Thread Threadkeys;
    private BufferedImage image, explosion, hitbox;
    private int x, dx; //dx and dy are the "delta" variables. They determine the speed of the craft, and the direction.
    private int y, dy;
    private int velocity;
    private int frame, expFrame, expIncr = 0; //calls which frame of the craft file is painted
    private int hitFrame, hitIncr = 0;
    private boolean hitPos = true;
	private boolean left, right, up, down, shot, bomb, focus, bombing = false;
	private boolean hit;
	private boolean invincible;
	private int limit; //limits the amount of frames required between shots.
	private boolean addBulletBool;
	public player(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    	try{
        	image = ImageIO.read(this.getClass().getResource("\\sprites\\newtestship.png"));
        	explosion = ImageIO.read(this.getClass().getResource("\\sprites\\testExplosion.png")); //the explosion sprite needs to be the same size as the player sprite. This should be the same for enemies, too.
        	hitbox = ImageIO.read(this.getClass().getResource("\\sprites\\hitbox.png"));
        } catch(IOException e) {
        	e.printStackTrace();
    	}
    	craftWidth = image.getWidth() / 3;
        craftHeight = image.getHeight();
        hitboxWidth = hitbox.getWidth() / 5;
        hitboxHeight = hitbox.getHeight();
        x = screenWidth / 2 - craftWidth/2;
        y = screenHeight - craftHeight;
        Threadkeys = new Thread(this);
        Threadkeys.start();
        explosionWidth = explosion.getWidth() / 8;
		explosionHeight = explosion.getHeight();
		isFocused();
		limit = 0;
		hit = false;
		invincible = false;
		invincibility = 0;
    }
	private BufferedImage getImage(){
		BufferedImage bi;
		if(hit){
			bi = explosion.getSubimage(expFrame * explosionWidth, 0, explosionWidth, explosionHeight);
			expIncr++;
			if(expFrame >= 7){
				hit = false; //this is test code
				invincible = true;
				expFrame = 0;
				invincibility = 600;
			}
		} else {
			bi = image.getSubimage(frame * craftWidth, 0, craftWidth, craftHeight); //subimages are important for the multiframe images like craft and explosion
		}
		if(expIncr == 60){
			expFrame++;
			expIncr = 0;
		}
		return bi;
	}
	public void move(){ //really, this should be part of the cycle method. but I keep it like this anyway.
    	if(!hit){
    		x += dx;
        	y += dy;
        	cycle(); //make sure to add a cycle method for when the ship is hit, if neccessary.
    	}
    }
    
	public Rectangle getHitboxShape(){
		return new Rectangle(x + (craftWidth / 2) - (hitboxWidth / 2), y + (craftHeight / 2) - (hitboxHeight / 2), hitboxWidth, hitboxHeight);
	}
	public Rectangle getBounds(){
		return new Rectangle(x, y, craftWidth, craftHeight);
	}
    public void run(){ // this thread runs the keylogging function. When active, it determines which actions can be done based on which buttons are pressed.
    	while(true){
    		isFocused();
    		if(up == true && down == false)
    	    {
    			if(y >= 5){ //determines where the wall of the player area is.
    	        	dy = -velocity; 
    	        } else {
    	        	dy = 0;
    	        }
    		} else if(down == true && up == false)
    	    {
    	    	if(y <= (screenHeight - craftHeight + 5)){ 
    	        	dy = velocity;
    	        } else {
    	        	dy = 0;
    	        }
    	    } else {
	        	dy = 0;
	        }
    	    if(left == true && right == false)
    	    {
    	    	if(x >= 5){ 
    	        	dx = -velocity;
    	        	frame = 2;
    	        } else {
    	        	dx = 0;
    	        }
    	    } else if(right == true && left == false)
    	    {
    	        if(x <= (screenWidth - craftWidth + 5)){ 
    	        	dx = velocity;
    	        	frame = 1;
    	        } else {
    	        	dx = 0;
    	        }
    	    } else {
    	    	frame = 0;
    	    	dx = 0;
    	    }
    		try{
    	        Thread.sleep(1); // not sure why this is here, probably to slow down the method. Might get rid of the method and put it in the cycle method instead?
    	    }
    	    catch(InterruptedException ie)
    	    {
    	        ie.printStackTrace();
    	    }
    	}
    }
    
    public void keyPressed(KeyEvent e) { //key functions are the primary input of the game. Each Button corresponds to a different function.

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            left  = true;
        }

        if (key == KeyEvent.VK_RIGHT) {
            right = true;
        }

        if (key == KeyEvent.VK_UP) {
            up = true;
        }

        if (key == KeyEvent.VK_DOWN) {
            down = true;
        }
        
        if (key == KeyEvent.VK_Z) {
            shot = true;
        }
        if (key == KeyEvent.VK_X) {
        	if(!bombing){
        		bomb = true;
        	}
        }
        if (key == KeyEvent.VK_SHIFT) {
            focus = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            left = false;
        }

        if (key == KeyEvent.VK_RIGHT) {
            right = false;
        }

        if (key == KeyEvent.VK_UP) {
            up = false;
        }

        if (key == KeyEvent.VK_DOWN) {
            down = false;
        }
        
        if (key == KeyEvent.VK_Z) {
            shot = false;
        }
        if (key == KeyEvent.VK_X) {
        	bomb = false;
        }
        if (key == KeyEvent.VK_SHIFT) {
            focus = false;
        }
    }
    public void isFocused(){
    	if(focus){
    		velocity = 1;
    	}
    	else{
    		velocity = 4;
    	}
    }
    public void cycle(){
    	if(!(invincibility <= 0)){
    		--invincibility;
    	} else {
    		invincible = false;
    	}
    	if(shot && limit <= 0){
    		addBulletBool = true;
    		limit = 5;
    	} else { 
    		addBulletBool = false;
    	}
    	if(!(limit <= 0)){
    		--limit;
    	}
    }
    public boolean canShoot(){
    	return addBulletBool;
    }
    public int getCraftWidth(){
    	return craftWidth;
    }
    public int getCraftHeight(){
    	return craftHeight;
    }
    public void paintComponent(Graphics g){
    	Graphics2D g2 = (Graphics2D) g;
    	g.drawImage(getImage(), x, y, null);
    	if(!hit){
    		if(focus){
				g.drawImage(getHitbox(), x + (craftWidth / 2) - (hitboxWidth / 2), y + (craftHeight / 2) - (hitboxHeight / 2), null);
			}
    	}
    }
    private BufferedImage getHitbox(){
		BufferedImage bi;
		bi = hitbox.getSubimage(hitFrame * hitboxWidth, 0, hitboxWidth, hitboxHeight); //subimages are important for the multiframe images like craft and explosion
		hitIncr++;
		if(hitIncr == 120){
			hitIncr = 0;
			if(hitPos){
				hitFrame++;
			} else {
				hitFrame--;
			}
		}
		if(hitFrame == 4){
			hitPos = false;
		} else if(hitFrame == 0){
			hitPos = true;
		}
		return bi;
	}
    public int getX(){ //returns x.
    	return x;
    }
    
    public int getY(){ // returns y.
    	return y;
    }
    
    public void isHit(){
    	if(!invincible){
    		hit = true;
    	}
    }
    public boolean isAlive(){
    	return !hit;
    }
}
