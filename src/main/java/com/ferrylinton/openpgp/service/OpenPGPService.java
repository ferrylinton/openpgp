package com.ferrylinton.openpgp.service;

import java.io.IOException;
import java.security.NoSuchProviderException;

import org.bouncycastle.openpgp.PGPException;

public interface OpenPGPService {

	String decrypt(String encrypted, String passPhrase) throws IOException, PGPException, NoSuchProviderException;
	
	String encrypt(String clearData, String passPhrase) throws IOException, PGPException, NoSuchProviderException;
	
}
