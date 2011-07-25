import java.math.BigInteger;

public class Decryptor {
    
    /*
     * Stores the prime number that is used to decrypt the data.
     * This is necessary if the strongEncryption option is read.
     */
    
	private static final String SECRET_PRIME_NUMBER = "44444444444444444444444444444444444444444444444444444444444467";
	private static final BigInteger prime = new BigInteger (SECRET_PRIME_NUMBER);

    /*
     * Stores the input image, which contains the input data file embedded in the pixels.
     */
    
	private static byte[][][] image;
	private static int imageWidth;
	private static int imageHeight;
	
    /*
     * Stores the read-in input.
     */
    
	private static byte[] input;
    
    /*
     * Stores the decrypted input, without any header information and contains the exact file.
     */
    
	private static byte[] decryptedInput;
	
	private static String inputFileName;
	
	
	public static void main(String args[]) {
		initialize(args);
		loadBytes();
		decrypt();
		output();
	}
    
    /**
     * Depending on the arguments provided, different actions occur.
     *
     * If 1 argument is provided, then the image that corresponds to the
     * file with the name of the first argument is read in and converted to a file.
     */
    
	private static void initialize(String[] args) {
        
		/*
		 * If no arguments are provided,
		 * display the correct usage of this
		 * utility, and then exit.
		 */
        
		if (args.length != 1) {
			System.err.println("Proper Usage: java FileDecryptor inputFileName");
			System.exit(-1);
		}
        
		/*
		 * If one argument is provided,
		 * display the number of pixels necessary
		 * to store the information given in the 
		 * input file.
		 */
        
		if (args.length == 1) {
			String imageFileName = args[0];
			
			image = ByteIO.readImage(imageFileName);
			
			imageHeight= image.length;
			imageWidth = imageHeight == 0 ? 0 : image[0].length;
			
			input = new byte[imageHeight*imageWidth];
		}
	}
	
    /*
     * Reads the image and takes one bit at a time to read the bytes that are encoded into the image.
     */
    
	private static void loadBytes() {
		for (int row = 0; row < imageHeight; row++) {
			for (int col = 0; col < imageWidth; col++) {
				for (int channel = 0; channel < 4; channel++) {
					setNextBit((image[row][col][channel] & 0x01) == 1);
				}
			}
		}
	}
	
    /*
     * Helper methods for loadBytes()
     */
    
	private static int nextByte = 0;
	private static int nextBit = 8;
	
	private static void setNextBit(boolean bit) {
		if (bit) {
			byte currByte = input[nextByte];
			currByte = (byte) (currByte | (1 << (nextBit - 1)));
			input[nextByte]=currByte;
		}
		
		nextBit--;
		if (nextBit==0) {
			nextByte++;
			nextBit=8;
		}
	}
	
    /*
     * Decrypts the input.
     */
    
	private static void decrypt() {
		//If strong encryption
		int currOffset = 0;
		if (input[0] == 1) {
			currOffset++;
			int numBytesForFileName = getIntWithOffset(input, currOffset);
			currOffset+=4;
			int numBytesForRemainder= getIntWithOffset(input, currOffset);
			currOffset+=4;
			int numBytesForQuotient = getIntWithOffset(input, currOffset);
			currOffset+=4;
			
			byte[] fileName = new byte[numBytesForFileName];
			byte[] remainder = new byte[numBytesForRemainder];
			byte[] quotient = new byte[numBytesForQuotient];
			writeByteArrayWithOffsetAndLength(fileName, currOffset, input, numBytesForFileName);
			currOffset+=numBytesForFileName;
			writeByteArrayWithOffsetAndLength(remainder, currOffset, input, numBytesForRemainder);
			currOffset+=numBytesForRemainder;
			writeByteArrayWithOffsetAndLength(quotient, currOffset, input, numBytesForQuotient);
			currOffset+=numBytesForQuotient;
			inputFileName = new String(fileName);
			BigInteger origFile = prime.multiply( new BigInteger(quotient) ).add( new BigInteger(remainder) );
			decryptedInput = origFile.toByteArray();
			
		}
		else {
			currOffset++;
			int numBytesForFileName = getIntWithOffset(input, currOffset);
			currOffset+=4;
			int numBytesForData= getIntWithOffset(input, currOffset);
			currOffset+=4;
			byte[] fileName = new byte[numBytesForFileName];
			decryptedInput = new byte[numBytesForData];
			
			writeByteArrayWithOffsetAndLength(fileName, currOffset, input, numBytesForFileName);
			currOffset+=numBytesForFileName;
			writeByteArrayWithOffsetAndLength(decryptedInput, currOffset, input, numBytesForData);
			currOffset+=numBytesForData;
			
			inputFileName = new String(fileName);
		}
	}

    /*
     * Helper methods for decrypt()
     */
    
	private static final int getIntWithOffset(byte [] from, int offset) {
        return (from[0+offset] << 24)
		+ ((from[1+offset] & 0xFF) << 16)
		+ ((from[2+offset] & 0xFF) << 8)
		+ (from[3+offset] & 0xFF);
	}
	
	private static void writeByteArrayWithOffsetAndLength(byte[] to, int offset, byte[] from, int length) {
		for (int i = 0; i < length; i++) {
			to[i] = from[offset+i];
		}
	}
	
	private static void output() {
		ByteIO.writeFile(inputFileName, decryptedInput);
	}
}