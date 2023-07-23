package br.com.bagarote.imageoptimization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class Image {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("idImagem")
    private Long id;

    @NotNull
    @Column(length = 5000)
    @JsonIgnore
    private byte[] imagem;

    @NotNull
    @Column(name = "largura_px")
    private int larguraPx;

    @NotNull
    @Column(name = "altura_px")
    private int alturaPx;

    @Column(name = "largura_mm")
    private int larguraMm;

    @Column(name = "altura_mm")
    private int alturaMm;
}
