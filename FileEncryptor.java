import java.awt.image.BufferedImage;

import java.io.FileReader;

import javax.imageio.ImageIO;

/**
 *
 * Encrypts a file into an image.
 *
 */
public class FileEncryptor {
	
	public static final String CORRECT_USAGE = "java FileEncryptor inputFileName [inputImageName [outputImageName]]";
	private static final int NUMBER_BITS_USED_PER_BYTE = 1;
	
	public static void main(String args[]) {
		/*
		 * If no arguments are provided,
		 * display the correct usage of this
		 * utility, and then exit.
		 */
		if (args.length == 0) {
			System.err.println(CORRECT_USAGE);
			System.exit(-1);
		}
		/*
		 * If one argument is provided,
		 * display the number of pixels necessary
		 * to store the information given in the 
		 * input file.
		 */
		if (args.length == 1) {
			
			System.exit(0);
		}
		/*
		 * If two or more arguments are provided,
		 * encrypt the file with the provided name
		 * into a new image with a new name.
		 */
		else {
			
		}
	}
}