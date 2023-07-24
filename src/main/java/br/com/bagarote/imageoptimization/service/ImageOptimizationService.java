package br.com.bagarote.imageoptimization.service;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.bagarote.imageoptimization.model.Image;
import br.com.bagarote.imageoptimization.model.dto.request.ImageRequestDTO;
import br.com.bagarote.imageoptimization.repository.ImageRepository;

@Service
public class ImageOptimizationService {
    // imagem alta cropada jpg no banco; ok
    // imagem alta cropada c/ marca d'agua menor tamanho possível em kb webp na web; ok
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

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        if (bufferedImage != null) {
            if (!format.equals("JPEG")) {
                bufferedImage = convertImage(bufferedImage, "jpg");
            }

            bufferedImage = crop(bufferedImage, 0.03);

            bufferedImage = resize(bufferedImage, 600, 900);
            display(bufferedImage, "Alta JPEG");

            imageAltaWebp = getNewImage(bufferedImage);
            imageAltaWebp = createAltaWebp(imageAltaWebp);
            display(imageAltaWebp, "Alta Webp");

            imageBaixaFullWebp = getNewImage(bufferedImage);
            imageBaixaFullWebp = createBaixaFullWebp(imageBaixaFullWebp);
            display(imageBaixaFullWebp, "Baixa Full Webp");

            imageBaixaProporcionalWebp = getNewImage(bufferedImage);
            imageBaixaProporcionalWebp = createBaixaProporcionalWebp(imageBaixaProporcionalWebp);
            display(imageBaixaProporcionalWebp, "Baixa Proporcional Webp");
        }

        Image img = new Image();

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

        BufferedImage newBufferedImage = getNewImage(bufferedImage);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(newBufferedImage, format, baos);
            baos.close();
            baos.toByteArray();

            ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
            return ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
            return bufferedImage;
        }
    }

    public static BufferedImage crop(BufferedImage source, double tolerance) {
        // Get our top-left pixel color as our "baseline" for cropping
        int baseColor = source.getRGB(0, 0);

        int width = source.getWidth();
        int height = source.getHeight();

        int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
        int bottomY = -1, bottomX = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (colorWithinTolerance(baseColor, source.getRGB(x, y), tolerance)) {
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
                (bottomY - topY + 1), BufferedImage.TYPE_INT_ARGB);

        destination.getGraphics().drawImage(source, 0, 0,
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
        BufferedImage scaledImage = null;
        double scaleFactor = 0;

        if (bufferedImage.getWidth() > bufferedImage.getHeight() && bufferedImage.getWidth() > maxWidth) {
            scaleFactor = (double) maxWidth / bufferedImage.getWidth();
            scaledImage = new BufferedImage(maxWidth, (int) (scaleFactor * bufferedImage.getHeight()),
                    BufferedImage.TYPE_INT_ARGB);
        } else if (bufferedImage.getWidth() < bufferedImage.getHeight() && bufferedImage.getHeight() > maxHeight) {
            scaleFactor = (double) maxHeight / bufferedImage.getHeight();
            scaledImage = new BufferedImage((int) (scaleFactor * bufferedImage.getWidth()), maxHeight,
                    BufferedImage.TYPE_INT_ARGB);
        } else if (bufferedImage.getWidth() == bufferedImage.getHeight() && bufferedImage.getWidth() > maxWidth) {
            scaleFactor = (double) maxWidth / bufferedImage.getWidth();
            scaledImage = new BufferedImage(maxWidth, maxWidth,
                    BufferedImage.TYPE_INT_ARGB);
        } else {
            return bufferedImage;
        }

        AffineTransform at = new AffineTransform();
        at.scale(scaleFactor, scaleFactor);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(bufferedImage, scaledImage);
    }

    public static BufferedImage createAltaWebp(BufferedImage bufferedImage) {
        BufferedImage newBufferedImage = addWaterMark(bufferedImage);
        return convertImage(newBufferedImage, "webp");
    }

    public static BufferedImage createBaixaFullWebp(BufferedImage bufferedImage) {
        BufferedImage newBufferedImage = convertImage(bufferedImage, "webp");
        return resize(newBufferedImage, 160, 180);
    }

    public static BufferedImage createBaixaProporcionalWebp(BufferedImage bufferedImage) {
        // TODO - Realizar cálculo de maxWidth e maxHeight baseado em inputs de tamanho
        // em milímetros
        BufferedImage newBufferedImage = convertImage(bufferedImage, "webp");
        return resize(newBufferedImage, 160, 188);
    }

    public static BufferedImage addWaterMark(BufferedImage bufferedImage) {
        File watermark = new File("src/main/resources/static/watermark.png");

        try {
            BufferedImage watermarkImage = ImageIO.read(watermark);

            Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g2d.setComposite(alphaChannel);

            int topLeftX = (bufferedImage.getWidth() - watermarkImage.getWidth()) / 2;
            int topLeftY = (bufferedImage.getHeight() - watermarkImage.getHeight()) / 2;

            g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);

            BufferedImage newBufferedImage = getNewImage(bufferedImage);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(newBufferedImage, "jpg", baos);
            baos.close();
            baos.toByteArray();
            g2d.dispose();

            ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
            return ImageIO.read(in);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return bufferedImage;
        }
    }
}