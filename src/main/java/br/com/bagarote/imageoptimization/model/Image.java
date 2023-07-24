package br.com.bagarote.imageoptimization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@Entity
public class Image {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("idImagem")
    private Long id;

    @NotNull
    @Column(length = 5000, name = "imagem_alta_jpg")
    @JsonIgnore
    private byte[] imagemAltaJpg;

    @NotNull
    @Column(length = 5000, name = "imagem_alta_webp")
    @JsonIgnore
    private byte[] imagemAltaWebp;

    @NotNull
    @Column(length = 5000, name = "imagem_baixa_full_webp")
    @JsonIgnore
    private byte[] imagemBaixaFullWebp;

    @NotNull
    @Column(length = 5000, name = "imagem_baixa_proporcional_webp")
    @JsonIgnore
    private byte[] imagemBaixaProporcionalWebp;

    public byte[] getImagemAltaJpg() {
        return imagemAltaJpg;
    }

    public void setImagemAltaJpg(byte[] imagemAltaJpg) {
        this.imagemAltaJpg = imagemAltaJpg;
    }

    public byte[] getImagemAltaWebp() {
        return imagemAltaWebp;
    }

    public void setImagemAltaWebp(byte[] imagemAltaWebp) {
        this.imagemAltaWebp = imagemAltaWebp;
    }

    public byte[] getImagemBaixaFullWebp() {
        return imagemBaixaFullWebp;
    }

    public void setImagemBaixaFullWebp(byte[] imagemBaixaFullWebp) {
        this.imagemBaixaFullWebp = imagemBaixaFullWebp;
    }

    public byte[] getImagemBaixaProporcionalWebp() {
        return imagemBaixaProporcionalWebp;
    }

    public void setImagemBaixaProporcionalWebp(byte[] imagemBaixaProporcionalWebp) {
        this.imagemBaixaProporcionalWebp = imagemBaixaProporcionalWebp;
    }    
}
