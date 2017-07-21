package com.ferrylinton.openpgp.openpgp.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class SimpleOpenPGP {

	@NotNull
	@NotEmpty
	private String data;
	
	@NotNull
	@NotEmpty
	private String passPhrase;
	
}
