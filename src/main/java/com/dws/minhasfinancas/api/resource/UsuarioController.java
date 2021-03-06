package com.dws.minhasfinancas.api.resource;

import com.dws.minhasfinancas.api.dto.AutenticarDTO;
import com.dws.minhasfinancas.api.dto.UsuarioDTO;
import com.dws.minhasfinancas.exception.ErroAutenticacao;
import com.dws.minhasfinancas.exception.RegraNegocioException;
import com.dws.minhasfinancas.model.entity.Usuario;
import com.dws.minhasfinancas.service.LancamentoService;
import com.dws.minhasfinancas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    UsuarioService service;

    @Autowired
    LancamentoService lancamentoService;

    @PostMapping("/autenticar")
    public ResponseEntity autenticar(@RequestBody AutenticarDTO dto) {
        try {
            Usuario autenticado = service.autenticar(dto.getEmail(), dto.getSenha());
            return ResponseEntity.ok(autenticado);
        } catch (ErroAutenticacao e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity salvar(@RequestBody UsuarioDTO dto) {
        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(dto.getSenha()).build();

        try {
            Usuario usuarioSalvo = service.salvarUsuario(usuario);
            return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
        } catch (RegraNegocioException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity obterSaldo(@PathVariable("id") Long id) {
        Optional<Usuario> usuario = service.buscarPorId(id);
        if (!usuario.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
        return ResponseEntity.ok(saldo);
    }
}
