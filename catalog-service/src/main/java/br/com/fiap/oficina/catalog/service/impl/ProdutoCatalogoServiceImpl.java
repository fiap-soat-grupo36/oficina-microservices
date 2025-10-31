package br.com.fiap.oficina.catalog.service.impl;

import br.com.fiap.oficina.catalog.dto.request.ProdutoCatalogoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ProdutoCatalogoResponseDTO;
import br.com.fiap.oficina.catalog.entity.ProdutoCatalogo;
import br.com.fiap.oficina.catalog.mapper.ProdutoCatalogoMapper;
import br.com.fiap.oficina.catalog.repository.ProdutoCatalogoRepository;
import br.com.fiap.oficina.catalog.service.ProdutoCatalogoService;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoCatalogoServiceImpl implements ProdutoCatalogoService {

    private final ProdutoCatalogoRepository produtoRepository;
    private final ProdutoCatalogoMapper produtoMapper;

    @Autowired
    public ProdutoCatalogoServiceImpl(ProdutoCatalogoRepository produtoRepository,
                                      ProdutoCatalogoMapper produtoMapper) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
    }

    @Override
    public ProdutoCatalogoResponseDTO salvar(ProdutoCatalogoRequestDTO produtoDTO) {
        ProdutoCatalogo produto = produtoMapper.toEntity(produtoDTO);
        produto.setAtivo(true);
        ProdutoCatalogo salvo = produtoRepository.save(produto);
        return produtoMapper.toDTO(salvo);
    }

    @Override
    public ProdutoCatalogoResponseDTO atualizar(Long id, ProdutoCatalogoRequestDTO produtoDTO) {
        ProdutoCatalogo produto = getProduto(id);
        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setCategoria(produtoDTO.getCategoria());
        produto.setPreco(produtoDTO.getPreco());
        ProdutoCatalogo atualizado = produtoRepository.save(produto);
        return produtoMapper.toDTO(atualizado);
    }

    @Override
    public ProdutoCatalogoResponseDTO buscarPorId(Long id) {
        return produtoMapper.toDTO(getProduto(id));
    }

    @Override
    public List<ProdutoCatalogoResponseDTO> listarTodos() {
        return produtoMapper.toDTO(produtoRepository.findAll());
    }

    @Override
    public List<ProdutoCatalogoResponseDTO> listarAtivos() {
        return produtoMapper.toDTO(produtoRepository.findByAtivoTrue());
    }

    @Override
    public List<ProdutoCatalogoResponseDTO> listarInativos() {
        return produtoMapper.toDTO(produtoRepository.findByAtivoFalse());
    }

    @Override
    public List<ProdutoCatalogoResponseDTO> buscarPorTermo(String termo) {
        return produtoMapper.toDTO(produtoRepository.buscarPorTermo(termo));
    }

    @Override
    public void deletar(Long id) {
        ProdutoCatalogo produto = getProduto(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Override
    public ProdutoCatalogo getProduto(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Produto de catálogo com ID %d não encontrado", id)));
    }
}
