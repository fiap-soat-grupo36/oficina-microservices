package br.com.fiap.oficina.auth.mapper;

import br.com.fiap.oficina.auth.dto.request.UsuarioRequestDTO;
import br.com.fiap.oficina.auth.dto.response.UsuarioResponseDTO;
import br.com.fiap.oficina.auth.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    Usuario toEntity(UsuarioRequestDTO dto);

    @Mapping(target = "role", expression = "java(usuario.getRole().name())")
    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}
