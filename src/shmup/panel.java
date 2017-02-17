package shmup;







import javax.swing.*;

import static java.util.Arrays.asList;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class panel extends JPanel implements Runnable {
	private int width = 600;
    private int height = 800;
    private Thread gameLoop;
    private state gameState = state.menu;
    private MenuState Menu = MenuState.Start;
    private player player;
    private ArrayList<bullet> bullets; //Important: this is a list of all the bullets in existence. Make sure EVERY class that has the capability of shooting bullets refers to the Panel Class!.
    private ArrayList<enemy> enemies;
    private ArrayList<Drops> itemList;
    private boolean canSpawn, canMoveUp, canMoveDown, canSelect, selectChoice, resetMousedOver, saveSelection, canReturn = false;
    private Random setSeed;
    private int seed;
    private Random_Number_Generator RNG;
    private Font font1, font2,font3;
    private Color color1, color2;
    private int mousedOverMin = 0; //both variables have default values that are set for MainMenu
	private int mousedOverMax = 3;
	private int mousedOver, waitTimer = 0;
	private String Replay[] = new String[10];
	private String Score[] = new String[10];
    public enum state{ //enum for game states. They are named for a reason.
    	game, menu, pause, stageEnd, gameEnd, Game_Over, Dialogue
    }
    private enum MenuState{ //enum for menuStates. I'm a lazy fuck.
		Start, Main, Replay, CharSelect, Score, Pause, Stage_Intermission, Game_Over, Continue, Ending, Enter_Score, Enter_Replay, Ask_Replay //Main, Replay, CharSelect, Pause, Enter_Score, Enter_Replay, Ask_Replay and Continue all have selections. Start, Stage_Intermission, Game_Over, Score, and Ending have none.
	}
	public panel() {
		setSeed = new Random();
		seed = setSeed.nextInt(1000);
		RNG = new Random_Number_Generator(seed);
		addKeyListener(new TAdapter());
		setFocusable(true);
		setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        player = new player(width, height); //constructor has two parameters: width and height of the screen.
        bullets = new ArrayList<bullet>(); //instantiates the array.
        enemies = new ArrayList<enemy>();
        itemList = new ArrayList<Drops>();
        font1 = new Font("Helvetica", Font.BOLD, 50);
		font2 = new Font("Helvetica", Font.BOLD, 20);
		font3 = new Font("Helvetica", Font.BOLD, 100);
		color1 = new Color(217, 128, 128, 255);
		color2 = new Color(255, 255, 255, 255);
		for(int i = 0; i < 10; i++){ //sets all the replay strings
			//code for getting replay Strings Here
			if(Replay[i] == null){
				Replay[i] = "Nanashi";
			}
		}
		for(int i = 0; i < 10; i++){ //sets all the Score strings
			//code for getting Score Strings Here
			if(Score[i] == null){
				Score[i] = "Nanashi";
			}
		}
    }
	
	
	@Override
    public void addNotify() {
        super.addNotify();

        gameLoop = new Thread(this);
        gameLoop.start();
    }
	
	public void run() { //this thread cycles through the game logic. It runs at a maximum of 60 FPS. Calls methods within the class.

        long lastTime = System.nanoTime();
        final double amountOfTicks = 60.0;
        double fps = 1000000000 / amountOfTicks;
        double delta = 0;
        int updates = 0;
        int frames = 0;
        long timer = System.currentTimeMillis();
        while (true) {
        	long now = System.nanoTime();
        	delta += (now - lastTime) / fps;
        	lastTime = now;
        	if(delta >= 2){
        		while(delta >= 2){
        			delta--;
        		}
        	}
        	if(delta >= 1){
        		if(gameState == state.game){
        			cycleStage();
        			checkCollisions();
        		}
        		cycle();
        		updates++;
        		delta--;
        	}
        	repaint();
            frames++;
            if(System.currentTimeMillis() - timer > 1000){
            	timer += 1000;
            	System.out.println(updates + " Ticks, Frames " + frames);
            	updates = 0;
            	frames = 0;
            }
            
        }
    }
	private class TAdapter extends KeyAdapter { //keylogger. like most of the methods here, it mostly serves to call methods outside the class.
		
        public void keyReleased(KeyEvent e) {
        	int key = e.getKeyCode();
        	player.keyReleased(e);
        	if(key == KeyEvent.VK_SPACE){
        		canSpawn = true;
        	}
        	if(key == KeyEvent.VK_UP){ //menu commands
        		canMoveUp = true;
        	}
        	if(key == KeyEvent.VK_DOWN){
        		canMoveDown = true;
        	}
        	if(key == KeyEvent.VK_Z){
        		canSelect = true;
        	}
        	if(key == KeyEvent.VK_X){
        		canReturn = true;
        	}
        }

        public void keyPressed(KeyEvent e) {
        	int key = e.getKeyCode();
        	player.keyPressed(e);
			if(key == KeyEvent.VK_SPACE && canSpawn){
				enemies.add(new enemy("testMook.png", "testExplosion.png", 300, 200, 9, 0)); //constructor requires Strings for the enemy's image, explosion, x and y, and health. Need to add: ai type, string for bullet name type.
				canSpawn = false;
        	}
			if(key == KeyEvent.VK_UP && canMoveUp){ //menu commands
        		canMoveUp = false;
        		mousedOver--;
        	}
        	if(key == KeyEvent.VK_DOWN && canMoveDown){
        		canMoveDown = false;
        		mousedOver++;
        	}
        	if(key == KeyEvent.VK_Z && canSelect){
        		canSelect = false;
        		selectChoice = true;
        	}
        	if(key == KeyEvent.VK_X && canReturn){
        		canReturn = false;
        		if(Menu == MenuState.Score || Menu == MenuState.Replay || Menu == MenuState.CharSelect){
        			Menu = MenuState.Main;
        		}
        		resetMousedOver = true;
        	}
        }
    }
	private void cycle() { //calls methods outside the class. This cycles the game logic of all the classes. very important.
		if(gameState == state.game){
			setBackground(Color.ORANGE);
			player.move();
			if(player.canShoot() && player.isAlive()){ //Yeah, this is going to need a major overhaul. Make sure that the method leads into a method within player, so that the player class can decide the bullets being shot. Then use those variables to decide the function of the bullet.
				bullets.add(new bullet(player.getX(), player.getY(), false, 0, 1, -10, 0, "\\sprites\\bullet1.png")); //gives data to the constructor function. Six variables: spawning x, spawning y, enemy bullet determination, x multiplier, y multiplier, and the ai type.
	    		bullets.add(new bullet(player.getX() + player.getCraftWidth(), player.getY(), false, 0, 1, -10, 0, "\\sprites\\bullet1.png"));
			}
			for (int i = 0; i < bullets.size(); i++) {
	    		bullet m = (bullet) bullets.get(i);
	    		if (m.isVisible()){
					m.cycle();
					if(m.resetAim){
						m.resetMultVars(player.getX() + (player.getCraftWidth() / 2), player.getY() + (player.getCraftHeight() / 2));
					}
				}else{ 
					bullets.remove(i);
				}
	    	}
			for (int i = 0; i < enemies.size(); i++) {
	    		enemy m = (enemy) enemies.get(i);
	    		if (m.isVisible()){
					m.cycle();
					if(m.wantsPlayerPosition()){ //the enemy can call whether it wants to know the player's position. This is specifically so that it can move toward the enemy, nothing to do with bullet aiming.
						m.setPlayerPosition(player.getX() + (player.getCraftWidth() / 2), player.getY() + (player.getCraftHeight() / 2));
					}
					if(m.AimBulletData){//aim bullet data decides the aim of bullets this enemy shoots. When it is called, the aim resets.
						m.setAimData(player.getX() + (player.getCraftWidth() / 2), player.getY() + (player.getCraftHeight() / 2));
					}
					if(m.addBullets() && !m.hit){ //need to add method to enemy for bullet data.
						bullets.add(new bullet(m.getX() + (m.getCraftWidth()/2), m.getY() + m.getCraftHeight(), true, m.bulletAimX, m.bulletAimY, m.bulletSpeed, m.bulletAi, m.BulletSprite));
					}
				}else{ 
					 if(m.hit){
						 for(int j = 0; j < m.scoreDrops; j++){
								itemList.add(new Drops(RNG.RNGINT(m.scoreDrops * 10) + m.getX() + (m.getCraftWidth() / 2) - ((m.scoreDrops * 10) / 2), RNG.RNGINT(m.scoreDrops * 10) + m.getY() + (m.getCraftHeight() / 2) - ((m.scoreDrops * 10) / 2), "testPickup.png", true));
							}
					 }
					enemies.remove(i);
				}
	    	}
			for (int j = 0; j < itemList.size(); j++){
				Drops m = (Drops) itemList.get(j);
				if(!m.hit){
					m.cycle();
				} else {
					itemList.remove(j);
				}
			}
		}
		if(gameState == state.menu){
			if(Menu != MenuState.Pause || Menu != MenuState.Game_Over){
				setBackground(Color.BLACK);
			}
			if(Menu == MenuState.Main || Menu == MenuState.CharSelect || Menu == MenuState.Replay || Menu == MenuState.Pause || Menu == MenuState.Continue || Menu == MenuState.Ask_Replay || Menu == MenuState.Enter_Score || Menu == MenuState.Enter_Replay){ //this is the code specifically for menus with selections.
				if(Menu == MenuState.Main){ //this if determines the lowest and highest number mousedOver can access. The Four Actions of Main are Start, Replay, Score, and Quit. It is static.
					mousedOverMin = 0;
	    			mousedOverMax = 3;
	    		} else if(Menu == MenuState.CharSelect){ //Fighter 1, Fighter 2, Fighter 3, and Fighter 4. Scrolling menu.
	    			mousedOverMin = 9;
	    			mousedOverMax = 12;
	    		} else if(Menu == MenuState.Pause){ //Back to Gameplay, Restart from Beginning, and Back to Title. Static and has the game as the background.
	    			mousedOverMin = 4;
	    			mousedOverMax = 6;
	    		} else if(Menu == MenuState.Continue){ //Yes and No. Static Menu.
	    			mousedOverMin = 7;
	    			mousedOverMax = 8;
	    		} else if(Menu == MenuState.Ask_Replay){ //Yes and No. Static Menu.
	    			mousedOverMin = 13;
	    			mousedOverMax = 14;
	    		} else if(Menu == MenuState.Replay){ //These are all the Replays that you can select. The Format for Replay Strings is: Name + " Shot Type: " + Type + " Score: " + Score. Static.
	     			mousedOverMin = 15;
	     			mousedOverMax = 24;
	     		} else if(Menu == MenuState.Enter_Score || Menu == MenuState.Enter_Replay){ //depends on the number of Characters I want available. Scrolling Menu.
	    			mousedOverMin = 25;
	    			mousedOverMax = 100; //this number is a placeholder.
	    		}
				if(resetMousedOver){ //this is called after a new selection is made; it resets mousedOver to the default Menu value.
					mousedOver = mousedOverMin; //this is mousedOver's default value.
					resetMousedOver = false;
					waitTimer = 0; //resets the waiting timer for certain screens.
				}
				if(mousedOver >  mousedOverMax) {
					mousedOver = mousedOverMin;
				}	
				if(mousedOver < mousedOverMin) {
					mousedOver = mousedOverMax;
				}
				if(selectChoice){ //this is called whenever a selection in the menu is made. It calls the reset of mousedOver.
					switch(mousedOver){ //each number of the mousedOver set has a unique path to go to.
					default: //used for Replay, Enter_Replay, and Enter_Score since they have so many damn choices and I'm not writing all that shit.
						break;
					case 0: //these are the actions for each selection. Most go to another Menu.
						Menu = MenuState.CharSelect; //shot type selection screen.
						break;
					case 1:
						Menu = MenuState.Replay; //replay selection screen
						saveSelection = false; //tells menu that the selection is not being used to save a new replay.
						break;
					case 2:
						Menu = MenuState.Score; //goes to the score screen.
						break;
					case 3:
						//quit code goes here.
						break;
					case 4:
						gameState = state.game; //goes back to the game.
						break;
					case 5:
						//returns to the beginning of the game. Reset Everything but shot type.
						break;
					case 6:
						Menu = MenuState.Main; //returns to title
						break;
					case 7:
						//restarts the game, everything but shot type and place of death are reset.
						break;
					case 8:
						Menu = MenuState.Enter_Score; //selecting no goes to enter score, which is where games go when finished. If score is not on scoreboard, then it should quit to main menu. If game is Completed, the game will instead go to Game_Over, then Ending.
						break;
					case 9: case 10: case 11: case 12:
						//selects Shot Type, and starts the game at the beginning.
						gameState = state.game;
						break;
					case 13:
						Menu = MenuState.Replay; //goes to replay selection
						saveSelection = true; //tells menu that the selection is going to be used to save a new replay.
						break;
					case 14:
						Menu = MenuState.Main; //returns to title
						break;
					}
					selectChoice = false;
					resetMousedOver = true;
				}
			} else { //for menus without selections.
				if(resetMousedOver){ //this is called after a new selection is made; it resets waitTimer to the default Menu value.
					waitTimer = 0; //resets the waiting timer for certain screens.
				}
				if(Menu == MenuState.Stage_Intermission){ //will show the intermission score, gives bonuses for stage clears without deaths/bombs.
					if(selectChoice && waitTimer >= 500){
						gameState = state.game; //return to game.
						//start next stage
						//reset stage scrolling
					}
				} else if(Menu == MenuState.Game_Over){ //this will show a "Game Over" screen, along with the final score (with lives + bombs added to it)
					if(selectChoice && waitTimer >= 500){
						Menu = MenuState.Ending;
					}
				} else if(Menu == MenuState.Ending){ //add later!
					if(selectChoice && waitTimer >= 500){
						
					}
				} else if(Menu == MenuState.Start){ //Title Screen.
					if(selectChoice){
						Menu = MenuState.Main;
					}
				}
			}
		}
    }
	public void paintComponent(Graphics g){ //calls methods outside the class. This calls paints methods for the game's graphics.
    	super.paintComponent(g);
    	if(gameState == state.game || Menu == MenuState.Pause){
    		player.paintComponent(g);
        	for (int i = 0; i < bullets.size(); i++) {
        		bullet m = (bullet) bullets.get(i);
        		m.paintComponent(g);
        	}
        	for (int i = 0; i < enemies.size(); i++) {
        		enemy m = (enemy) enemies.get(i);
        		m.paintComponent(g);
        	}
        	for(int j = 0; j < itemList.size(); j++){
    			Drops m = (Drops) itemList.get(j);
    			m.paintComponent(g);
    		}
    	}
    	if(gameState == state.menu){
    		if(Menu == MenuState.Main || Menu == MenuState.CharSelect || Menu == MenuState.Replay || Menu == MenuState.Pause || Menu == MenuState.Continue || Menu == MenuState.Ask_Replay){ //for the options with selections and are static. CharSelect will need its own eventually since it scrolls.
    			String[] menuSelections = {"Start", "Replay", "Score", "Quit", "Back to Gameplay", "Restart from Beginning", "Back to Title", "Yes", "No", "Fighter 1", "Fighter 2", "Fighter 3", "Fighter 4", "Yes", "No", Replay[0], Replay[1], Replay[2], Replay[3], Replay[4], Replay[5], Replay[6], Replay[7], Replay[8], Replay[9]};
    			for(int i = mousedOverMin; i <= mousedOverMax; ++i){
        			g.setFont(font2);
    				if( i == mousedOver){
    					g.setColor(color1);
    				} else {
    					g.setColor(color2);
    				}
    				String s = menuSelections[i];
    				if(Menu != MenuState.Replay){
    					g.drawString(s, 600 / 2 - getStringWidth(g, s), 800 / 2 + (100*(i - mousedOverMin)) - 50 + getStringHeight(g, s));
    				} else { //this is the string drawing for Replay
    					g.drawString(s, 600 / 2 - getStringWidth(g, s), (75*(i - mousedOverMin)) + 50 + getStringHeight(g, s));
    				}
    			}
    		} else if(Menu == MenuState.Enter_Score || Menu == MenuState.Enter_Replay){ //for Entering Score and Replay names.
    			
    		} else { //for all options without selections.
    			if(Menu == MenuState.Stage_Intermission){ //Shows Intermission text. Current is a placeholder.
					g.setColor(color2);
					g.setFont(font1);
					String s = ("Placeholder Text.");
					String s2 = ("Press Shot to Continue!");
					g.drawString(s, 600 / 2 - getStringWidth(g, s), 100 + getStringHeight(g, s));
					if(waitTimer >= 500){
						g.setFont(font2);
						g.drawString(s2, 600 / 2 - getStringWidth(g, s2), 800 / 2 + getStringHeight(g, s2));
					}
				} else if(Menu == MenuState.Start){ //title and a "press shot to begin" String
					g.setColor(color2);
					g.setFont(font1);
					String s = new String("PACIFIC RIFT:");
					String s2 = new String("EPISODE 0");
					g.drawString(s, 600 / 2 - getStringWidth(g, s), 50 + getStringHeight(g, s));
		        	g.drawString(s2, 600 / 2 - getStringWidth(g, s2), 100 + getStringHeight(g, s2));
		        	g.setFont(font2);
		        	String s3 = new String("Press Shot to Begin");
		        	g.drawString(s3, 600 / 2 - getStringWidth(g, s3), 800 / 2 + getStringHeight(g, s3));
				} else if(Menu == MenuState.Score){ //This has no selections, or actions. The only option here is to return. Format For Score is the same as replays.
					for(int i = 0; i < 10; ++i){
						g.setColor(color2);
	        			g.setFont(font2);
	        			String s = Score[i];
	    				g.drawString(s, 600 / 2 - getStringWidth(g, s), (75*(i - mousedOverMin)) + 50 + getStringHeight(g, s));
	    			}
				} else if(Menu == MenuState.Ending){ //Shows the ending. Multiple slides for this, they will be handled in the cycle method.
					
				} else if(Menu == MenuState.Game_Over){ //Shows Game Over as described in the cycle method.
					
				}
    		}
    	}
		Toolkit.getDefaultToolkit().sync();
		g.dispose();
    }
	private void cycleStage(){ //handles the stages of the game. A separate function as it only needs to be called a few times.
    	
    }
	private void checkCollisions(){ //calls methods outside the class. This cycles the collision detection.
		for (int i = 0; i < bullets.size(); i++) {
            bullet m = (bullet) bullets.get(i);
            Rectangle r1 = m.getBounds();
            AffineTransform transform = new AffineTransform();
            transform.rotate(-m.getAngle(), r1.getX() + (r1.width/2), r1.getY() + (r1.height/2));
            Shape bullet = transform.createTransformedShape(r1);
            Rectangle r2;
            if(m.getEnemyBullet()){
            	if(player.isAlive()){
            		r2 = player.getHitboxShape();
            		if (bullet.intersects(r2)) {
                		m.isHit();
            			player.isHit();
                    }
            	}
            	
            } else {
            	for (int j = 0; j<enemies.size(); j++) {
                    enemy a = (enemy) enemies.get(j);
                    r2 = a.getBounds();

                    	if (bullet.intersects(r2)) {
                    		if(!a.hit){
                    			m.isHit();
                    			a.isHit(10);
                    	}
                    }
            	}
            }    
        }
		for (int j = 0; j<enemies.size(); j++) {
            enemy a = (enemy) enemies.get(j);
            Rectangle r1 = a.getBounds();
            Rectangle r2 = player.getHitboxShape();

            	if (r1.intersects(r2)) {
            		if(!a.hit){
            			player.isHit();
            			a.isHit(10);
            	}
            }
    	}
		for (int j = 0; j < itemList.size(); j++){
			Drops a = (Drops) itemList.get(j);
			Rectangle r1 = a.getBounds();
			Rectangle r2 = player.getBounds();
				if(r1.intersects(r2)){
					if(!a.hit){
            			a.isHit();
            			if(a.isScore){ //must be finished later
            				
            			} else {
            				
            			}
					}
				}
		}
    }
	private int getStringWidth(Graphics g, String s){ //these return a String given some stuff
		int stringWidth = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
		return stringWidth/2;
	}
	private int getStringHeight(Graphics g, String s){
		int stringHeight = (int) g.getFontMetrics().getStringBounds(s, g).getHeight();
		return stringHeight;
	}
}
