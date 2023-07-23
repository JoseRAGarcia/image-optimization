package br.com.bagarote.imageoptimization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.bagarote.imageoptimization.model.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
}
