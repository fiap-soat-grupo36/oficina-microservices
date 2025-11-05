package br.com.fiap.oficina.catalog.repository;

import br.com.fiap.oficina.catalog.entity.Servico;
import br.com.fiap.oficina.shared.enums.CategoriaServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class ServicoRepositoryTest {

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar serviços ativos")
    void deveBuscarServicosAtivos() {
        // Arrange
        Servico ativo = new Servico();
        ativo.setNome("Serviço Ativo");
        ativo.setDescricao("Descrição");
        ativo.setPrecoBase(BigDecimal.valueOf(150.00));
        ativo.setCategoria(CategoriaServico.MECANICO);
        ativo.setTempoEstimado(java.time.Duration.ofMinutes(60));
        ativo.setAtivo(true);

        Servico inativo = new Servico();
        inativo.setNome("Serviço Inativo");
        inativo.setDescricao("Descrição");
        inativo.setPrecoBase(BigDecimal.valueOf(150.00));
        inativo.setCategoria(CategoriaServico.ELETRICO);
        inativo.setTempoEstimado(java.time.Duration.ofMinutes(60));
        inativo.setAtivo(false);

        entityManager.persist(ativo);
        entityManager.persist(inativo);
        entityManager.flush();

        // Act
        List<Servico> result = servicoRepository.findByAtivoTrue();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAtivo());
    }

    @Test
    @DisplayName("Deve buscar serviços inativos")
    void deveBuscarServicosInativos() {
        // Arrange
        Servico ativo = new Servico();
        ativo.setNome("Serviço Ativo");
        ativo.setDescricao("Descrição");
        ativo.setPrecoBase(BigDecimal.valueOf(150.00));
        ativo.setCategoria(CategoriaServico.MECANICO);
        ativo.setTempoEstimado(java.time.Duration.ofMinutes(60));
        ativo.setAtivo(true);

        Servico inativo = new Servico();
        inativo.setNome("Serviço Inativo");
        inativo.setDescricao("Descrição");
        inativo.setPrecoBase(BigDecimal.valueOf(150.00));
        inativo.setCategoria(CategoriaServico.ELETRICO);
        inativo.setTempoEstimado(java.time.Duration.ofMinutes(60));
        inativo.setAtivo(false);

        entityManager.persist(ativo);
        entityManager.persist(inativo);
        entityManager.flush();

        // Act
        List<Servico> result = servicoRepository.findByAtivoFalse();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getAtivo());
    }

    @Test
    @DisplayName("Deve buscar serviços por termo no nome")
    void deveBuscarServicosPorTermoNoNome() {
        // Arrange
        Servico servico = new Servico();
        servico.setNome("Troca de Óleo");
        servico.setDescricao("Serviço de manutenção");
        servico.setPrecoBase(BigDecimal.valueOf(100.00));
        servico.setCategoria(CategoriaServico.MECANICO);
        servico.setTempoEstimado(java.time.Duration.ofMinutes(30));
        servico.setAtivo(true);

        entityManager.persist(servico);
        entityManager.flush();

        // Act
        List<Servico> result = servicoRepository.buscarPorTermo("troca");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getNome().toLowerCase().contains("troca"));
    }

    @Test
    @DisplayName("Deve buscar serviços por termo na descrição")
    void deveBuscarServicosPorTermoNaDescricao() {
        // Arrange
        Servico servico = new Servico();
        servico.setNome("Serviço Especial");
        servico.setDescricao("Alinhamento e balanceamento");
        servico.setPrecoBase(BigDecimal.valueOf(100.00));
        servico.setCategoria(CategoriaServico.ALINHAMENTO);
        servico.setTempoEstimado(java.time.Duration.ofMinutes(45));
        servico.setAtivo(true);

        entityManager.persist(servico);
        entityManager.flush();

        // Act
        List<Servico> result = servicoRepository.buscarPorTermo("alinhamento");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDescricao().toLowerCase().contains("alinhamento"));
    }

    @Test
    @DisplayName("Não deve buscar serviços inativos por termo")
    void naoDeveBuscarServicosInativosPorTermo() {
        // Arrange
        Servico inativo = new Servico();
        inativo.setNome("Troca Inativa");
        inativo.setDescricao("Descrição");
        inativo.setPrecoBase(BigDecimal.valueOf(100.00));
        inativo.setCategoria(CategoriaServico.MECANICO);
        inativo.setTempoEstimado(java.time.Duration.ofMinutes(30));
        inativo.setAtivo(false);

        entityManager.persist(inativo);
        entityManager.flush();

        // Act
        List<Servico> result = servicoRepository.buscarPorTermo("troca");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
