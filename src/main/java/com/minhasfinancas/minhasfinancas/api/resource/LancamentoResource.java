package com.minhasfinancas.minhasfinancas.api.resource;

import java.util.List;
import java.util.Optional;

import javax.persistence.Entity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minhasfinancas.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.minhasfinancas.minhasfinancas.api.dto.LancamentoDTO;
import com.minhasfinancas.minhasfinancas.exeception.RegraNegocioException;
import com.minhasfinancas.minhasfinancas.model.entity.Lancamento;
import com.minhasfinancas.minhasfinancas.model.entity.Usuario;
import com.minhasfinancas.minhasfinancas.model.enums.StatusLancamento;
import com.minhasfinancas.minhasfinancas.model.enums.TipoLancamento;
import com.minhasfinancas.minhasfinancas.service.LancamentoService;
import com.minhasfinancas.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoResource {

	private final LancamentoService service;
	private final UsuarioService usuarioService;
	
	
	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
		
		try {
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			return new ResponseEntity(entidade, HttpStatus.CREATED);
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
		return service.obeterPorId(id).map(entity -> {
			try {
				Lancamento lancamento = converter(dto);
				lancamento.setId(entity.getId());
				service.atualizar(lancamento);
				return ResponseEntity.ok(lancamento);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> 
			new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST)
		);
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id) {
		return service.obeterPorId(id).map(entity -> {
				service.deletar(entity);
				return new ResponseEntity(HttpStatus.NO_CONTENT);
		}).orElseGet(() -> 
			new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST)
		);
	}
	
	@GetMapping
	public ResponseEntity buscar(
			@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam("usuario") Long idUsuario			
			) {
		
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setAno(ano);
		lancamentoFiltro.setMes(mes);
		
		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		
		if (!usuario.isPresent()) {
			return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuario não encontrado");
		} else {
			lancamentoFiltro.setUsuario(usuario.get());
		}
		
		List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
		return ResponseEntity.ok(lancamentos);
		
	}
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto) {
		return service.obeterPorId(id).map(entity -> {

			StatusLancamento statusLancamento = StatusLancamento.valueOf(dto.getStatus());
			
			if (statusLancamento == null) {
				return ResponseEntity.badRequest().body("Não foi possível atualizar o status do lançamento, envie um status valido.");
			}
			
			try {
				entity.setStatus(statusLancamento);
				service.atualizar(entity);
				return ResponseEntity.ok(entity);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> 
			new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST)
		);
	}
	
	private Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		
		Usuario usuario = usuarioService.obterPorId(dto.getUsuario()).orElseThrow(() -> new RegraNegocioException("Usuário não encontrado para o Id informado."));
		
		lancamento.setUsuario(usuario);
		
		if (dto.getTipo() != null) {
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		}
		
		if (dto.getStatus() != null) {
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		}
		
		return lancamento;
	}
	
	private LancamentoDTO converter(Lancamento lancamento) {
		return LancamentoDTO.builder()
				.id(lancamento.getId())
				.descricao(lancamento.getDescricao())
				.valor(lancamento.getValor())
				.mes(lancamento.getMes())
				.ano(lancamento.getAno())
				.status(lancamento.getStatus().name())
				.tipo(lancamento.getTipo().name())
				.usuario(lancamento.getUsuario().getId())
				.build();
	}
	
	@GetMapping("{id}")
	public ResponseEntity obterLancamento(@PathVariable("id") Long id) {
		return service.obeterPorId(id).map(lancamento -> new ResponseEntity(converter(lancamento), HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
	}
}
