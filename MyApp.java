import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import java.awt.GridLayout; // Allaksa to borderlayout se gridlayout epeidh einai pio oraio (kai epeidh eixa ena provlima me to allo)
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import ij.process.ImageProcessor; // To xreiazomai gia na na allazo svsta ta pixels
import javax.swing.*;
import ij.measure.ResultsTable; // Xrisimopoio results table sto "Analyze lungs" koumpi
import ij.plugin.filter.ParticleAnalyzer; //Gia to teleytaio "Analyze lungs" koumpi gia na vrw ta : Area, Perimeter, Circularity, Feret Diameter
import java.lang.Math; // Gia to round poy kanw stis times
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis; //Apokleistika gia to kurtosis
import org.apache.commons.math3.stat.descriptive.moment.Skewness; // Apokleistika gia to skweness
import java.util.ArrayList; // Ftiaxno lista sto teleytaio koympi

public class MyApp extends JFrame {
	    private ImagePlus imp;
		private ImageJ ij;
		private boolean lungsDetected = false; // flag poy voithaei na kserw an o xrhsths exei pathsei to koumpi "lungs detected". Mono an to exei pathsei, mporw na xrhsimpopoihsw ta teleytaia dio koumpia

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
         JButton openImageButton = new JButton("Open Image"); // Me ton idio tropo orizo panta kainoyrgia koumpia
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
					JOptionPane.showMessageDialog(MyApp.this, "Applying Gamma correction"); //Minima ston xrhsth
					IJ.run("Gamma...", "value=2.0"); // Vazo diko moy value. Tha mporoysa na kano ton xrhsth na epileksei me: IJ.run("Gamma...");
					imp.updateAndDraw(); // Kano update thn eikona
			}
         });
         JButton enhance2Button = new JButton("Unsharp Mask");
         enhance2Button.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
                if (imp == null) { //Ean den yparxei anoixth eikona tote den afinw ton xrhsth na pathsei to koympi gia to enhance
					JOptionPane.showMessageDialog(null, "You need to open an image first!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(MyApp.this, "Applying Unsharp Mask");
				IJ.run(imp, "Unsharp Mask...", "radius=3 amount=0.6"); // Pali vazo dikiew mnoy times kai den adhnw ton xrhsth na epileksei. Tha mporoysa na ton afiso me: IJ.run(imp, "Unsharp Mask...");
				imp.updateAndDraw();
			}
         });
         JButton detectLungsButton = new JButton("Detect Lungs");
         detectLungsButton.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
                if (imp == null) { //Ean den yparxei anoixth eikona tote den afinw ton xrhsth na pathsei to koympi gia to enhance
					JOptionPane.showMessageDialog(null, "You need to open an image first!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				lungsDetected = true;
				JOptionPane.showMessageDialog(MyApp.this, "Detecting the Lungs...");
				IJ.run(imp, "Median...", "radius=2"); // Kano mean  gia na kanw pio smooth tin eikona
				IJ.run("Enhance Contrast...", "saturated=35 normalize"); // Vazo contrast gia na ginoun entonoi oi pneymones
				IJ.setThreshold(80, 255); // Kano to threshold gia na ginei h eikona mayrh kai asprh
				IJ.run(imp, "Threshold", "");
				IJ.run(imp, "Invert", ""); // Kano invert gia na ginoun asproi oi pneymones
				
				// pairno ta width kai height ths eikonas
				int width = imp.getWidth();
				int height = imp.getHeight();

				// pairno ton processor ths eikonas
				ImageProcessor proc = imp.getProcessor();

				// vazw ta pixels sto eksoteriko daxtylidi ths eikonas ws mayra gia na meinoun mono oi pneymones leykoi
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (y < 55 || y >= height - 65 || x < 105 || x >= width - 65) {
							proc.set(x, y, 0);
						}
					}
				}
				// kano Update sthn eikona
				imp.updateAndDraw();
			}
         });
         JButton evaluateResultButton = new JButton("Evaluate result");
         evaluateResultButton.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
				if (lungsDetected) { // Ean o xrhsths exei metatrepsei thn eikona se 8-bit binary (0 kai 255 mono) tote mporw na kanw to evaluate, alliws vgazei error message
				JOptionPane.showMessageDialog(MyApp.this, "Please Select the left lung image");
				// rotao ton user gia thn "left lung" eikona
				String leftLungPath = IJ.getFilePath("Select 'left lung' image");
				ImagePlus leftLung = IJ.openImage(leftLungPath);
				
				JOptionPane.showMessageDialog(MyApp.this, "Please Select the right lung image");
				// rotao ton user gia thn "right lung" eikona
				String rightLungPath = IJ.getFilePath("Select 'right lung' image");
				ImagePlus rightLung = IJ.openImage(rightLungPath);
					
				// ftiaxno mia kainourgia eikona me ta swsta width kai height ths original eikonas
				int width = imp.getWidth();
				int height = imp.getHeight();
				ImagePlus groundTruth = IJ.createImage("Ground truth", "8-bit black", width, height, 1);

				// epanalipsi poy pairnei ola ta pixels sthn eikona toy aristeroy pneymona, kai orizei to swsto pixel sthn nea eikona ws aspro, an to antistoixo pixel sthn eikona toy aristerou pneymona einai aspro
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (leftLung.getProcessor().get(x, y) == 255) {  // 255 == aspro
							groundTruth.getProcessor().set(x, y, 255);
						}
					}
				}

				// epanalipsi poy pairnei ola ta pixels sthn eikona toy deksioy pneymona, kai orizei to swsto pixel sthn nea eikona ws aspro, an to antistoixo pixel sthn eikona toy deksioy pneymona einai aspro
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (rightLung.getProcessor().get(x, y) == 255) {  
							groundTruth.getProcessor().set(x, y, 255);
						}
					}
				}
				groundTruth.show(); //Anoigw thn "ground truth" eikona gia na thn deiksw
				// vohtitika integers gia na ypologiso ta: DKS, Δ(A-B), and Δ(B-A)
				int areaAIntersectB = 0;
				int areaAUnionBMinusB = 0;
				int areaAUnionBMinusA = 0;

				// epanalipsi poy pernaei apo ola ta pixels stis eikones
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pixelA = groundTruth.getProcessor().get(x, y);
						int pixelB = imp.getProcessor().get(x, y);

						// pixels poy eiani koina shmeia: A∩B
						if ((pixelA == 255 && pixelB == 255) || (pixelA == 0 && pixelB == 0)) {
							areaAIntersectB++;
						}
						// pixels anoikoun sto (A∪B)-B an sto A einai aspra kai sto B den einai mavra
						else if (pixelA == 255 && pixelB != 255) {
							areaAUnionBMinusB++;
						}
						// pixels anoikoun sto (A∪B)-A an sto A den einai aspra kai sto B einai mavra
						else if (pixelA != 255 && pixelB == 255) {
							areaAUnionBMinusA++;
						}
					}
				}

				int areaAUnionB = areaAIntersectB + areaAUnionBMinusB + areaAUnionBMinusA;
				double dks = (double) areaAIntersectB / areaAUnionB;

				// ypologizw to Δ(A-B)
				double deltaAminusB = (double) areaAUnionBMinusB / areaAUnionB;

				// ypologizw to Δ(B-A)
				double deltaBminusA = (double) areaAUnionBMinusA / areaAUnionB;

				// ypologizo to accuracy tou "imp" me to "ground truth"
				double accuracy = 100.0 * areaAIntersectB / (width * height);

				// deixnw ena message dialog me ta apotelesmata
				JOptionPane.showMessageDialog(MyApp.this, String.format("Accuracy: %.2f%%\nDKS: %f\nD(A-B): %f\nD(B-A): %f", accuracy, dks, deltaAminusB, deltaBminusA));
				groundTruth.close(); //kleinw tin eikona
				} else {
					JOptionPane.showMessageDialog(null, "You need to detect the lungs first!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			 }
         });
         JButton analyzeLungsButton = new JButton("Analyze Lungs");
         analyzeLungsButton.addActionListener(new ActionListener() {

            @Override
			public void actionPerformed(ActionEvent e) {
				if (lungsDetected) { // Ean o xrhsths exei metatrepsei thn eikona se 8-bit binary (0 kai 255 mono) tote mporw na kanw to analyze, alliws vgazei error message
					// vazw measurement options
					int options = ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
					// ftiaxno kenougrio measurement kai to onomazo  measurements
					int measurements = ParticleAnalyzer.AREA + ParticleAnalyzer.PERIMETER + ParticleAnalyzer.CIRCULARITY + ParticleAnalyzer.FERET;
					// Ftiaxno ena results table
					ResultsTable rt = new ResultsTable();
					//Kanw Set up ton particle analyzer
					ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, 0, Double.POSITIVE_INFINITY);
					// Kanw Analyze ta particles sthn eikona imp
					pa.analyze(imp);

					// ftiaxnw mia lista gia na apothikeyo tis perioxes ton particles
					ArrayList<Double> areas = new ArrayList<>();

					// epanalipsi poy pernaei apo ola ta particles
					for (int i = 0; i < rt.size(); i++) {
						// prosthetw thn perioxh twn particle stin lista
						areas.add(rt.getValue("Area", i));
					}

					// metatrepw apo list se array
					double[] areasArray = new double[areas.size()];
					for (int i = 0; i < areas.size(); i++) {
						areasArray[i] = areas.get(i);
					}

					// ypologizw skewness kai kurtosis apo tis perioxes ton particles
					Skewness skewnessCalculator = new Skewness();
					Kurtosis kurtosisCalculator = new Kurtosis();

					// orizw ton arithmo twn dekadikwn psifiwn
					int decimalPlaces = 5;
					// ftiaxnw to multiplier gia na kanw round ston katallilo arithmo dekadikwn thesewn
					double multiplier = Math.pow(10, decimalPlaces);

					// Kanw round ola ta apotelesmata se dekadikous, sto 5o psifio
					double area = Math.round(rt.getValue("Area", 0) * multiplier) / multiplier;
					double perimeter = Math.round(rt.getValue("Perim.", 0) * multiplier) / multiplier;
					double circularity = Math.round(rt.getValue("Circ.", 0) * multiplier) / multiplier;
					double kurtosis = Math.round(kurtosisCalculator.evaluate(areasArray) * multiplier) / multiplier;
					double feret = Math.round(rt.getValue("Feret", 0) * multiplier) / multiplier;
					double skewness = Math.round(skewnessCalculator.evaluate(areasArray) * multiplier) / multiplier;

					// ektypwnw ta stoggilopoihmena apotelesmata
					System.out.println("Area: " + area + " pixels");
					System.out.println("Area perimeter: " + perimeter + " pixels");
					System.out.println("Circularity: " + circularity);
					System.out.println("Kurtosis: " + kurtosis);
					System.out.println("Feret diameter: " + feret + " pixels");
					System.out.println("Skewness: " + skewness);
					// Ftiaxnw string me ta apotelesmata gia eykolia
					String results = "Area: " + area + " pixels\n"
								   + "Perimeter: " + perimeter + " pixels\n"
								   + "Circularity: " + circularity + "\n"
								   + "Kurtosis: " + kurtosis + "\n"
								   + "Feret diameter: " + feret + " pixels\n"
								   + "Skewness: " + skewness;
					//Ektypwnw se message dialog to string me ta apotelemsta
					JOptionPane.showMessageDialog(null, results);
				} else {
					JOptionPane.showMessageDialog(null, "You need to detect the lungs first!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JPanel contentPane = new JPanel(new GridLayout(2, 3)); //grid layout me 2 seires kai 3 stiles
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