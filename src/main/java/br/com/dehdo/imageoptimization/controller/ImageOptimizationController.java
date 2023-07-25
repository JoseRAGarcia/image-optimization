package br.com.dehdo.imageoptimization.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.dehdo.imageoptimization.model.Image;
import br.com.dehdo.imageoptimization.model.dto.request.ImageRequestDTO;
import br.com.dehdo.imageoptimization.service.ImageOptimizationService;

@RestController
@RequestMapping("/")
public class ImageOptimizationController {

    private final ImageOptimizationService imageOptimizationService;

    public ImageOptimizationController(ImageOptimizationService imageOptimizationService) {
        this.imageOptimizationService = imageOptimizationService;
    }

    @GetMapping
    public String index() {
        return "Image Optimization";
    }

    @GetMapping("/image")
    public List<Image> list() {
        return imageOptimizationService.list();
    }

    @PostMapping("/image")
    public ResponseEntity<Image> create(@RequestBody ImageRequestDTO imageBase64) throws IOException {
        return imageOptimizationService.create(imageBase64);
    }
}
