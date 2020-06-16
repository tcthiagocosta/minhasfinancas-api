package com.minhasfinancas.minhasfinancas.exeception;

public class ErroAutenticacao extends RuntimeException {

	public ErroAutenticacao(String msg) {
		super(msg);
	}
}
