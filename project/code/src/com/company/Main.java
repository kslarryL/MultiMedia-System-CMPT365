package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import static java.lang.System.exit;
import static javax.swing.JOptionPane.showOptionDialog;


class Main {
    static byte[] data=null;//save data to byte array

    static boolean ByteOrder;//true if it is II  false if it is MM
    static public int ImageWidth = 0;
    static public int ImageLength = 0;
    static public java.util.List<Integer> StripOffsets = new ArrayList<Integer>();
    static int index;
    static public Color[] myColor=null;
    static public BufferedImage myImage;


    static class Type {
        public String name;
        public int size;

        public Type(String n, int s) {
            name = n;
            size = s;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TIFF");

        FileDialog dialog = new FileDialog(frame, "Open File", FileDialog.LOAD);

        Button button1 = new Button("Open file");
        Button button2 = new Button("exit");

        button1.addActionListener(e -> {
            dialog.setVisible(true);
            System.out.println(dialog.getDirectory() + dialog.getFile());
            data = null;
            myColor = null;
            Decode(dialog.getDirectory() + dialog.getFile());
            int printResult = 0;
            while(printResult==0)
            {
                myImage = new BufferedImage(ImageWidth, ImageLength, BufferedImage.TYPE_INT_RGB);
                int num = 0;
                for (int j = 0; j < ImageLength; j++){
                    for (int i = 0; i < ImageWidth; i++){
                        myImage.setRGB(i,j, myColor[num].getRGB());
                        num++;
                    }
                }
                printResult = printImage(myImage);
                if(printResult ==1){
                    JOptionPane.showMessageDialog(null, "Done");
                    System.exit(0);
                }
                //turning image to greyscale
                if(printResult==0)
                {
                    GreyScale(myImage);
                    printResult = printImage(myImage);
                }
                else{
                    JOptionPane.showMessageDialog(null, "Done");
                    System.exit(0);
                }
                if(printResult==0) {
                    //turning image to dithered image
                    DitheredImage(myImage);
                    printResult = printImage(myImage);}
                else{
                    JOptionPane.showMessageDialog(null, "Done");
                    System.exit(0);
                }
                if (printResult == 0) {
                    //turning image to dynamic range
                    DynamicRange(myImage);
                    printResult = printImage(myImage);}
                else{
                    JOptionPane.showMessageDialog(null, "Done");
                    System.exit(0);
                }
                if (printResult == 1) {
                    JOptionPane.showMessageDialog(null, "Done");
                    System.exit(0);
                }
            }
        });
        button2.addActionListener(e -> {
            exit(0);
        });

        frame.add(button1,BorderLayout.CENTER);
        frame.add(button2, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit(0);
            }
        });

        frame.setSize(400, 400);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    static public void Decode(String path) {

        //readAllBytes
        try {
            data = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }


        //decode header, Know the position of first IFD and II or MM
        int myIFD = IFH();

        //decode IFD and return address of next IFD
        while (myIFD != 0) {
            myIFD = IFD(myIFD);
        }
    }

    static public int IFD(int Position) {
        int temp = Position;
        int DECount = GetInt(temp, 2);
        temp += 2;
        for (int i = 0; i < DECount; i++) {
            DE(temp);
            temp += 12;
        }
        Strips();
        int pNext = GetInt(temp, 4);
        return pNext;
    }

    static private void Strips() {
        myColor = new Color[ImageLength * ImageWidth];
        index = StripOffsets.get(0);
        int R = 0;
        int G = 0;
        int B = 0;
        for (int i = 0; i <= (ImageWidth * ImageLength) - 1; i++) {
            int x = Byte.toUnsignedInt(data[index]);
            R = x;
            x = Byte.toUnsignedInt(data[index + 1]);
            G = x;
            x = Byte.toUnsignedInt(data[index + 2]);
            B = x;
            index += 3;
            myColor[i] = new Color(R, G, B);
        }
    }


    static public void DE(int Position) {
        //declare tag index
        int IndexOfTag = GetInt(Position, 2);
        //declare type index
        int IndexOfType = GetInt(Position + 2, 2);
        //declare number of count
        int Count = GetInt(Position + 4, 4);
        //get position of next data
        int nextData = Position + 8;
        int SizeInTotal = TypeArray[IndexOfType].size * Count;
        if (SizeInTotal > 4)
            nextData = GetInt(nextData, 4);
        GetDEValue(IndexOfTag, IndexOfType, Count, nextData);
    }

    static private void GetDEValue(int TagIndex, int TypeIndex, int Count, int pdata) {
        int typesize = TypeArray[TypeIndex].size;
        switch (TagIndex) {
            case 256://ImageWidth
                ImageWidth = GetInt(pdata, typesize);
                break;
            case 257://ImageLength
                if (TypeIndex == 3)//short
                    ImageLength = GetInt(pdata, typesize);
                break;
            case 273://StripOffsets
                for (int i = 0; i < Count; i++) {
                    int v = GetInt(pdata + i * typesize, typesize);
                    StripOffsets.add(v);
                }
                break;
            default:
                break;
        }
    }


    static private int IFH() {
        //firstly check the image is TIFF or not
        String OrderOfByte = GetString(0, 2);
        if (OrderOfByte.equals("II"))
            ByteOrder = true;
        else if (OrderOfByte.equals("MM"))
            ByteOrder = false;
        else
            exit(3);//if it is not MM / II

        int Version = GetInt(2, 2);

        if (Version != 42)
            exit(2);//if the image is not TIFF

        return GetInt(4, 4);
    }

    //find string function
    static private String GetString(int startingPosition, int Length) {
        String temp = "";
        for (int i = 0; i < Length; i++)
            temp += (char) data[startingPosition];
        return temp;
    }

    static private int GetInt(int StartingPosition, int Length) {
        int returnedResult = 0;
        // for "II"
        if (ByteOrder)
            for (int i = 0; i < Length; i++) {
                int x = Byte.toUnsignedInt(data[StartingPosition + i]);
                returnedResult |= x << i * 8;
            }
            // for "MM"
        else
            for (int i = 0; i < Length; i++){
                int x = Byte.toUnsignedInt(data[StartingPosition +Length-1- i]);
                returnedResult |= x << i * 8;
            }
        return returnedResult;
    }


    static private Type[] TypeArray = {
            new Type("???", 0),
            new Type("byte", 1),
            new Type("ascii", 1),
            new Type("short", 2),
            new Type("long", 4),
            new Type("rational", 8),
            new Type("sbyte", 1),
            new Type("undefined", 1),
            new Type("sshort", 1),
            new Type("slong", 1),
            new Type("srational", 1),
            new Type("float", 4),
            new Type("double", 8)
    };

    static public int printImage(BufferedImage Image)
    {
        ImageIcon myIcon = new ImageIcon(myImage);
        String[] options = {"Next","Quit"};
        Image = new BufferedImage(ImageWidth, ImageLength, BufferedImage.TYPE_INT_RGB);
        int dialogResult;
        dialogResult = showOptionDialog(null,null, "Show Image",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,myIcon,options,options[0]);
        return dialogResult;
    }

    static public void GreyScale(BufferedImage image){
        for(int i =0; i<ImageLength;i++) {
            for (int j = 0; j < ImageWidth; j++) {
                int R = myColor[i * ImageWidth + j].getRed();
                int G = myColor[i * ImageWidth + j].getGreen();
                int B = myColor[i * ImageWidth + j].getBlue();
                int Y = (int)(0.299 * R + 0.587 * G + 0.114 * B);
                int newColor = (Y << 16) | (Y << 8) | Y;
                image.setRGB(j, i, newColor);
            }
        }
    }


    static public void DitheredImage(BufferedImage image){
        int X,Y;
        int[][] dMatrix = {{0,2},{3,1}};//dither matrix 0,2,3,1
        int[][] original = new int[ImageLength][ImageWidth];
        for(X=0;X<ImageLength;X++) {
            for (Y = 0; Y < ImageWidth; Y++) {
                int R = myColor[X * ImageWidth + Y].getRed();
                int G = myColor[X * ImageWidth + Y].getGreen();
                int B = myColor[X * ImageWidth + Y].getBlue();
                int temp = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                temp = (int) Math.floor(temp / (256.0 / 5));
                original[X][Y] = temp;
                if (original[X][Y] > dMatrix[Y % 2][X % 2])
                    original[X][Y] = 255;
                else
                    original[X][Y] = 0;
                temp = original[X][Y];
                temp = (temp << 16) | (temp << 8) | temp;
                image.setRGB(Y, X, temp);
            }
        }
    }

    static public void DynamicRange(BufferedImage image){
        for(int i =0; i<ImageLength;i++) {
            for (int j = 0; j < ImageWidth; j++) {
                int R = myColor[i * ImageWidth + j].getRed();
                int G = myColor[i * ImageWidth + j].getGreen();
                int B = myColor[i * ImageWidth + j].getBlue();
                int Y = (int)(0.299 * R + 0.587 * G + 0.114 * B);
                int U = (int)(-0.299 * R - 0.587 * G + 0.886 * B);
                int V = (int)(0.701 * R - 0.587 * G -0.114 * B);
                if(Y >=200){
                    Y *= 0.8;
                }
                else if (Y <=30){
                    Y *= 1.2;
                }
                R = Y + V;
                G = (int)(Y- 0.194*U -0.510*V);
                B = Y + U;
                R = changeOutOfRangeRGB(R);
                G = changeOutOfRangeRGB(G);
                B = changeOutOfRangeRGB(B);
                int newColor = (R << 16) | (G << 8) | B;
                image.setRGB(j, i, newColor);
            }
        }
    }

    //if Value of RGB is out of range [0,255], set them to 0 or 255
    static public int changeOutOfRangeRGB(int color)
    {
        if(color > 255)
           color = 255;
        else if(color < 0)
            color =0;
        return color;
    }
}