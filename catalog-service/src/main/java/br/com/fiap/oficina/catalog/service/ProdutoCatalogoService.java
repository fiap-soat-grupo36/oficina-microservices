package br.com.fiap.oficina.catalog.service;

import br.com.fiap.oficina.catalog.dto.request.ProdutoCatalogoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ProdutoCatalogoResponseDTO;
import br.com.fiap.oficina.catalog.entity.ProdutoCatalogo;

import java.util.List;

public interface ProdutoCatalogoService {

    ProdutoCatalogoResponseDTO salvar(ProdutoCatalogoRequestDTO produto);

    ProdutoCatalogoResponseDTO atualizar(Long id, ProdutoCatalogoRequestDTO produto);

    ProdutoCatalogoResponseDTO buscarPorId(Long id);

    List<ProdutoCatalogoResponseDTO> listarTodos();

    List<ProdutoCatalogoResponseDTO> listarAtivos();

    List<ProdutoCatalogoResponseDTO> listarInativos();

    void deletar(Long id);

    ProdutoCatalogo getProduto(Long id);
}
