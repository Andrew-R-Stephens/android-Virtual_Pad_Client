package com.tritiumgamingstudios.virtualpadclient.ui.main;

import android.content.Context;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.net.InetAddresses;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tritiumgamingstudios.virtualpadclient.R;
import com.tritiumgamingstudios.virtualpadclient.uicomponent.TouchCanvas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainFragment extends Fragment {

    private MainViewModel viewModel;
    private TouchCanvas touchCanvas;
    private TextView connectionStatus;

    private Thread t;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        if(getActivity() != null)
            (getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        touchCanvas = view.findViewById(R.id.touchCanvas);
        touchCanvas.init(viewModel);
        connectionStatus = view.findViewById(R.id.label_connectionStatus);
        SwitchCompat brushSwitch = view.findViewById(R.id.switch1);
        brushSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                brushSwitch.setText("Eraser");
                touchCanvas.setBrushToEraser();
            } else {
                brushSwitch.setText("Pen");
                touchCanvas.setBrushToPen();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        t = new Thread(() -> {

            try {
                WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo;
                String ssid = null;
                wifiInfo = wifiManager.getConnectionInfo();
                int ipInt = wifiInfo.getIpAddress();
                ssid = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
                /*
                if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    ssid = Formatter.formatIpAddress(wifiInfo.getIpAddress());
                    System.out.println(ssid + " located");
                } else {
                    System.out.println("No SSID located");
                }
                */
                //InetAddress thisHost;
                String publicip = null;
                URL whatismyip = new URL("https://icanhazip.com/");
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                    // return the i.p address
                    publicip = in.readLine();
                    System.out.println("Public IP: " + publicip);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


                if(viewModel.socket == null) {
                    try {
                        viewModel.socket = new Socket("137.139.128.19", 1234);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                if(viewModel.out == null)
                    viewModel.out = new PrintWriter(viewModel.socket.getOutputStream(), true);
                if(viewModel.in == null)
                    viewModel.in = new BufferedReader(new InputStreamReader(viewModel.socket.getInputStream()));

                getActivity().runOnUiThread(() -> connectionStatus.setText("Connected!"));

                Log.d("VirtualPad", "Sending Client Confirmation...");
                viewModel.out.println("0");
                Log.d("VirtualPad", "Awaiting Server Confirmation... ");

                while(true) {
                    try {
                        if(touchCanvas.hasRegisteredInput() && viewModel.in.readLine().equals("0")) {
                            String msg = touchCanvas.getNextRegisteredInput();
                            if(msg != null && !msg.equals("null"))
                                viewModel.out.println(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        t.interrupt();
        t = null;
        try {
            viewModel.in.close();
            viewModel.in = null;
            viewModel.out.close();
            viewModel.out = null;
            viewModel.socket.close();
            viewModel.socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}