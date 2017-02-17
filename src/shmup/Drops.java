package shmup;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Drops {
	private int x;
	private int y;
	private BufferedImage Drop;
	private int width;
	private int height;
	private double speed;
	private double yMult;
	public boolean isScore;
	public boolean hit;
	public Drops(int x, int y, String Sprite, boolean isScore){
		this.x = x;
		this.y = y;
		speed = -1;
		this.isScore = isScore;
		hit = false;
		try{
        	Drop = ImageIO.read(this.getClass().getResource("\\sprites\\" + Sprite));
        } catch(IOException e) {
        	e.printStackTrace();
    	}
		width = Drop.getWidth();
		height = Drop.getHeight();
	}
	public Rectangle getBounds(){
		return new Rectangle(x, y, width, height);
	}
	public void paintComponent(Graphics g){
		g.drawImage(Drop, x, y, null);
	}
	public void cycle(){
		yMult += speed;
		int dy = 0;
		while(Math.abs(yMult) > 1){
			if(yMult > 0){
				dy++;
				yMult--;
			} else {
				dy--;
				yMult++;
			}
		}
		y += dy;
		speed += .05;
		if(y >= 800 || x >= 600 || x <= 0 - width || y <= 0 - height){
			hit = true;
		}
	}
	public void isHit(){
		hit = true;
	}
}
