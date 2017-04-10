import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by arunan on 4/8/17.
 */
public class ProductCipher {


	private int[] keyPhrases = new int[4];


	public ProductCipher(byte[] key) {
		if (key == null) {
			System.out.println("Enter a key");
			System.exit(0);
		}
		if (key.length < 16){
			System.out.println("Enter a valid key with 128 bits");
			System.exit(0);
		}

		for (int m=0, i=0; i<4; i++) {
			keyPhrases[i] = ((key[m++] & 0xff)) | ((key[m++] & 0xff) <<  8) | ((key[m++] & 0xff) << 16) | ((key[m++] & 0xff) << 24);
		}
	}

	public byte[] encrypt(byte[] stringToEncrypt) {
		int stringLength = ((stringToEncrypt.length/8) + (((stringToEncrypt.length%8)==0)?0:1)) * 2;
		int[] buffer = new int[stringLength + 1];
		buffer[0] = stringToEncrypt.length;
		pack(stringToEncrypt, buffer, 1);
		encryptAlgo(buffer);
		return unpack(buffer, 0, buffer.length * 4);
	}

	public byte[] decrypt(byte[] encryptedText) {
		assert encryptedText.length % 4 == 0;
		assert (encryptedText.length / 4) % 2 == 1;
		int[] buffer = new int[encryptedText.length / 4];
		pack(encryptedText, buffer, 0);
		decryptAlgo(buffer);
		return unpack(buffer, 1, buffer[0]);
	}

	private void encryptAlgo(int[] buf) {
		assert buf.length % 2 == 1;
		int i, v0, v1, sum, n;
		i = 1;
		while (i<buf.length) {
			n = 32;
			v0 = buf[i];
			v1 = buf[i+1];
			sum = 0;
			while (n-->0) {
				sum += delta;
				v0  += ((v1 << 4 ) + keyPhrases[0] ^ v1) + (sum ^ (v1 >>> 5)) + keyPhrases[1];
				v1  += ((v0 << 4 ) + keyPhrases[2] ^ v0) + (sum ^ (v0 >>> 5)) + keyPhrases[3];
			}
			buf[i] = v0;
			buf[i+1] = v1;
			i+=2;
		}
	}

	private void decryptAlgo(int[] buf) {
		assert buf.length % 2 == 1;
		int i, v0, v1, sum, n;
		i = 1;
		while (i<buf.length) {
			n = 32;
			v0 = buf[i];
			v1 = buf[i+1];
			sum = unDelta;
			while (n--> 0) {
				v1  -= ((v0 << 4 ) + keyPhrases[2] ^ v0) + (sum ^ (v0 >>> 5)) + keyPhrases[3];
				v0  -= ((v1 << 4 ) + keyPhrases[0] ^ v1) + (sum ^ (v1 >>> 5)) + keyPhrases[1];
				sum -= delta;
			}
			buf[i] = v0;
			buf[i+1] = v1;
			i+=2;
		}
	}

	private void pack(byte[] src, int[] dest, int destOffset) {
		assert destOffset + (src.length / 4) <= dest.length;
		int i = 0, shift = 24;
		int j = destOffset;
		dest[j] = 0;
		while (i<src.length) {
			dest[j] |= ((src[i] & 0xff) << shift);
			if (shift==0) {
				shift = 24;
				j++;
				if (j<dest.length) dest[j] = 0;
			}
			else {
				shift -= 8;
			}
			i++;
		}
	}

	private byte[] unpack(int[] src, int sOffset, int desLength) {
		assert desLength <= (src.length - sOffset) * 4;
		byte[] dest = new byte[desLength];
		int i = sOffset;
		int count = 0;
		for (int j = 0; j < desLength; j++) {
			dest[j] = (byte) ((src[i] >> (24 - (8*count))) & 0xff);
			count++;
			if (count == 4) {
				count = 0;
				i++;
			}
		}
		return dest;
	}

	private final static int delta = 0x9E3779B9;
	private final static int unDelta = 0xC6EF3720;

	public static void main(String[] args) {

		String substitutionInput = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader("input.txt"));

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			substitutionInput = sb.toString();
			br.close();
		} catch (Exception e) {
			System.out.println("File \"input.txt\" not found");
			e.printStackTrace();
		}
		System.out.println("-------------Text read from file----------------\n");
		System.out.println(substitutionInput);

		ProductCipher pC = new ProductCipher("My Index Number is 140045E".getBytes());

		byte[] fileString = substitutionInput.getBytes();

		byte[] encryptedText = pC.encrypt(fileString);

		System.out.println("-------------Text after encryption----------------\n");
		System.out.println(new String(encryptedText));

		System.out.println("-------------Text after decryption----------------\n");
		byte[] decryptedText = pC.decrypt(encryptedText);
		System.out.println(new String(decryptedText));

		String test = new String(decryptedText);
		if (!test.equals(substitutionInput))
			throw new RuntimeException("Fail");
	}
}
