import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import ij.process.ImageProcessor;
import javax.swing.*;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import java.lang.Math;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import java.util.ArrayList;

public class MyApp extends JFrame {
	    private ImagePlus imp;
		private ImageJ ij;
		private boolean lungsDetected = false;

     public MyApp() {
         super("Image Proccessing");
         this.ij = new ImageJ(ImageJ.NO_SHOW);
         setSize(450, 450);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         addWindowListener(new WindowAdapter() {

             @Override
             public void windowClosing(WindowEvent e) {
                 ij.quit();
                 super.windowClosing(e);
             }
         });
         JButton openImageButton = new JButton("Open Image");
         openImageButton.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
                 imp = IJ.openImage();
                 imp.show();
             }
         });
         JButton enhance1Button = new JButton("Gamma correction");
         enhance1Button.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
                if (imp == null) {
					JOptionPane.showMessageDialog(null, "You need to open an image first!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
					JOptionPane.showMessageDialog(MyApp.this, "Applying Gamma correction");
					IJ.run("Gamma...", "value=2.0");
					imp.updateAndDraw();
			}
         });
         JButton enhance2Button = new JButton("Unsharp Mask");
         enhance2Button.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
                if (imp == null) { 
					JOptionPane.showMessageDialog(null, "You need to open an image first!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(MyApp.this, "Applying Unsharp Mask");
				IJ.run(imp, "Unsharp Mask...", "radius=3 amount=0.6");
				imp.updateAndDraw();
			}
         });
         JButton detectLungsButton = new JButton("Detect Lungs");
         detectLungsButton.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
                if (imp == null) { 
					JOptionPane.showMessageDialog(null, "You need to open an image first!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				lungsDetected = true;
				JOptionPane.showMessageDialog(MyApp.this, "Detecting the Lungs...");
				IJ.run(imp, "Median...", "radius=2"); 
				IJ.run("Enhance Contrast...", "saturated=35 normalize");
				IJ.setThreshold(80, 255);
				IJ.run(imp, "Threshold", "");
				IJ.run(imp, "Invert", "");
				
				int width = imp.getWidth();
				int height = imp.getHeight();

				ImageProcessor proc = imp.getProcessor();

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (y < 55 || y >= height - 65 || x < 105 || x >= width - 65) {
							proc.set(x, y, 0);
						}
					}
				}
				
				imp.updateAndDraw();
			}
         });
         JButton evaluateResultButton = new JButton("Evaluate result");
         evaluateResultButton.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
				if (lungsDetected) {
				JOptionPane.showMessageDialog(MyApp.this, "Please Select the left lung image");
				
				String leftLungPath = IJ.getFilePath("Select 'left lung' image");
				ImagePlus leftLung = IJ.openImage(leftLungPath);
				
				JOptionPane.showMessageDialog(MyApp.this, "Please Select the right lung image");
				
				String rightLungPath = IJ.getFilePath("Select 'right lung' image");
				ImagePlus rightLung = IJ.openImage(rightLungPath);
					
				int width = imp.getWidth();
				int height = imp.getHeight();
				ImagePlus groundTruth = IJ.createImage("Ground truth", "8-bit black", width, height, 1);

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (leftLung.getProcessor().get(x, y) == 255) { 
							groundTruth.getProcessor().set(x, y, 255);
						}
					}
				}

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (rightLung.getProcessor().get(x, y) == 255) {  
							groundTruth.getProcessor().set(x, y, 255);
						}
					}
				}
				groundTruth.show();
				
				int areaAIntersectB = 0;
				int areaAUnionBMinusB = 0;
				int areaAUnionBMinusA = 0;

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pixelA = groundTruth.getProcessor().get(x, y);
						int pixelB = imp.getProcessor().get(x, y);

						
						if ((pixelA == 255 && pixelB == 255) || (pixelA == 0 && pixelB == 0)) {
							areaAIntersectB++;
						}
						
						else if (pixelA == 255 && pixelB != 255) {
							areaAUnionBMinusB++;
						}
						
						else if (pixelA != 255 && pixelB == 255) {
							areaAUnionBMinusA++;
						}
					}
				}

				int areaAUnionB = areaAIntersectB + areaAUnionBMinusB + areaAUnionBMinusA;
				double dks = (double) areaAIntersectB / areaAUnionB;

				
				double deltaAminusB = (double) areaAUnionBMinusB / areaAUnionB;

				
				double deltaBminusA = (double) areaAUnionBMinusA / areaAUnionB;

				
				double accuracy = 100.0 * areaAIntersectB / (width * height);

				JOptionPane.showMessageDialog(MyApp.this, String.format("Accuracy: %.2f%%\nDKS: %f\nD(A-B): %f\nD(B-A): %f", accuracy, dks, deltaAminusB, deltaBminusA));
				groundTruth.close();
				} else {
					JOptionPane.showMessageDialog(null, "You need to detect the lungs first!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			 }
         });
         JButton analyzeLungsButton = new JButton("Analyze Lungs");
         analyzeLungsButton.addActionListener(new ActionListener() {

            @Override
			public void actionPerformed(ActionEvent e) {
				if (lungsDetected) { 
					
					int options = ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
					int measurements = ParticleAnalyzer.AREA + ParticleAnalyzer.PERIMETER + ParticleAnalyzer.CIRCULARITY + ParticleAnalyzer.FERET;
					
					ResultsTable rt = new ResultsTable();
					ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, 0, Double.POSITIVE_INFINITY);
					pa.analyze(imp);

					ArrayList<Double> areas = new ArrayList<>();

					for (int i = 0; i < rt.size(); i++) {
						
						areas.add(rt.getValue("Area", i));
					}

					double[] areasArray = new double[areas.size()];
					for (int i = 0; i < areas.size(); i++) {
						areasArray[i] = areas.get(i);
					}

					Skewness skewnessCalculator = new Skewness();
					Kurtosis kurtosisCalculator = new Kurtosis();

					int decimalPlaces = 5;
					double multiplier = Math.pow(10, decimalPlaces);

					double area = Math.round(rt.getValue("Area", 0) * multiplier) / multiplier;
					double perimeter = Math.round(rt.getValue("Perim.", 0) * multiplier) / multiplier;
					double circularity = Math.round(rt.getValue("Circ.", 0) * multiplier) / multiplier;
					double kurtosis = Math.round(kurtosisCalculator.evaluate(areasArray) * multiplier) / multiplier;
					double feret = Math.round(rt.getValue("Feret", 0) * multiplier) / multiplier;
					double skewness = Math.round(skewnessCalculator.evaluate(areasArray) * multiplier) / multiplier;

					System.out.println("Area: " + area + " pixels");
					System.out.println("Area perimeter: " + perimeter + " pixels");
					System.out.println("Circularity: " + circularity);
					System.out.println("Kurtosis: " + kurtosis);
					System.out.println("Feret diameter: " + feret + " pixels");
					System.out.println("Skewness: " + skewness);
					
					String results = "Area: " + area + " pixels\n"
								   + "Perimeter: " + perimeter + " pixels\n"
								   + "Circularity: " + circularity + "\n"
								   + "Kurtosis: " + kurtosis + "\n"
								   + "Feret diameter: " + feret + " pixels\n"
								   + "Skewness: " + skewness;
					
					JOptionPane.showMessageDialog(null, results);
				} else {
					JOptionPane.showMessageDialog(null, "You need to detect the lungs first!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JPanel contentPane = new JPanel(new GridLayout(2, 3));
		getContentPane().add(contentPane);
		contentPane.add(openImageButton);
		contentPane.add(enhance1Button);
		contentPane.add(enhance2Button);
		contentPane.add(detectLungsButton);
		contentPane.add(evaluateResultButton);
		contentPane.add(analyzeLungsButton);
		setVisible(true);
	}
	public static void main(String[] args) {
         new MyApp();
     }
} 
