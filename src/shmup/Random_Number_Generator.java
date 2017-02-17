package shmup;

import java.util.Random;

public final class Random_Number_Generator { //THIS CLASS KEEPS TRACK OF THE RNG GIVEN A SEED ON CREATION.
	private static Random RNG;
	public Random_Number_Generator(int seed){
		RNG = new Random(seed);
	}
	public Random_Number_Generator(){
	}
	public double RNGDOUBLE(double maximum){ //returns a double between 0 and the given maximum
		return RNG.nextDouble() * maximum;
	}
	public boolean RNGBOOL(){
		return RNG.nextBoolean();
	}
	public int RNGINT(int maximum){ //returns an int between 0 and the given integer.
		return RNG.nextInt(maximum);
	}
}
