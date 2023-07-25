package br.com.dehdo.imageoptimization.model.dto.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class ImageRequestDTO implements Serializable {
    private String imagem;

    public ImageRequestDTO(String imagem) {
        this.imagem = imagem;
    }

    public ImageRequestDTO() {
    }
}
