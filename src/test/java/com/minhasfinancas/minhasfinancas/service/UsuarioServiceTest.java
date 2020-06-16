package com.minhasfinancas.minhasfinancas.service;


import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.minhasfinancas.minhasfinancas.exeception.ErroAutenticacao;
import com.minhasfinancas.minhasfinancas.exeception.RegraNegocioException;
import com.minhasfinancas.minhasfinancas.model.entity.Usuario;
import com.minhasfinancas.minhasfinancas.model.repository.UsuarioRepository;
import com.minhasfinancas.minhasfinancas.service.impl.UsuarioServiceImpl;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@SpyBean
	UsuarioServiceImpl usuarioService;
	
	@MockBean
	UsuarioRepository repository;
	
	@org.junit.Test(expected = org.junit.Test.None.class)
	public void deveAutenticarUmUsuarioCOmSUcesso() {
		// cenário
		String email = "email@email.com";
		String senha  = "Senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
		
		// ação
		Usuario result = usuarioService.autenticar(email, senha);
		
		// verificação
		Assertions.assertThat(result).isNotNull();
	}
	
	@org.junit.Test
	public void deveLancarErroQuandoNaoEncontrarusuarioCadastradoComEmailInformado() {
		
		// cenário
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		
		// ação
		Throwable exception = Assertions.catchThrowable(() -> usuarioService.autenticar("email@email.com", "senha"));
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário não encontrado para o email informado.");
	}
	
	@org.junit.Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		
		// cenário
		String email = "email@email.com";
		String senha = "senha";

		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
		
		// ação
		Throwable exception = Assertions.catchThrowable(() -> usuarioService.autenticar(email, "senhaerrada"));
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha inválida.");
	}
	
	@org.junit.Test(expected = org.junit.Test.None.class)
	public void deveValidarEmail() {
		
		// cenário
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		// ação
		usuarioService.validarEmail("email@email.com");
	}
	
	@org.junit.Test(expected = RegraNegocioException.class)
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {
		
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

		// ação
		usuarioService.validarEmail("email@email.com");
	}
	
	@org.junit.Test(expected = org.junit.Test.None.class)
	public void deveSalvarUmUsuario() {
		// cenário
		Mockito.doNothing().when(usuarioService).validarEmail(Mockito.anyString());
		
		Usuario usuario = Usuario.builder().email("email@email.com").senha("senha").id(1l).nome("nome").build();
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		// ação
		Usuario usuarioSalvo = usuarioService.salvarUsuario(new Usuario());
		
		// verificação
		Assertions.assertThat(usuarioSalvo).isNotNull();
		Assertions.assertThat(usuarioSalvo.getId()).isEqualTo(1l);
		Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
		Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
		Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
	}
	
	@Test(expected = RegraNegocioException.class)
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
		
		// cenário
		String email = "email@emal.com";
		Usuario usuario = Usuario.builder().email(email).build();
		Mockito.doThrow(RegraNegocioException.class).when(usuarioService).validarEmail(email);
		
		// ação
		usuarioService.salvarUsuario(usuario);
		
		// verificação
		Mockito.verify(repository, Mockito.never()).save(usuario);
		
	}

}
