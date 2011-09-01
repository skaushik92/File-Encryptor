import java.math.BigInteger;

/**
 * The following class is a simple static class that performs one operation.
 * It embeds and writes out an file into an image.
 *
 * The code structure is laid out in this simple manner:
 *
 * globalVariables
 * main
 *  function1
 *      helpersForFunction1
 *  function2
 *      helpersForFunction2
 *   ...
 *
 * where function1 ... functionn are called sequentially in main().
 */

public class Encryptor {
    
    /*
     * Stores the prime number that is used to encrypt the data.
     * This is necessary if the strongEncryption option is chosen.
     */
    
	private static final String SECRET_PRIME_NUMBER = "44444444444444444444444444444444444444444444444444444444444467";
	private static final BigInteger prime = new BigInteger (SECRET_PRIME_NUMBER);

    /*
     * Stores the input image, and is modified in the output() method to store the final output image.
     */
    
	private static byte[][][] image;
	private static int imageWidth;
	private static int imageHeight;
	
    /*
     * Stores the input file.
     */
    
	private static byte[] input;
    
    /*
     * Stores the input file once it has been encrypted.
     */
    
	private static byte[] encryptedInput;
	
    /*
     * Stores image and file data.
     */
    
	private static String inputFileName;
	private static String inputImageName;
	
    /*
     * Encryption flag type.
     */
    
	private static boolean strongEncryption;
    
    /*
     * Stores the encrypted bytes of the file.
     */
    
	private static byte[] remainder;
	private static byte[] quotient;
	
	/**
     * Depending on the arguments provided, different actions occur.
     *
     * If 1 argument is provided, then the necessary image size that could
     * contain the file with that argument name will be displayed.
     *
     * If 2 arguments are provided, then the first argument will be considered
     * the name of the file to be embedded into the image with the name as the second argument.
     * 
     * If 3 arguments are provided, then the first argument will be considered
     * the name of the file to be embedded into the image with the name as the second argument, and strong encryption will be used.
     */
    
	public static void main(String args[]) {
		initialize(args);
		encrypt();
		embed();
		output();
	}
	
    /*
     * Initializes the static variables.
     */
    
	private static void initialize(String[] args) {
		/*
		 * If no arguments are provided,
		 * display the correct usage of this
		 * utility, and then exit.
		 */
		if (args.length == 0) {
			System.err.println("Proper Usage: java FileEncryptor inputFileName [inputPNGImageName [-STR]]");
			System.exit(-1);
		}
		/*
		 * If one argument is provided,
		 * display the number of pixels necessary
		 * to store the information given in the 
		 * input file.
		 */
		else if (args.length == 1) {
			inputFileName = args[0];
			
			input = ByteIO.readFile(inputFileName);
			
			if (input == null)
				System.exit(-1);
            
			calculateNecessaryResources(true);
			System.exit(0);
		}
		
		else if (args.length == 2) {
			inputFileName = args[0];
			inputImageName= args[1];
			strongEncryption = false;
			
			input = ByteIO.readFile(inputFileName);
			image = ByteIO.readImage(inputImageName);
			
			if (input == null || image == null)
				System.exit(-1);
				
			imageHeight= image.length;
			imageWidth = imageHeight == 0 ? 0 : image[0].length;
		}
		
		else if (args.length == 3) {
			inputFileName = args[0];
			inputImageName= args[1];
			
			input = ByteIO.readFile(inputFileName);
			image = ByteIO.readImage(inputImageName);
			
			if (input == null || image == null)
				System.exit(-1);
				
			imageHeight= image.length;
			imageWidth = imageHeight == 0 ? 0 : image[0].length;
			
			if (args[2].equals("-STR"))
				strongEncryption = true;
		}
		
		else {
			System.err.println("Proper Usage: java FileEncryptor inputFileName [inputPNGImageName [-STR]]");
			System.exit(-1);
		}
	}
    
    /*
     * Calculates and stores some values in the global variables.
     * Information can optionally be printed out; (e.g. in the case of 1 argument).
     */
	
	private static int[] calculateNecessaryResources(boolean printOut) {
		
		int fileByteLength = input.length;
		int fileNameByteLength = inputFileName.getBytes().length;
		
		//4 for fileNameByteLength + 4 for fileByteLength + 1 for simpleEncryption signature
		int simpleEncryptionNumBytes = 1 + 4 + 4 + fileNameByteLength + fileByteLength;
		
		//Strong Encryption Calculation
		BigInteger fileAsNumber = new BigInteger (input);
		BigInteger[] quotientAndRemainder = fileAsNumber.divideAndRemainder(prime);
		
		BigInteger qtnt = quotientAndRemainder[0];
		BigInteger rmndr= quotientAndRemainder[1];
		
		//Stores into global variables
		remainder = rmndr.toByteArray();
		quotient = qtnt.toByteArray();
		
		int rmndrBytesLen= remainder.length;
		int qtntBytesLen = quotient.length;
		
		//4 for fileNameByteLength + 4 for rmndrBytesLen + 4 for qtntBytesLen + 1 for strongEncryption signature
		int strongEncryptionNumBytes = 1 + 4 + 4 + 4 + fileNameByteLength + rmndrBytesLen + qtntBytesLen;
		
		int simpleEncryptionNumPixels= 2 * simpleEncryptionNumBytes;
		int strongEncryptionNumPixels= 2 * strongEncryptionNumBytes;
		
		int[] simpleFactors = largestFactors(simpleEncryptionNumPixels);
		int[] strongFactors = largestFactors(strongEncryptionNumPixels);
		
		if (printOut) {
			System.out.println("For simple encryption of the file \""+inputFileName+"\", " + simpleEncryptionNumPixels + " pixels are needed. That means it requires at least a " + simpleFactors[0] + " x " + simpleFactors[1] + " image.");
			System.out.println("For strong encryption of the file \""+inputFileName+"\", " + strongEncryptionNumPixels + " pixels are needed. That means it requires at least a " + strongFactors[0] + " x " + strongFactors[1] + " image.");
			System.out.println("The benefit of strong encryption is that the binary information stored on the last bit of each channel of each pixel is not the actual binary information of the file, but rather an output of a function. The inverse of the function will be taken when the file is decrypted.");		
		}
		return new int[] {simpleEncryptionNumPixels, strongEncryptionNumPixels};
	}
    
	private static int[] largestFactors ( int num ) {
		int sqrt = (int)Math.sqrt(num);
		
		while (sqrt > 1 && Math.abs(sqrt - num/sqrt) < Math.sqrt(num) * .1) {
			if (num % sqrt == 0)
				return new int [] {sqrt, num / sqrt};
			else
				sqrt--;
		}
		return largestFactors ( num + 1 );
	}
	
    /*
     * Encrypts the input file by either using simple encryption or strong encryption.
     * They are detailed below:
     *
     * Simple encryption:
     * 1. The data file's name, and byte-data are stored directly into the image.
     * 2. Header data, including the lengths of the file and filename, are provided.
     * Strong encryption:
     * 1. The data file's data is considered to be one integer, and the number is divided by a prime
     * and the quotient, and remainder, are given.
     * 2. The header data, is generated to indicate the positions of the info in the byte array.
	 */
    
	private static void encrypt() {
		int[] requiredPixels = calculateNecessaryResources(false);
		
		if (strongEncryption) {
			if (imageWidth*imageHeight < requiredPixels[1]) {
				System.err.println("For strong encryption of the file \""+inputFileName+"\", " + requiredPixels[1] + " pixels are needed.");
				System.err.println("Currently, only " + imageWidth*imageHeight + " (" + imageWidth + "x" + imageHeight + ") pixels are available.");
				System.exit(-1);
			}
			else {
				encryptedInput = new byte[requiredPixels[1]];
				int nextFreeIndex = 0;
				encryptedInput[0] = 1;
				nextFreeIndex++;
				writeIntWithOffset(encryptedInput, nextFreeIndex, inputFileName.getBytes().length);
				nextFreeIndex+=4;
				writeIntWithOffset(encryptedInput, nextFreeIndex, remainder.length);
				nextFreeIndex+=4;
				writeIntWithOffset(encryptedInput, nextFreeIndex, quotient.length);
				nextFreeIndex+=4;
				writeByteArrayWithOffset(encryptedInput, nextFreeIndex, inputFileName.getBytes());
				nextFreeIndex+=inputFileName.getBytes().length;
				writeByteArrayWithOffset(encryptedInput, nextFreeIndex, remainder);
				nextFreeIndex+=remainder.length;
				writeByteArrayWithOffset(encryptedInput, nextFreeIndex, quotient);
				nextFreeIndex+=quotient.length;
			}
		}
		else {
			if (imageWidth*imageHeight < requiredPixels[0]) {
				System.err.println("For strong encryption of the file \""+inputFileName+"\", " + requiredPixels[0] + " pixels are needed.");
				System.err.println("Currently, only " + imageWidth*imageHeight + " (" + imageWidth + "x" + imageHeight + ") pixels are available.");
				System.exit(-1);
			}
			else {
				encryptedInput = new byte[requiredPixels[0]];
				int nextFreeIndex = 0;
				encryptedInput[0] = 0;
				nextFreeIndex++;
				writeIntWithOffset(encryptedInput, nextFreeIndex, inputFileName.getBytes().length);
				nextFreeIndex+=4;
				writeIntWithOffset(encryptedInput, nextFreeIndex, input.length);
				nextFreeIndex+=4;
				writeByteArrayWithOffset(encryptedInput, nextFreeIndex, inputFileName.getBytes());
				nextFreeIndex+=inputFileName.getBytes().length;
				writeByteArrayWithOffset(encryptedInput, nextFreeIndex, input);
				nextFreeIndex+=input.length;
			}
		}
	}
    
    /*
     * Helper methods that do simple byte array operations.
     * The first method stores an integer into a byte array.
     * The second method copies an array into another array.
     */
	
    private static void writeIntWithOffset(byte[] array, int offset, int value) {
		byte byte4 =(byte) (value >>> 24);
		byte byte3 =(byte) (value >>> 16);
		byte byte2 =(byte) (value >>> 8);
		byte byte1 =(byte) (value);
		
		array[offset] = byte4;
		array[offset+1]= byte3;
		array[offset+2]= byte2;
		array[offset+3]= byte1;
	}
	
	private static void writeByteArrayWithOffset(byte[] to, int offset, byte[] from) {
		for (int i = 0; i < from.length; i++) {
			to[offset+i] = from[i];
		}
	}
    
    /*
     * Modifies the images to include the output generated by the encrypt() method.
     *
     * Each channel of each pixel can contain 1 bit of data. This embeds the file information
     * into the image by writing out bytes to multiple pixels.
     */
	
	private static void embed() {
		for (int row = 0; row < imageHeight; row++) {
			for (int col = 0; col < imageWidth; col++) {
				for (int channel = 0; channel < 4; channel++) {
					image[row][col][channel] = 
					(byte)((image[row][col][channel] & 0xFE) | (getNextBit() ? 1 : 0));
				}
			}
		}
	}
	
    /*
     * Helper methods for embedding, which retrieve the correct bit
     * for each pixel-channel to store.
     */
    
	private static int nextByte = 0;
	private static int nextBit = 8;
	
	private static boolean getNextBit() {
		if (nextByte >= encryptedInput.length) {
			return false;
		}
		
		byte currByte = encryptedInput[nextByte];
		
		boolean currBit = ((currByte >> (nextBit-1)) & 1) == 0 ? false : true;
		
		nextBit--;
		if (nextBit == 0) {
			nextByte++;
			nextBit = 8;
		}
		return currBit;
	}
	
    /*
     * Calls the ByteIO method to write out the new modified image.
     */
    
	private static void output() {
		String encryptTag = strongEncryption ? "-STR-ENC" : "-ENC";
		
		int lastDot = inputImageName.lastIndexOf('.');
		String newName = lastDot == -1 ? inputImageName + encryptTag + ".png" : 
										 inputImageName.substring(0, lastDot) + "-" + inputFileName + encryptTag + inputImageName.substring(lastDot);
		
		ByteIO.writeImage(newName, image);
	}
}