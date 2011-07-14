import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.IOException;

import java.math.BigInteger;

import java.util.Arrays;

import javax.imageio.ImageIO;

public class FileDecryptor {
	
	public static final String CORRECT_USAGE = "java FileDecryptor inputFileName [decryptedFileName]";
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
			String inputImageName= args[0];
			
			File imageFile = new File (inputImageName);
			
			/**
			 *================================================================
			 *Step 3: Read in Image.
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
			 *Step 4: Pull Bytes from Image.
			 *================================================================
			 */
			
			int origImgWidth = image.getWidth();
			int origImgHeight= image.getHeight();
			/*
			 * To decrypt we must first take the bits and put it in a bitstream.
			 */
			byte[] input = new byte[origImgWidth*origImgWidth];
			int currByte = 0;
			int currBit = 8;
			
			int upperbound = Integer.MAX_VALUE;
			
			for (int row = 0; row < origImgHeight; row++) {
				for (int col = 0; col < origImgWidth; col++) {
					if (row*origImgWidth + col < upperbound) {
						int currPixelValue = image.getRGB(col, row);
						byte[] currARGB = intToByteArray(currPixelValue);
						
						byte alpha = currARGB[0];
						byte red = currARGB[1];
						byte green= currARGB[2];
						byte blue = currARGB[3];
						
						//alpha
						alpha = (byte) (alpha & 0x01); //0000 0001
						byte byteToEdit = input[currByte];
						alpha = (byte) (alpha << (currBit - 1));
						byteToEdit = (byte) (byteToEdit | alpha);
						input[currByte] = byteToEdit;
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
						//red
						red = (byte) (red & 0x01); //0000 0001
						byteToEdit = input[currByte];
						red = (byte) (red << (currBit - 1));
						byteToEdit = (byte) (byteToEdit | red);
						input[currByte] = byteToEdit;
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
						//green
						green = (byte) (green & 0x01); //0000 0001
						byteToEdit = input[currByte];
						green = (byte) (green << (currBit - 1));
						byteToEdit = (byte) (byteToEdit | green);
						input[currByte] = byteToEdit;
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
						//blue
						blue = (byte) (blue & 0x01); //0000 0001
						byteToEdit = input[currByte];
						blue = (byte) (blue << (currBit - 1));
						byteToEdit = (byte) (byteToEdit | blue);
						input[currByte] = byteToEdit;
						currBit--;
						if (currBit == 0) {
							currBit = 8;
							currByte++;
						}
					}
				}
			}
			
			/**
			 *================================================================
			 *Step 5: Decrypt Bytes.
			 *================================================================
			 */
			
			//First 4 bytes show remainder
			
			byte [] remLenBytes = Arrays.copyOfRange (input, 0, 4);
			byte [] divLenBytes = Arrays.copyOfRange (input, 4, 8);
			
			int remLen = byteArrayToInt(remLenBytes);
			int divLen = byteArrayToInt(divLenBytes);
			
			//Uncomment to see the remainder and dividend lengths
			//System.out.println("Rem: " + remLen);
			//System.out.println("Div: " + divLen);
			
			byte [] rem = Arrays.copyOfRange(input, 8, 8 + remLen);
			byte [] div = Arrays.copyOfRange(input, 8 + remLen, 8 + remLen + divLen);
			
			//Uncomment to see the actual encrypted bytes
			//System.out.println("(div):\n" + new String(div));
			int totalByteLength = 4 + 4 + remLen + divLen;
			
			//Uncomment to see the size of the encrypted file.
			//System.out.println("Encrypted file size: "+totalByteLength+" bytes.");

			BigInteger primeNumber = new BigInteger ("52428" + "7");
			BigInteger dividend = new BigInteger(div);
			BigInteger remainder= new BigInteger(rem);
			
			//Uncomment to see the dividend and remainder representation of file
			//System.out.println("Dividend: " + dividend);
			//System.out.println("Remainder: "+ remainder);
			
			BigInteger fileAsNum = dividend.multiply(primeNumber).add(remainder);
			
			byte[] fileInBytes = fileAsNum.toByteArray();
			
			
			//Uncomment to see the size of the encrypted file.
			//System.out.println("Encrypted file size: "+fileInBytes.length+" ("+totalByteLength+") bytes.");
			
			/**
			 *================================================================
			 *Step 6: Write out File.
			 *================================================================
			 */
			
			String decryptFileName = "decrypted";
			if (args.length >= 2) {
				decryptFileName = args[1];
			}
			try {
				File decrypted = new File (decryptFileName);
				FileOutputStream writeOut = new FileOutputStream(decrypted, false);
				writeOut.write(fileInBytes);
				writeOut.close();
			}
			catch (IOException e) {
				System.err.println("Could not output the decrypted file.");
				System.err.println(e.getMessage());
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