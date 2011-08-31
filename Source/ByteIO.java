import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Author: Kaushik Shankar
 *
 * IO class handles image and file IO at the byte level.
 */

public class ByteIO {
	
	/**
	 * Provide a filename, get back the bytes that represent that file on disk.
	 */
	
	public static byte[] readFile(String fileName) {
		try {
			File inputFile = new File (fileName);
			FileInputStream inputStream = new FileInputStream(inputFile);
			
			int length = (int) inputFile.length();
			
			byte[] fileInBytes = new byte[length];
			
			int bytesRead = inputStream.read(fileInBytes, 0, length);
			
			//If the file could not be fully read.
			if (bytesRead != length) {
				System.err.println("\"" + fileName + "\" could not be read completely!");
				return null;
			}
			
			return fileInBytes;
		} catch (FileNotFoundException e) {
			System.err.println("\"" + fileName + "\" not found!");
			return null;
		} catch (IOException e) {
			System.err.println("\"" + fileName + "\" could not be read!");
			return null;
		}
	}
	
	/**
	 * Provide a filename of an image, get back a grid of pixels.
	 * Each pixel contains 4 bytes of data (1 for each channel).
	 */
	
	public static byte[][][] readImage(String imageName) {
		try {
			File imageFile = new File(imageName);
			BufferedImage inputImage = ImageIO.read(imageFile);
			
			int width = inputImage.getWidth();
			int height= inputImage.getHeight();
			
			byte[][][] imageInBytes = new byte[height][width][4];
			
			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					//Going left-right then next line (1 down)
					int pixel = inputImage.getRGB(col, row);
					
					for (int channel = 0; channel < 4; channel++)
						imageInBytes[row][col][channel] = (byte) (pixel >>> (24 - channel*8));
				}
			}
			
			return imageInBytes;
		} catch (IOException e) {
			System.err.println("\""+imageName+"\" could not be read!");
			return null;
		}
	}
	
	/**
	 * Provide a filename for the new file that is to be written
	 * with the provided byte data.
	 */
	
	public static boolean writeFile(String fileName, byte[] fileData) {
		try {
			File outputFile = new File (fileName);
			FileOutputStream fileToWrite = new FileOutputStream(outputFile, false);
			fileToWrite.write(fileData);
			fileToWrite.close();
			return true;
		}
		catch (IOException e) {
			System.err.println("\""+fileName+"\" could not be written!");
			return false;
		}
	}
	
	/**
	 * Provide a filename for the new image that is to be written
	 * with the provided image data.
	 */
	
	public static boolean writeImage(String imageName, byte[][][] imageData) {
		try {
			int height= imageData.length;
			int width = height == 0 ? 0 : imageData[0].length;
			
			File imageFile = new File ( imageName );
			BufferedImage imageToWrite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			
			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					//Going left-right then next line (1 down)
					int pixel = 0;
					for (int channel = 0; channel < 4; channel++) {
						pixel += (imageData[row][col][channel] & 0xFF) << (24 - channel*8);
					}
					
					imageToWrite.setRGB(col, row, pixel);
				}
			}
			
			int lastDot = imageName.lastIndexOf('.');
			String fileType = lastDot == -1 ? "png" : imageName.substring(lastDot + 1);
			
			ImageIO.write(imageToWrite, fileType, imageFile);
			return true;
		} catch (IOException e) {
			System.err.println("\""+imageName+"\" could not be written!");
			return false;
		}
	}
}