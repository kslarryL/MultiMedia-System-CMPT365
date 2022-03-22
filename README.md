# MultiMedia-System-CMPT365

  This Course is about Multimedia systems design, multimedia hardware and software, issues in effectively representing, processing, and retrieving multimedia data such as text, graphics, sound and music, image and video. Coding in Java.



# project 
Create a program that reads an uncompressed TIFF image file and display it on the screen. A brief introduction
and historical review of TIFF can be found at https://en.wikipedia.org/wiki/TIFF
Details about the TIFF 6.0 format can be found in TIFF6.pdf as well as many other places online.
Only the 24-bit RGB full color uncompressed mode in TIFF will be considered (see Section 6: RGB Full Color
Images in TIFF6.pdf). You can assume that the image is no bigger than the 4*CIF size (i.e., 704*576). You can
ignore header tags that are not used in this project.
Your program should first show an open file dialog box for the user to select the TIFF file. It should then work as
follows:
1. Displays the original colored image;
2. Refreshes by the corresponding grayscale image of the original image;
3. Refreshes by applying ordered dithering on the grayscale image;
4. Refreshes by making the brighter part of the image darker and the darker part brighter, which is known as
“adjusting the dynamic range” in photo editing.
To move to the next step, your can either place a “next” button on screen, or pause until any key is pressed. Go
back to step 1 after step 4. There should also be a “quit” button.
We will not specify any dither matrix. You can experiment different ones and choose a good one. You need to
discuss your choice of the dither matrix in the report. For adjustment of dynamic range, you should experiment
different ways/thresholds and discuss the effects, too. 

