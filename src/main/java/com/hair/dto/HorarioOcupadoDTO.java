package com.hair.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HorarioOcupadoDTO {
    private String horario;
    private String nomeUsuario;
    private String telefoneUsuario;

    public HorarioOcupadoDTO(String horario, String nomeUsuario, String telefoneUsuario) {
        this.horario = horario;
        this.nomeUsuario = nomeUsuario;
        this.telefoneUsuario = telefoneUsuario;
    }

}
