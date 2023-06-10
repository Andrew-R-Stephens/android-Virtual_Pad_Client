package com.tritiumgamingstudios.virtualpadclient.ui.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainViewModel extends ViewModel {

    public Socket socket;

    public PrintWriter out;
    public BufferedReader in;

    public Bitmap bmp;
    public Canvas canvas;

}