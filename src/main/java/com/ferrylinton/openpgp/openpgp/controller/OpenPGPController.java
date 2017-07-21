package com.ferrylinton.openpgp.openpgp.controller;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.validation.Valid;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ferrylinton.openpgp.openpgp.dto.Result;
import com.ferrylinton.openpgp.openpgp.dto.SimpleOpenPGP;
import com.ferrylinton.openpgp.service.OpenPGPService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class OpenPGPController {

	@Autowired
	private OpenPGPService openPGPService;
	
	@RequestMapping(value="/simple/encrypt", method = RequestMethod.POST)
	public ResponseEntity<Object> encrypt(@Valid @RequestBody SimpleOpenPGP input){
		Security.addProvider(new BouncyCastleProvider());
		try {
			String result = openPGPService.encrypt(input.getData(), input.getPassPhrase());
			return new ResponseEntity<>(new Result("OK", result), HttpStatus.OK);
		} catch (NoSuchProviderException | IOException | PGPException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(new Result("ERROR", e.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value="/simple/decrypt", method = RequestMethod.POST)
	public ResponseEntity<Object> decrypt(@Valid @RequestBody SimpleOpenPGP input){
		Security.addProvider(new BouncyCastleProvider());
		try {
			String result = openPGPService.decrypt(input.getData(), input.getPassPhrase());
			return new ResponseEntity<>(new Result("OK", result), HttpStatus.OK);
		} catch (NoSuchProviderException | IOException | PGPException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(new Result("ERROR", e.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
