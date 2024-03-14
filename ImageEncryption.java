import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ImageEncryption extends JFrame implements ActionListener {
    private JTextField inputField;
    private JButton selectImageButton;
    private JButton ecbEncryptButton;
    private JButton cbcEncryptButton;
    private JLabel imageLabel;
    private JFileChooser fileChooser;
    
    // AES encryption key
    private static final String AES_KEY = "0123456789abcdef";
    
    // IV for CBC mode
    private static final String IV = "abcdefghijklmnop";
    
    public ImageEncryption() {
        setTitle("Image Encryption");
        setSize(1600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        
        inputField = new JTextField();
        selectImageButton = new JButton("Select Image");
        selectImageButton.addActionListener(this);
        selectImageButton.setPreferredSize(new Dimension(600, 40));
        ecbEncryptButton = new JButton("ECB Encryption");
        ecbEncryptButton.addActionListener(this);
        ecbEncryptButton.setPreferredSize(new Dimension(600, 40));
        cbcEncryptButton = new JButton("CBC Encryption");
        cbcEncryptButton.addActionListener(this);
        imageLabel = new JLabel();
        fileChooser = new JFileChooser();
        
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);
        
        panel.add(selectImageButton);
        panel.add(inputField);
        panel.add(ecbEncryptButton);
        panel.add(cbcEncryptButton);
        
        add(panel, BorderLayout.NORTH);
        add(imageScrollPane, BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectImageButton) {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                inputField.setText(selectedFile.getAbsolutePath());
            }
        } else if (e.getSource() == ecbEncryptButton) {
            try {
                String inputFile = inputField.getText();
                BufferedImage image = ImageIO.read(new File(inputFile));
                
                // Encrypt the image data using ECB mode
                BufferedImage encryptedImage = encryptImage(image, AES_KEY, "ECB");

                // Display encrypted image
                ImageIcon icon = new ImageIcon(encryptedImage);
                imageLabel.setIcon(icon);
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error encrypting image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == cbcEncryptButton) {
            try {
                String inputFile = inputField.getText();
                BufferedImage image = ImageIO.read(new File(inputFile));
                
                // Encrypt the image data using CBC mode
                BufferedImage encryptedImage = encryptImage(image, AES_KEY, "CBC");

                // Display encrypted image
                ImageIcon icon = new ImageIcon(encryptedImage);
                imageLabel.setIcon(icon);
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error encrypting image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private BufferedImage encryptImage(BufferedImage image, String key, String mode) throws Exception {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage encryptedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY); // Set type to grayscale
    
        // Create AES cipher
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher;
        byte[] previousBlock = null;
        if (mode.equals("ECB")) {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } 
        else if (mode.equals("CBC")) {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            previousBlock = ivParameterSpec.getIV();
        }
        else {
            throw new IllegalArgumentException("Invalid encryption mode");
        }
    
        // Iterate over each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                byte[] pixelData = intToByteArray(pixel);
    
                // Perform XOR with previous block if CBC mode
                if (mode.equals("CBC")) {
                    for (int i = 0; i < pixelData.length && i < previousBlock.length; i++) {
                        pixelData[i] ^= previousBlock[i];
                    }
                }
    
                byte[] encryptedPixelData = cipher.doFinal(pixelData);
    
                // Update previous block for CBC mode
                if (mode.equals("CBC")) {
                    previousBlock = encryptedPixelData;
                }
    
                int encryptedPixel = byteArrayToInt(encryptedPixelData);
                encryptedImage.setRGB(x, y, -1*encryptedPixel);
            }
        }
    
        return encryptedImage;
    }
    
    

    
    // Helper method to convert int to byte array
    private byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value
        };
    }
    
    // Helper method to convert byte array to int
    private int byteArrayToInt(byte[] bytes) {
        return  (bytes[3] & 0xFF) | ((bytes[2] & 0xFF) << 8) | ((bytes[1] & 0xFF) << 16) |  (bytes[0] << 24);
    }
    
    public static void main(String[] args) {
        new ImageEncryption();
    }
}
