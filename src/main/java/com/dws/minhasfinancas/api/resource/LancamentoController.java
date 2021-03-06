package com.dws.minhasfinancas.api.resource;

import com.dws.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.dws.minhasfinancas.api.dto.LancamentoDTO;
import com.dws.minhasfinancas.exception.RegraNegocioException;
import com.dws.minhasfinancas.model.entity.Lancamento;
import com.dws.minhasfinancas.model.entity.Usuario;
import com.dws.minhasfinancas.model.enums.StatusLancamento;
import com.dws.minhasfinancas.model.enums.TipoLancamento;
import com.dws.minhasfinancas.service.LancamentoService;
import com.dws.minhasfinancas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoController {

    @Autowired
    LancamentoService service;

    @Autowired
    UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento lancamentoSalvo = service.salvar(dtoParaLancamento(dto));
            return new ResponseEntity(lancamentoSalvo, HttpStatus.CREATED);
        } catch (RegraNegocioException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity buscar(
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "tipo", required = false, defaultValue = "") String tipo,
            @RequestParam("usuario") Long idUsuario) {

        Optional<Usuario> usuario = usuarioService.buscarPorId(idUsuario);

        if (!usuario.isPresent()) return new ResponseEntity("Usuário não encontrado", HttpStatus.BAD_REQUEST);

        Lancamento filtro = new Lancamento();
        filtro.setMes(mes);
        filtro.setAno(ano);
        filtro.setDescricao(descricao);
        filtro.setUsuario(usuario.get());

        if (!tipo.equals("")) {
            try {
                filtro.setTipo(TipoLancamento.valueOf(tipo));
            } catch (IllegalArgumentException e) {
                return new ResponseEntity("Não existe o Tipo de lançamento informado", HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity(service.buscar(filtro), HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
        try {
            ResponseEntity responseEntity = service.buscarPorId(id).map(entity -> {
                        Lancamento lancamento = dtoParaLancamento(dto);
                        lancamento.setId(entity.getId());
                        lancamento.setStatus(entity.getStatus());
                        service.atualizar(lancamento);
                        return new ResponseEntity(entity, HttpStatus.OK);
                    })
                    .orElseGet(() -> new ResponseEntity("Não existe nenhum lançamento com ID informado", HttpStatus.BAD_REQUEST));

            return responseEntity;

        } catch (RegraNegocioException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("{id}/atualiza-status")
    public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto) {
            ResponseEntity responseEntity = service.buscarPorId(id).map(entity -> {
                StatusLancamento statusLancamento = StatusLancamento.valueOf(dto.getStatus());
                if (statusLancamento == null) {
                    return new ResponseEntity("Status informado não existe", HttpStatus.BAD_REQUEST);
                }

                try {
                    entity.setStatus(statusLancamento);
                    service.atualizar(entity);
                    return new ResponseEntity(entity, HttpStatus.OK);
                } catch (Exception e) {
                    return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
                }
            }).orElseGet(() ->
                    new ResponseEntity("Não existe nenhum lançamento com ID informado", HttpStatus.BAD_REQUEST));

            return responseEntity;
    }

    @DeleteMapping("{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id) {

        try {
            ResponseEntity responseEntity = service.buscarPorId(id).map(entity -> {
                service.deletar(entity);
                return new ResponseEntity("Excluido com sucesso!", HttpStatus.NO_CONTENT);
            }).orElseGet(() -> new ResponseEntity("Não existe nenhum lançamento com esse ID", HttpStatus.BAD_REQUEST));

            return responseEntity;
        } catch (RegraNegocioException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private Lancamento dtoParaLancamento(LancamentoDTO dto) {
        Usuario usuario = usuarioService
                .buscarPorId(dto.getUsuario())
                .orElseThrow(() -> new RegraNegocioException("Usuário não existe"));

        Lancamento lancamento = Lancamento.builder()
                .id(dto.getId())
                .usuario(usuario)
                .descricao(dto.getDescricao())
                .tipo(TipoLancamento.valueOf(dto.getTipo()))
                .valor(dto.getValor())
                .mes(dto.getMes())
                .ano(dto.getAno())
                .build();
        return lancamento;
    }
}
