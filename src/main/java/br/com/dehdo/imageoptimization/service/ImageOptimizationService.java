package br.com.dehdo.imageoptimization.service;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.dehdo.imageoptimization.model.Image;
import br.com.dehdo.imageoptimization.model.dto.request.ImageRequestDTO;
import br.com.dehdo.imageoptimization.repository.ImageRepository;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Canvas;
import net.coobird.thumbnailator.geometry.Positions;

@Service
public class ImageOptimizationService {
    // imagem alta cropada jpg no banco; ok
    // imagem alta cropada c/ marca d'agua menor tamanho possível em kb webp na web;
    // ok
    // imagem baixa full webp na web; ok
    // imagem baixa proporcionalizada webp na web; ok

    private final ImageRepository imageRepository;

    public ImageOptimizationService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public List<Image> list() {
        return imageRepository.findAll();
    }

    public ResponseEntity<Image> create(@RequestBody ImageRequestDTO originalImage) throws IOException {

        byte[] i = Base64.getDecoder().decode(originalImage.getImagem());
        BufferedImage bufferedImage = null;
        BufferedImage imagemAltaJpg = null;
        BufferedImage imageAltaWebp = null;
        BufferedImage imageBaixaFullWebp = null;
        BufferedImage imageBaixaProporcionalWebp = null;
        String format = "JPEG";

        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(i));

            // Teste de formato
            ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(i));
            try {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(input);
                        format = reader.getFormatName();
                        System.out.println(format);
                    } finally {
                        reader.dispose();
                    }
                }
            } finally {
                input.close();
            }
            // Teste de formato

            if (!format.equals("WebP")) {
                final ImageInfo imageInfo = Sanselan.getImageInfo(i);

                System.out.println("INTENSIDADE DE BITS " + imageInfo.getBitsPerPixel());
                System.out.println("WIDTH DPI " + imageInfo.getPhysicalWidthDpi());
                System.out.println("HEIGHT DPI " + imageInfo.getPhysicalHeightDpi());
                System.out.println("WIDTH " + imageInfo.getWidth());
                System.out.println("HEIGHT " + imageInfo.getHeight());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        if (bufferedImage != null) {
            display(bufferedImage, "Original " + format);

            if (!format.equals("JPEG")) {
                bufferedImage = convertImage(bufferedImage, "jpg");
            }

            bufferedImage = crop(bufferedImage, 0.03);

            imagemAltaJpg = resize(bufferedImage, 600, 900);
            display(imagemAltaJpg, "Alta");

            imageAltaWebp = getNewImage(imagemAltaJpg);
            imageAltaWebp = createAltaWebp(imageAltaWebp);
            display(imageAltaWebp, "Alta com Marca Dagua");

            imageBaixaFullWebp = getNewImage(imagemAltaJpg);
            imageBaixaFullWebp = createBaixaFullWebp(imageBaixaFullWebp);
            display(imageBaixaFullWebp, "Baixa Full");

            imageBaixaProporcionalWebp = getNewImage(imagemAltaJpg);
            imageBaixaProporcionalWebp = createBaixaProporcionalWebp(imageBaixaProporcionalWebp);
            display(imageBaixaProporcionalWebp, "Baixa Proporcional");
        }

        Image img = new Image();
        img.setImagemAltaJpg(getByteArrayImage(imagemAltaJpg, "jpg"));
        img.setImagemAltaWebp(getByteArrayImage(imageAltaWebp, "webp"));
        img.setImagemBaixaFullWebp(getByteArrayImage(imageBaixaFullWebp, "webp"));
        img.setImagemBaixaProporcionalWebp(getByteArrayImage(imageBaixaProporcionalWebp, "webp"));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(img);

    }

    public static BufferedImage getNewImage(BufferedImage bufferedImage) {
        BufferedImage newBufferedImage = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_BGR);

        newBufferedImage.createGraphics()
                .drawImage(bufferedImage, 0, 0, Color.white, null);

        return newBufferedImage;
    }

    public static byte[] getByteArrayImage(BufferedImage bufferedImage, String format) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Thumbnails.of(bufferedImage)
                    .outputFormat(format).scale(1.0).outputQuality(1.0f).toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static void display(BufferedImage bufferedImage, String title) {
        JFrame frame = new JFrame();
        JLabel label = new JLabel();
        frame.setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
        label.setIcon(new ImageIcon(bufferedImage));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setTitle(title + "  " + Integer.toString(bufferedImage.getWidth()) + " / "
                + Integer.toString(bufferedImage.getHeight()));
        frame.pack();
        frame.setVisible(true);
    }

    public static BufferedImage convertImage(BufferedImage bufferedImage, String format) {
        try {
            BufferedImage newBufferedImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_BGR);

            newBufferedImage.createGraphics()
                    .drawImage(bufferedImage, 0, 0, Color.white, null);

            return Thumbnails.of(newBufferedImage)
                    .outputFormat(format).scale(1.0)
                    .outputQuality(1.0f).asBufferedImage();
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }

    public static BufferedImage crop(BufferedImage bufferedImage, double tolerance) {
        // Get our top-left pixel color as our "baseline" for cropping
        int baseColor = bufferedImage.getRGB(0, 0);

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
        int bottomY = -1, bottomX = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (colorWithinTolerance(baseColor, bufferedImage.getRGB(x, y), tolerance)) {
                    if (x < topX)
                        topX = x;
                    if (y < topY)
                        topY = y;
                    if (x > bottomX)
                        bottomX = x;
                    if (y > bottomY)
                        bottomY = y;
                }
            }
        }

        BufferedImage destination = new BufferedImage((bottomX - topX + 1),
                (bottomY - topY + 1), bufferedImage.getType());

        destination.getGraphics().drawImage(bufferedImage, 0, 0,
                destination.getWidth(), destination.getHeight(),
                topX, topY, bottomX, bottomY, null);

        return destination;
    }

    private static boolean colorWithinTolerance(int a, int b, double tolerance) {
        int aAlpha = (int) ((a & 0xFF000000) >>> 24); // Alpha level
        int aRed = (int) ((a & 0x00FF0000) >>> 16); // Red level
        int aGreen = (int) ((a & 0x0000FF00) >>> 8); // Green level
        int aBlue = (int) (a & 0x000000FF); // Blue level

        int bAlpha = (int) ((b & 0xFF000000) >>> 24); // Alpha level
        int bRed = (int) ((b & 0x00FF0000) >>> 16); // Red level
        int bGreen = (int) ((b & 0x0000FF00) >>> 8); // Green level
        int bBlue = (int) (b & 0x000000FF); // Blue level

        double distance = Math.sqrt((aAlpha - bAlpha) * (aAlpha - bAlpha) +
                (aRed - bRed) * (aRed - bRed) +
                (aGreen - bGreen) * (aGreen - bGreen) +
                (aBlue - bBlue) * (aBlue - bBlue));

        // 510.0 is the maximum distance between two colors
        // (0,0,0,0 -> 255,255,255,255)
        double percentAway = distance / 510.0d;

        return (percentAway > tolerance);
    }

    public static BufferedImage resize(BufferedImage bufferedImage, int maxWidth, int maxHeight) {
        try {
            return Thumbnails.of(bufferedImage).size(maxWidth, maxHeight)
                    .outputFormat("jpg").outputQuality(1.0f).asBufferedImage();

        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }

    public static BufferedImage createAltaWebp(BufferedImage bufferedImage) {
        return addWaterMark(bufferedImage);
    }

    public static BufferedImage createBaixaFullWebp(BufferedImage bufferedImage) {
        return resize(bufferedImage, 160, 188);
    }

    public static BufferedImage createBaixaProporcionalWebp(BufferedImage bufferedImage) {
        // TODO - Realizar cálculo de maxWidth e maxHeight baseado em inputs de tamanho
        // em milímetros
        return resize(bufferedImage, 160, 188);
    }

    public static BufferedImage addWaterMark(BufferedImage bufferedImage) {
        File watermark = new File("src/main/resources/static/watermark.png");

        try {
            return Thumbnails.of(bufferedImage)
                    .scale(1.0)
                    .watermark(Positions.CENTER, ImageIO.read(watermark), 0.3f)
                    .outputQuality(1.0f)
                    .asBufferedImage();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}