# Lung_XRAY_Image_Proccessing_App

The development of computational systems for automated diagnosis from medical images is a field of intense scientific research in recent years. Digital medical images are found in the vast majority of diagnostic laboratories, allowing them to be flexibly managed by computer systems. In particular, the digital processing of medical images involves the application of techniques for extracting the descriptive characteristics of human tissues in an accurate and reliable manner (eg segmentation). The detailed description of radiological images with values of specific characteristics can be used by pattern recognition methods with the aim of automated diagnosis and strengthening the work of doctors. The practical application of such systems is enormous, since it facilitates the timely and reliable detection of important incidents, and can be integrated both in local information systems of diagnostic centers and in wider telemedicine systems. Chest X-ray is one of the first methods to identify patients with COVID-19.

I have developed a program that:  
**1.	Opens an x-ray image  
2.	Improves imaging (using two appropriate methods of my choice).  
3.	Segments the lung region, and displays the evaluation of the segmentation relative to the actual segmentation (Ground Truth).  
4.	Extracts features from the lung region so that they can be used to classify a potential disease.**    
To evaluate the delineation, I have the real mask (ground truth) and I calculate the accuracy of the segmentation method I chose. Let A be the actual mask (Ground Truth) and B be the mask calculated by the automatic segmentation algorithm. For each image I calculate the number of pixels included in both masks and the number of pixels that belong to the area defined by mask A and not to B and vice versa. Therefore the following magnitudes are calculated:   

**(1) Common points or Area(A∩B)  
(2) Points that belong to A and not to B or Area ((A∪B)-B)    
(3) Points that belong to B and not in A or Area((A∪B)-A).**  
  
To normalize the results I used the following quantities:  
  
**DCS=Area(A∩B)/(Area(A∩B)+Area((A∪B)-B)+Area((A∪B)-A))  
  
D(A-B)=(Area((A∪B)-B))/(Area(A∩B)+Area((A∪B)-B)+Area((A∪B)-A))  
  
D(B-A)=(Area((A∪B)-A))/(Area(A∩B)+Area((A∪B)-B)+Area((A∪B)-A))**  
  
where DCS is the percentage (index) of common points and D(A-B), D(B-A) are the corresponding percentages of areas (2) and (3). The DCS value is MAX when the two contours coincide and zero when regions A and B have no points in common, i.e. the algorithm found no information. The range of interest therefore for the DCS is (0.0-1.0).  
Also the application calculates the following 6 features from the detected lung area.  
**1.	Area  
2.	Perimeter  
3.	Circularity  
4.	kurtosis  
5.	Feret diameter  
6.	Skewness**  
  
UI Buttons  
**• Open Image…  
• Preprocess: Enhance1  
• Preprocess: Enhance2  
• Segment: Image: Detect Lungs  
• Segment: Image: Evaluate result with ground truth  
• Analyze Lungs (here the values of the 6 attributes are displayed)**  
  
The application is developed with the Fiji package http://fiji.sc/  . The execution of java class MyApp  is done through the Script Editor of Fiji.  
The images used are in the relevant folder (files). The actual masks are separate for the right and left lung. When calculating the delineation assessment, however, both parts of the lungs are taken.  
  
Note: Images are from JSRT Database, and masks are from SCR Database  









































