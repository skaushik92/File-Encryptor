import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;

import java.math.BigInteger;

import javax.imageio.ImageIO;

/**
 *
 * Encrypts a file into an image.
 *
 */
public class FileEncryptor {
	
	public static final String CORRECT_USAGE = "java FileEncryptor inputFileName [inputPNGImageName]";
	
	public static void main(String args[]) {
		
		/**
		 *================================================================
		 *Step 1: Check arguments.
		 *================================================================
		 */
		
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
			String inputFileName = args[0];
			File fileToEncrypt = new File (inputFileName);
			InputStream is = null;
			try {
				is = new FileInputStream(fileToEncrypt);
			} catch (FileNotFoundException e) {
				System.err.println("Input file cannot be read.");
				System.exit(-1);
			}
			
			// Get the size of the file in bytes
			long length = fileToEncrypt.length();
			
			//can store half a byte in 1 pixel. so 2 pixels per byte in file + 8 bytes for the initial values.
			long pixelsNeeded = length*2 + 2*4*(1 + 1);
			System.out.println("Pixels needed (lowerbound): " + pixelsNeeded +"px");
			System.exit(0);
		}
		
		/**
		 *================================================================
		 *Step 2: Read in File and Image.
		 *================================================================
		 */
		
		/*
		 * If two or more arguments are provided,
		 * encrypt the file with the provided name
		 * into a new image with a new name.
		 */
		
		else {
			String inputFileName = args[0];
			String inputImageName= args[1];
			
			File fileToEncrypt = new File (inputFileName);
			File imageFile = new File (inputImageName);
			
			/**
			 *================================================================
			 *Step 2a: Read in Image.
			 *================================================================
			 */
			
			BufferedImage image = null;
			
			try {
				image = ImageIO.read(imageFile);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			if (image == null) {
				System.err.println("Image cannot be read.");
				System.exit(-1);
			}
			
			//Uncomment to see the size of the image being read
			//System.out.println("W: " + image.getWidth() + " H: " + image.getHeight());

			
			/**
			 *================================================================
			 *Step 2b: Read in File.
			 *================================================================
			 */
			
			InputStream is = null;
			try {
			is = new FileInputStream(fileToEncrypt);
			} catch (FileNotFoundException e) {
				System.err.println("Input file cannot be read.");
				System.exit(-1);
			}
			
			// Get the size of the file
			long length = fileToEncrypt.length();
			
			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				System.err.println("File is too large!");
				System.exit(-1);
			}
			//can store half a byte in 1 pixel. so 2 pixels per byte in file + 8 bytes for the initial values.
			long pixelsNeeded = length*2 + 2*4*(1 + 1);
			
			if (pixelsNeeded > image.getWidth() * image.getHeight()) {
				System.err.println("Pixels needed (lowerbound): " + pixelsNeeded +"px");
				System.err.println("Pixels currently: " + image.getWidth() * image.getHeight() + "px");
				System.exit(-1);
			}
			
			// Create the byte array to hold the data
			byte[] bytes = new byte[(int)length];
			
			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			
			try {
				while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead; 
				}
			} catch (IOException e) {
				System.err.println("Input file cannot be read completely.");
				System.exit(-1);
			}
			
			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				System.err.println("Could not completely read file "+fileToEncrypt.getName());
				System.exit(-1);
			}
			
			// Close the input stream and return bytes
			try {
				is.close();
			} catch (IOException e) {
				System.err.println("Input file cannot be closed.");
				System.exit(-1);
			}
			
			/**
			 *================================================================
			 *Step 3: Encrypt.
			 *================================================================
			 */
			
			/*
			 * Encryption happens in two steps.
			 * 1. Take the data and divide by a prime.
			 * 2. Write out the remainder and then the actual divided data.
			 */
			
			BigInteger primeNumber = new BigInteger ("52428" + "7");
			
			BigInteger fileAsNumber= new BigInteger (bytes);
			BigInteger[] divAndRem = fileAsNumber.divideAndRemainder(primeNumber);
			
			//Uncomment to see the dividend and the remainder
			//System.out.println("Dividend: " + divAndRem[0]);
			//System.out.println("Remainder: "+ divAndRem[1]);
			
			byte[] div = divAndRem[0].toByteArray();
			byte[] rem = divAndRem[1].toByteArray();
			
			int divLen = div.length;
			int remLen = rem.length;
			
			//Uncomment to see the remainder and dividend lengths
			//System.out.println("RemLen: " + remLen);
			//System.out.println("DivLen: " + divLen);
			
			int writeOutByteCount = 4 + 4 + remLen + divLen;
			
			byte[] remLenBytes = intToByteArray(remLen);
			byte[] divLenBytes = intToByteArray(divLen);
			
			String temp1 = new String(remLenBytes);
			String temp2 = new String(divLenBytes);
			String temp3 = new String(rem);
			String temp4 = new String(div);
			
			String cat = temp1 + temp2 + temp3 + temp4;
			
			//Uncomment to see the encrypted data
			//System.out.println("Temp4(div):\n" + temp4);
			
			byte[] toWrite = cat.getBytes();
			
			//Uncomment to see the size of the encrypted file.
			//System.out.println("Encrypted file size: " + toWrite.length + " ("+writeOutByteCount+") bytes.");
			
			/**
			 *================================================================
			 *Step 4: Create to new Image.
			 *================================================================
			 */
			
			int currByte = 0;
			int currBit = 8;
			
			int origImgWidth = image.getWidth();
			int origImgHeight= image.getHeight();
			int newType = BufferedImage.TYPE_INT_ARGB;
			BufferedImage finalIm = new BufferedImage(origImgWidth, origImgHeight, newType);
			
			/*
			 * This loop creates the final image.
			 */
			
			for (int row = 0; row < origImgHeight; row++) {
				for (int col = 0; col < origImgWidth; col++) {
					if (currByte < toWrite.length) {
						int currPixelValue = image.getRGB(col, row);
						byte[] currARGB = intToByteArray(currPixelValue);
						
						byte alpha = currARGB[0];
						byte red = currARGB[1];
						byte green= currARGB[2];
						byte blue = currARGB[3];
						
						//alpha
						byte bitToWrite = (toWrite[currByte] & (1<<(currBit-1))) == 0 ? (byte)0 : (byte)1;
						alpha = (byte) (alpha & 0xFE); //1111 1110
						alpha = (byte) (alpha | bitToWrite);
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
						//red
						bitToWrite = (toWrite[currByte] & (1<<(currBit-1))) == 0 ? (byte)0 : (byte)1;
						red = (byte) (red & 0xFE); //1111 1110
						red = (byte) (red | bitToWrite);
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
						//green
						bitToWrite = (toWrite[currByte] & (1<<(currBit-1))) == 0 ? (byte)0 : (byte)1;
						green = (byte) (green & 0xFE); //1111 1110
						green = (byte) (green | bitToWrite);
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
						//blue
						bitToWrite = (toWrite[currByte] & (1<<(currBit-1))) == 0 ? (byte)0 : (byte)1;
						blue = (byte) (blue & 0xFE); //1111 1110
						blue = (byte) (blue | bitToWrite);
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
						
						byte[] newARGB = new byte[] {alpha, red, green, blue};
						int newPixelValue = byteArrayToInt(newARGB);
						finalIm.setRGB(col, row, newPixelValue);
					}
					//If we have written the data, just clear the rest to have 0 in LSB.
					else {
						int currPixelValue = image.getRGB(col, row);
						byte[] currARGB = intToByteArray(currPixelValue);
						
						byte alpha = currARGB[0];
						byte red = currARGB[1];
						byte green= currARGB[2];
						byte blue = currARGB[3];
						
						//alpha
						byte bitToWrite = 0;
						alpha = (byte) (alpha & 0xFE); //1111 1110
						alpha = (byte) (alpha | bitToWrite);
						//red
						bitToWrite = 0;
						red = (byte) (red & 0xFE); //1111 1110
						red = (byte) (red | bitToWrite);
						//green
						bitToWrite = 0;
						green = (byte) (green & 0xFE); //1111 1110
						green = (byte) (green | bitToWrite);
						//blue
						bitToWrite = 0;
						blue = (byte) (blue & 0xFE); //1111 1110
						blue = (byte) (blue | bitToWrite);
						
						byte[] newARGB = new byte[] {alpha, red, green, blue};
						int newPixelValue = byteArrayToInt(newARGB);
						finalIm.setRGB(col, row, newPixelValue);
					}
				}
			}
			
			/**
			 *================================================================
			 *Step 5: Write to new Image.
			 *================================================================
			 */
			
			String oldName = imageFile.getName();
			String newName = "output";
			if (oldName.lastIndexOf('.') != -1) {
				newName = oldName.substring(0, oldName.lastIndexOf('.')) + "-ENCRYPTED" + oldName.substring(oldName.lastIndexOf('.'));
			} else {
				newName = oldName + "-ENCRYPTED.png";
			}
			
			try {
				File newFile = new File ( newName );
				ImageIO.write(finalIm, "png", newFile);
			} catch (IOException e) {
				System.err.println("Cannot write encrypted file.");
				System.exit(-1);
			}
		}
	}
	
	/**
	 * Helper methods.
	 */
	
	public static final byte[] intToByteArray(int value) {
        return new byte[] {
			(byte)(value >>> 24),	//Gets Most significant 8 bits
			(byte)(value >>> 16),	//Gets 2nd Most significant 8 bits
			(byte)(value >>> 8),	//Gets 3rd Most significant 8 bits
			(byte)value				//Gets least significant 8 bits
		};
	}
	
	public static final int byteArrayToInt(byte [] b) {
        return (b[0] << 24)
		+ ((b[1] & 0xFF) << 16)
		+ ((b[2] & 0xFF) << 8)
		+ (b[3] & 0xFF);
	}
}