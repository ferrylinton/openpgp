package com.ferrylinton.openpgp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPBEEncryptedData;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBEDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBEKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.util.io.Streams;
import org.springframework.stereotype.Service;

import com.ferrylinton.openpgp.service.OpenPGPService;

@Service
public class OpenPGPServiceImpl implements OpenPGPService{

	@Override
	public String decrypt(String encrypted, String passPhrase) throws IOException, PGPException, NoSuchProviderException {
		InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(encrypted.getBytes()));

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
						.build(passPhrase.toCharArray()));

		JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(clear);

		Object message = pgpFact.nextObject();
		if (message instanceof PGPCompressedData) {
			pgpFact = new JcaPGPObjectFactory(((PGPCompressedData) message).getDataStream());

			PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();

			return new String(Streams.readAll(ld.getInputStream()), "UTF-8");
		} else {
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			PGPLiteralData pgpLiteralData = (PGPLiteralData) message;

			int ch;
			while ((ch = pgpLiteralData.getInputStream().read()) >= 0) {
				bOut.write(ch);
			}

			return new String(bOut.toByteArray(), "UTF-8");
		}
	}

	@Override
	public String encrypt(String clearData, String passPhrase) throws IOException, PGPException, NoSuchProviderException {
		String fileName = PGPLiteralData.CONSOLE;

		byte[] compressedData = compress(clearData.getBytes(), fileName, CompressionAlgorithmTags.ZIP);

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		OutputStream out = new ArmoredOutputStream(bOut);;

		PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(PGPEncryptedDataGenerator.CAST5).setSecureRandom(new SecureRandom()).setProvider("BC"));
		encGen.addMethod(new JcePBEKeyEncryptionMethodGenerator(passPhrase.toCharArray()).setProvider("BC"));

		OutputStream encOut = encGen.open(out, compressedData.length);

		encOut.write(compressedData);
		encOut.close();
		out.close();

		return new String(bOut.toByteArray(), "UTF-8");
	}

	private byte[] compress(byte[] clearData, String fileName, int algorithm) throws IOException {
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
}
