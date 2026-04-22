package com.hair.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasProfissionalDTO {
    private String nomeProfissional;
    private Long quantidadeServicos;
}
