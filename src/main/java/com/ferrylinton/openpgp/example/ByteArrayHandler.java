package com.ferrylinton.openpgp.example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPBEEncryptedData;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBEDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBEKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.util.io.Streams;

/**
 * Simple routine to encrypt and decrypt using a passphrase. This service
 * routine provides the basic PGP services between byte arrays.
 * 
 * Note: this code plays no attention to -CONSOLE in the file name the
 * specification of "_CONSOLE" in the filename. It also expects that a single
 * pass phrase will have been used.
 * 
 */
public class ByteArrayHandler {
	/**
	 * decrypt the passed in message stream
	 * 
	 * @param encrypted
	 *            The message to be decrypted.
	 * @param passPhrase
	 *            Pass phrase (key)
	 * 
	 * @return Clear text as a byte array. I18N considerations are not handled
	 *         by this routine
	 * @exception IOException
	 * @exception PGPException
	 * @exception NoSuchProviderException
	 */
	public static byte[] decrypt(byte[] encrypted, char[] passPhrase)
			throws IOException, PGPException, NoSuchProviderException {
		InputStream in = new ByteArrayInputStream(encrypted);

		in = PGPUtil.getDecoderStream(in);
		

		JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
		PGPEncryptedDataList enc;
		Object o = pgpF.nextObject();

		//
		// the first object might be a PGP marker packet.
		//
		if (o instanceof PGPEncryptedDataList) {
			enc = (PGPEncryptedDataList) o;
		} else {
			enc = (PGPEncryptedDataList) pgpF.nextObject();
		}

		PGPPBEEncryptedData pbe = (PGPPBEEncryptedData) enc.get(0);

		InputStream clear = pbe.getDataStream(new JcePBEDataDecryptorFactoryBuilder(
				new JcaPGPDigestCalculatorProviderBuilder().setProvider("BC").build()).setProvider("BC")
						.build(passPhrase));

		JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(clear);
		
		Object message = pgpFact.nextObject();
		if (message instanceof PGPCompressedData) {
			pgpFact = new JcaPGPObjectFactory(((PGPCompressedData) message).getDataStream());

			PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();

			return Streams.readAll(ld.getInputStream());
		}else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PGPLiteralData pgpLiteralData = (PGPLiteralData) message;

			int ch;
			while ((ch = pgpLiteralData.getInputStream().read()) >= 0) {
				out.write(ch);
			}
			
			return new String(out.toByteArray(), "UTF-8").getBytes();
		}
	}

	/**
	 * Simple PGP encryptor between byte[].
	 * 
	 * @param clearData
	 *            The test to be encrypted
	 * @param passPhrase
	 *            The pass phrase (key). This method assumes that the key is a
	 *            simple pass phrase, and does not yet support RSA or more
	 *            sophisiticated keying.
	 * @param fileName
	 *            File name. This is used in the Literal Data Packet (tag 11)
	 *            which is really inly important if the data is to be related to
	 *            a file to be recovered later. Because this routine does not
	 *            know the source of the information, the caller can set
	 *            something here for file name use that will be carried. If this
	 *            routine is being used to encrypt SOAP MIME bodies, for
	 *            example, use the file name from the MIME type, if applicable.
	 *            Or anything else appropriate.
	 * 
	 * @param armor
	 * 
	 * @return encrypted data.
	 * @exception IOException
	 * @exception PGPException
	 * @exception NoSuchProviderException
	 */
	public static byte[] encrypt(byte[] clearData, char[] passPhrase, int algorithm, boolean armor)
			throws IOException, PGPException, NoSuchProviderException {
		String fileName = PGPLiteralData.CONSOLE;

		byte[] compressedData = compress(clearData, fileName, CompressionAlgorithmTags.ZIP);

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		OutputStream out = bOut;
		if (armor) {
			out = new ArmoredOutputStream(out);
		}

		PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
				new JcePGPDataEncryptorBuilder(algorithm).setSecureRandom(new SecureRandom()).setProvider("BC"));
		encGen.addMethod(new JcePBEKeyEncryptionMethodGenerator(passPhrase).setProvider("BC"));

		OutputStream encOut = encGen.open(out, compressedData.length);

		encOut.write(compressedData);
		encOut.close();

		if (armor) {
			out.close();
		}

		return bOut.toByteArray();
	}

	private static byte[] compress(byte[] clearData, String fileName, int algorithm) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(algorithm);
		OutputStream cos = comData.open(bOut); // open it with the final
												// destination

		PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();

		// we want to generate compressed data. This might be a user option
		// later,
		// in which case we would pass in bOut.
		OutputStream pOut = lData.open(cos, // the compressed output stream
				PGPLiteralData.UTF8, fileName, // "filename" to store
				clearData.length, // length of clear data
				new Date() // current time
		);

		pOut.write(clearData);
		pOut.close();

		comData.close();

		return bOut.toByteArray();
	}

	public static void main(String[] args) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		String passPhrase = "password";
		char[] passArray = passPhrase.toCharArray();

		byte[] original = "Hello world".getBytes();
		System.out.println("Starting PGP test");
		byte[] encrypted = encrypt(original, passArray, PGPEncryptedDataGenerator.CAST5, false);

		System.out.println("\nencrypted data = '" + new String(org.bouncycastle.util.encoders.Hex.encode(encrypted)) + "'");

		
		String temp = "-----BEGIN PGP MESSAGE-----\n"
				+ "Version: BCPG v1.57\n\n"
				+ "jA0EAwMC+57T1rKqjN9gySfNql/rHZSOAZdJq4pnn+itkf03l+MpCH2Oz4XaL0Dp\n"
				+ "KA43vYVa/0o=\n"
				+ "=z3rh\n"
				+ "-----END PGP MESSAGE-----";
		
		String temp2 = "-----BEGIN PGP MESSAGE-----\n"
				+ "Version: OpenPGP.js v2.5.6\n"
				+ "Comment: https://openpgpjs.org\n\n"
				+ "wy4ECQMItudmIxIY2oxgWiN/IhdNvmvDGLjAa+KnKnn0/6wGCcDDCFWhkZFj\n"
				+ "aVO10k0BvuoNJpjboJZwsPPr5nigBs3TMSouv6nfvqm30YYa2qbnDApR1aZm\n"
				+ "l9fGCMxQ9kphFHTR5rzL7xJZzK8qVKSCD18K2y6ui/HBPl2nAg==\n"
				+ "=GnLs\n"
				+ "-----END PGP MESSAGE-----";
		
		byte[] decrypted = decrypt(temp2.getBytes(), passArray);

		System.out.println("\ndecrypted data = '" + new String(decrypted) + "'");

		encrypted = encrypt(original, passArray, PGPEncryptedDataGenerator.AES_256, false);

		System.out.println(
				"\nencrypted data = '" + new String(org.bouncycastle.util.encoders.Hex.encode(encrypted)) + "'");
		decrypted = decrypt(encrypted, passArray);

		System.out.println("\ndecrypted data = '" + new String(decrypted) + "'");
	}
}
