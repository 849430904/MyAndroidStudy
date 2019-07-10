package com.aylanetworks.agilelink.fragments;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.android.volley.Response;
import com.aylanetworks.agilelink.MainActivity;
import com.aylanetworks.agilelink.R;
import com.aylanetworks.agilelink.controls.OutletLoadingView;
import com.aylanetworks.agilelink.framework.AMAPCore;
import com.aylanetworks.agilelink.framework.DeviceNotificationHelper;
import com.aylanetworks.agilelink.util.Constant;
import com.aylanetworks.aylasdk.AylaAPIRequest;
import com.aylanetworks.aylasdk.AylaDevice;
import com.aylanetworks.aylasdk.AylaDeviceManager;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.setup.AylaRegistration;
import com.aylanetworks.aylasdk.setup.AylaRegistrationCandidate;
import com.aylanetworks.aylasdk.setup.AylaSetup;
import com.aylanetworks.aylasdk.setup.AylaSetupDevice;
import com.aylanetworks.aylasdk.setup.AylaWifiStatus;
import com.aylanetworks.aylasdk.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.WIFI_SERVICE;


public class ConfigAPModeDialogFragment extends DialogFragment implements View.OnClickListener,
        AylaSetup.DeviceWifiStateChangeListener {


    private static String TAG = "ConfigAPModeDialogFragment";

    private TextView tvStep1;
    private TextView tvStep2;
    private TextView tvStep3;
    private TextView tvStep4;

    private ImageView ivStep1;
    private ImageView ivStep2;
    private ImageView ivStep3;
    private ImageView ivStep4;
    private TextView tvCountDown;
    private ImageView btnClose;

    private LinearLayout layoutRetry;
    private TextView tvFailStep;
    private TextView tvRemindMsg;
    private Button btnRetry;

    private OutletLoadingView outletLoadingView;
    private CountDownTimer timer;

    private static final int REQUEST_LOCATION = 2;
    private static final String DEFAULT_HOST_SCAN_REGEX = "Ayla-[0-9a-zA-Z]{12}";
    private String mSelectedWifiSSID;
    private String mSelectedWifiPassword;
    private AylaSetup _aylaSetup;

    private int countdown = Constant.CONFIG_AP_MODE_TIME_OUT;
    private volatile boolean isTimeOut = false;

    private static final String SSID = "SSID";
    private static final String PASSWORD = "PASSWORD";
    private static final String DSN = "DSN";
    public boolean isAddSuccess = false;


    private String deviceDsn = null;
    private int configType = Constant.CONFIG_TYPE_AP_MODE;



    private AP_MODE_CONFIG_STEP configStep = AP_MODE_CONFIG_STEP.STEP1;

    private View.OnClickListener listener;

    public enum AP_MODE_CONFIG_STEP{
        STEP1,
        STEP2,
        STEP3,
        STEP4;
        public int toInt(){
            return this.ordinal();
        }
    }

    private int[] stepMsg = new int[]{
            R.string.add_device_step1_fail,
            R.string.add_device_step2_fail,
            R.string.add_device_step3_fail,
            R.string.add_device_step4_fail};




    public ConfigAPModeDialogFragment() {

    }

    private void hideStepImageView(){
        ivStep1.setVisibility(View.INVISIBLE);
        ivStep2.setVisibility(View.INVISIBLE);
        ivStep3.setVisibility(View.INVISIBLE);
        ivStep4.setVisibility(View.INVISIBLE);
    }

    public static ConfigAPModeDialogFragment newInstance(String ssid, String wifiPassword,String deviceDsn) {
        ConfigAPModeDialogFragment fragment = new ConfigAPModeDialogFragment();
        Bundle args = new Bundle();
        args.putString(SSID, ssid);
        args.putString(PASSWORD, wifiPassword);
        args.putString(DSN, deviceDsn);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedWifiSSID = this.getArguments().getString(SSID);
        mSelectedWifiPassword = this.getArguments().getString(PASSWORD);
        deviceDsn = this.getArguments().getString(DSN);
        if(deviceDsn != null && deviceDsn.length() >0){
            configType = Constant.CONFIG_TYPE_QR_CODE;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        initData();

        View view = inflater.inflate(R.layout.fragment_config_apmode_dialog, container, false);
        tvStep1 = (TextView) view.findViewById(R.id.tv_step1);
        tvStep2 = (TextView) view.findViewById(R.id.tv_step2);
        tvStep3 = (TextView) view.findViewById(R.id.tv_step3);
        tvStep4 = (TextView) view.findViewById(R.id.tv_step4);
        ivStep1 = (ImageView) view.findViewById(R.id.iv_step1);
        ivStep2 = (ImageView) view.findViewById(R.id.iv_step2);
        ivStep3 = (ImageView) view.findViewById(R.id.iv_step3);
        ivStep4 = (ImageView) view.findViewById(R.id.iv_step4);

        layoutRetry = (LinearLayout) view.findViewById(R.id.layout_retry);
        tvFailStep = (TextView) view.findViewById(R.id.tv_fail_step);
        tvRemindMsg = (TextView) view.findViewById(R.id.tv_remind_msg);
        btnRetry = (Button) view.findViewById(R.id.btn_retry);
        tvCountDown = (TextView) view.findViewById(R.id.tv_countdown);
        btnClose = (ImageView) view.findViewById(R.id.btn_close);
        outletLoadingView = (OutletLoadingView) view.findViewById(R.id.loading_view);

        hideStepImageView();
        btnClose.setOnClickListener(this.getListener());
        btnRetry.setOnClickListener(this);

        outletLoadingView.startLoading();

        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long l) {
                if(countdown<=0) {
                    countdown = 0;
                    isTimeOut = true;
                    layoutRetry.setVisibility(View.VISIBLE);
                    outletLoadingView.stopLoading();
                    outletLoadingView.setVisibility(View.INVISIBLE);
                    if(isAdded()) {
                        tvFailStep.setText(getResources().getString(stepMsg[configStep.toInt()]));
                    }
                    timer.cancel();
                }
                if(isAdded()) {
                    tvCountDown.setText(countdown + "s");
                }
                countdown = countdown -1;
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();


        initAylaSetup();
        isAddSuccess = false;
        //开始配网，执行第一步
        doScanDevice();
        Log.d(TAG, "ssid:" + mSelectedWifiSSID + ",password:" + mSelectedWifiPassword);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // isGPSOpen();
    }


    @Override
    public void onDestroyView() {
        timer.cancel();
        timer = null;
        super.onDestroyView();
        Log.d(TAG, "onDestroyView call");
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_close:{

                exitSetup();
                dismissAllowingStateLoss();
                if(isAddSuccess){//添加成功了，返回到首页去
                    MainActivity.getInstance().popBackstackToRoot();
                }
                isTimeOut = true;
                break;
            }
            case R.id.btn_retry:{

                countdown = Constant.CONFIG_AP_MODE_TIME_OUT;
                isTimeOut = false;
                layoutRetry.setVisibility(View.INVISIBLE);
                outletLoadingView.startLoading();
                outletLoadingView.setVisibility(View.VISIBLE);

                if(configStep == AP_MODE_CONFIG_STEP.STEP1){
                    doScanDevice();
                }else if(configStep == AP_MODE_CONFIG_STEP.STEP2){
                    connectToDeviceAP(connectingSSID);
                }else if(configStep == AP_MODE_CONFIG_STEP.STEP3){
                    registerWithDsn();
                }

                timer.start();
                break;
            }

        }
    }


    //wifi状态发生了改变
    @Override
    public void wifiStateChanged(String currentState) {
        Log.d(TAG, "wifiStateChanged() called with: currentState = [" + currentState + "]");
        if (currentState != null) {
            MainActivity.getInstance().updateDialogText(String.format("%s: %s", getString(R.string.device_wifi_state),
                    currentState));
        }
    }


    /****************************配网核心流程*****************************/
    private String connectingSSID;
    //step1 扫描设备
    private void doScanDevice() {

        Log.d(TAG, "doScanDevice() called，第一步开始执行");
        try {
            if(!isAdded() || _aylaSetup == null || isTimeOut) {
                return;
            }
            configStep = AP_MODE_CONFIG_STEP.STEP1;
            tvStep1.setTextColor(getResources().getColor(R.color.green));
            _aylaSetup.scanForAccessPoints(15, new Predicate<ScanResult>() {
                        @Override
                        public boolean apply(ScanResult scanResult) {
                            // 扫描不包含 ayla 正则的wifi
                            return scanResult != null && scanResult.SSID.matches(DEFAULT_HOST_SCAN_REGEX);
                        }
                    },
                    new Response.Listener<ScanResult[]>() {
                        @Override
                        public void onResponse(ScanResult[] results) {

                            if (results.length > 0 && configStep == AP_MODE_CONFIG_STEP.STEP1) {

                                configStep = AP_MODE_CONFIG_STEP.STEP2;
                                if (results.length == 1) {
                                    connectingSSID = results[0].SSID;
                                    connectToDeviceAP(connectingSSID);//连接设备ssid
                                } else {

                                    // Let the user choose which device to connect to
                                    List<String> tempAPNames = new ArrayList<String>();
                                    for (int i = 0; i < results.length; i++) {
                                        if(tempAPNames.contains(results[i].SSID) == false){//有时候有重重的ssid
                                            tempAPNames.add(results[i].SSID);
                                        }
                                    }

                                    if(tempAPNames.size() == 1){
                                        connectingSSID = results[0].SSID;
                                        connectToDeviceAP(connectingSSID);//连接设备ssid
                                    }else {
                                        String[] apNames = new String[tempAPNames.size()];
                                        tempAPNames.toArray(apNames);

                                        new android.app.AlertDialog.Builder(getActivity())
                                                .setIcon(R.drawable.ic_launcher_belon)
                                                .setTitle(R.string.choose_new_device)
                                                .setSingleChoiceItems(apNames, -1, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        connectingSSID = apNames[which];
                                                        connectToDeviceAP(apNames[which]);
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.cancel, null)
                                                .create()
                                                .show();
                                    }

                                }
                            } else {
                                if (isTimeOut == false) {
                                    doScanDevice();
                                }
                            }
                    }
                    }, new ErrorListener() {
                        @Override
                        public void onErrorResponse(AylaError error) {
                            Log.d(TAG, "doScanDevice error:"+error.getLocalizedMessage());
                            if(isTimeOut == false) {
                                doScanDevice();
                            }
                        }
                    });

        }catch (Exception e){
             e.printStackTrace();
        }

    }

    //step2 连接设备
    private void connectToDeviceAP(String ssid) {

        if(!isAdded() || _aylaSetup == null || isTimeOut) {
            return;
        }

        Log.d(TAG, "connectToDeviceAP() called with: ssid = [" + ssid + "] ，第2步开始执行");
        tvStep2.setTextColor(getResources().getColor(R.color.green));
        ivStep1.setVisibility(View.VISIBLE);
        configStep = AP_MODE_CONFIG_STEP.STEP2;

        _aylaSetup.connectToNewDevice(ssid, 5,
                new Response.Listener<AylaSetupDevice>() {
                    @Override
                    public void onResponse(AylaSetupDevice setupDevice) {
                        if(setupDevice.getDsn() == null || setupDevice.getDsn().length() == 0) {
                            Log.d(TAG, "connectToDeviceAP onResponse success,but dsn is null...."+setupDevice.getDsn());
                            connectToDeviceAP(ssid);
                        }else {
                            _setupToken = ObjectUtils.generateRandomToken(8);//生成token
                            _setupDevice = setupDevice;
                            deviceDsn = _setupDevice.getDsn();
                            Log.d(TAG, "connectToDeviceAP onResponse success _setupToken:" + _setupToken + ",dsn:" + _setupDevice.getDsn());
                            prepareConnectDeviceToService(mSelectedWifiSSID, mSelectedWifiPassword, _setupToken);
                        }
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(final AylaError error) {
                        Log.d(TAG, "connect AP：" +ssid+",error:"+ error+",detail:"+error.getMessage());
                        if(isTimeOut == false) {
                            connectToDeviceAP(ssid);
                        }
                    }
                });
    }

    private void registerWithDsn(){

        if(!isAdded() || _aylaSetup == null || isTimeOut) {
            return;
        }

        if(deviceDsn == null || deviceDsn.length() == 0){
            Toast.makeText(getContext(), "dsn 为空!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStep3.setTextColor(getResources().getColor(R.color.green));
        ivStep2.setVisibility(View.VISIBLE);
        configStep = AP_MODE_CONFIG_STEP.STEP3;

        String connectedSSID = getConnectedSSID();
        Log.d(TAG, "connectedSSID:" + connectedSSID);
        if(connectedSSID.startsWith("Ayla-") || connectedSSID.length() == 0){
            reTryHandler.postDelayed(retryRegistRunnable,1500);//1.5s
            Log.d(TAG, "wifi名称不对，1.5后再检测. "+connectedSSID);
            return;
        }

        AylaRegistrationCandidate candidate = new AylaRegistrationCandidate();
        candidate.setDsn(deviceDsn);
        candidate.setRegistrationType(AylaDevice.RegistrationType.DSN);
        //Optional parameters for sending location during registration
        if (ContextCompat.checkSelfPermission(getActivity(),
                ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager =
                    (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            Location currentLocation;
            List<String> locationProviders = locationManager.getAllProviders();
            for (String provider : locationProviders) {
                currentLocation = locationManager.getLastKnownLocation(provider);
                if (currentLocation != null) {
                    candidate.setLatitude(String.valueOf(currentLocation.getLatitude()));
                    candidate.setLongitude(String.valueOf(currentLocation.getLongitude()));
                    break;
                }
            }

        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        final AylaAPIRequest request = _aylaRegistration.registerCandidate(candidate,

                new Response.Listener<AylaDevice>() {
                    @Override
                    public void onResponse(AylaDevice response) {
                        Log.d(TAG, "bind deivce success:"+response);
                        showCompleteWizar();
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        Log.d(TAG, "registerWithDsn error = [" + error + "]"+deviceDsn+"，isTimeOut："+isTimeOut);

                        AylaDeviceManager deviceManager = AMAPCore.sharedInstance().getDeviceManager();
                        if(deviceManager.deviceWithDSN(deviceDsn) != null){//已经绑定成功了
                            showCompleteWizar();
                        }else {
                            reTryHandler.postDelayed(retryRegistRunnable,1000);//1s
                          //  registerWithDsn();
                        }
                    }
                });

    }

    //step5 完成配置
    private void showCompleteWizar() {
        Log.d(TAG, "showCompleteWizar() called");
        if(!isAdded() || _aylaSetup == null  || isTimeOut) {
            return;
        }
        ivStep3.setVisibility(View.VISIBLE);
        tvStep4.setTextColor(getResources().getColor(R.color.green));
        ivStep4.setVisibility(View.VISIBLE);
        outletLoadingView.setLoadingComplete(getResources().getString(R.string.add_success));
        if(timer != null)
            timer.cancel();
        tvCountDown.setVisibility(View.INVISIBLE);
        isAddSuccess = true;
        
        goToPageIndex();
    }


    /****************************配网核心流程完*****************************/

    private void prepareConnectDeviceToService(String ssid, String password, final String setupToken) {
        if(!isAdded() || _aylaSetup == null ) {
            return;
        }
        mConnectDeviceToServiceSuccess = false;
        if (mConnectDeviceToServiceSuccess) {
            return;
        }
        mConnectDeviceToServiceSuccess = false;
        Log.d(TAG, "connectDeviceToService() called with: ssid = [" + ssid + "], password = [" + password + "], setupToken = [" + setupToken + "]");
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean locationAcquired = false;
        double lat = 0;
        double lng = 0;

        if (gpsEnabled || netEnabled) {
            String locationProvider = locationManager.getBestProvider(criteria, false);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission
                    .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getActivity(),
                            ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestScanPermissions();
            } else {
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location != null) {
                    locationAcquired = true;
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                }
                Log.d(TAG, "location：" + location);
            }
        } else {
            Toast.makeText(getActivity(), R.string.warning_location_accuracy, Toast.LENGTH_SHORT).show();
        }
        connectDeviceToService(ssid, password, setupToken, locationAcquired ? lat : null, locationAcquired ? lng : null);
    }


    //连接设备
    private void connectDeviceToService(String ssid, String password, final String setupToken, final Double lat, final Double lng) {


        _aylaSetup.connectDeviceToService(ssid, password, setupToken, lat, lng, 10,
                new Response.Listener<AylaWifiStatus>() {
                    @Override
                    public void onResponse(AylaWifiStatus response) {
                        mConnectDeviceToServiceSuccess = true;//连接成功
                        registerWithDsn();//连接成功，用dsn直接绑定
                        Log.d(TAG, "connectDeviceToService success onResponse() called with: response = [" + response + "]");

                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        Log.e(TAG, "connectDeviceToService onErrorResponse() called with: error = [" + error + "]");
                    }
                });

    }



    private void registerCandidate(final AylaRegistrationCandidate candidate) {

        Log.d(TAG, "registerCandidate() called with: candidate = [" + candidate + "]");
        _aylaRegistration.registerCandidate(candidate,
                new Response.Listener<AylaDevice>() {
                    @Override
                    public void onResponse(AylaDevice registeredDevice) {
                        // Now update the device notifications
                        DeviceNotificationHelper helper = new DeviceNotificationHelper(registeredDevice);
                        helper.initializeNewDeviceNotifications(new DeviceNotificationHelper.DeviceNotificationHelperListener() {
                            @Override
                            public void newDeviceUpdated(AylaDevice device, AylaError error) {
                                showCompleteWizar();
                                if (error != null) {
                                    AMAPCore.sharedInstance().getDeviceManager().fetchDevices();
                                }
                            }
                        });
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {

                    }
                });
    }


    private void fetchCandidateAndRegister(final String dsn, final AylaDevice.RegistrationType regType, final String regToken, final String lanIP) {

        Log.d(TAG, "fetchCandidateAndRegister() called with: dsn = [" + dsn + "], regType = [" + regType + "], regToken = [" + regToken + "], lanIP = [" + lanIP + "]");
        _aylaRegistration.fetchCandidate(dsn, regType,
                new Response.Listener<AylaRegistrationCandidate>() {
                    @Override
                    public void onResponse(AylaRegistrationCandidate candidate) {
                        registerCandidate(candidate, dsn, regType, regToken);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        AylaRegistrationCandidate candidate = new AylaRegistrationCandidate();
                        candidate.setDsn(dsn);
                        candidate.setLanIp(lanIP);
                        candidate.setRegistrationType(AylaDevice.RegistrationType.SameLan);

                        registerCandidate(candidate, dsn, AylaDevice.RegistrationType.SameLan, _setupDevice.getRegToken());
                    }
                });
    }

    private void registerCandidate(AylaRegistrationCandidate candidate, String dsn, AylaDevice.RegistrationType regType, String regToken) {
        Log.d(TAG, "registerCandidate() called with: candidate = [" + candidate + "], dsn = [" + dsn + "], regType = [" + regType + "], regToken = [" + regToken + "]");

        if (regToken != null) {
            candidate.setRegistrationToken(regToken);
        } else if (TextUtils.equals(dsn, candidate.getDsn()) || regType == AylaDevice.RegistrationType.APMode) {
            candidate.setSetupToken(_setupToken);
        }
        if (getActivity() == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestScanPermissions();
        } else {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            Location currentLocation;
            List<String> locationProviders = locationManager.getAllProviders();
            for (String provider : locationProviders) {
                currentLocation = locationManager.getLastKnownLocation(provider);
                if (currentLocation != null) {
                    candidate.setLatitude(String.valueOf(currentLocation.getLatitude()));
                    candidate.setLongitude(String.valueOf(currentLocation.getLongitude()));
                    break;
                }
            }
        }

        registerCandidate(candidate);
    }


    public static AylaDevice.RegistrationType fromDisplayString(String displayString) {
        Log.d(TAG, "fromDisplayString() called with: displayString = [" + displayString + "]");
        for (AylaDevice.RegistrationType type : AylaDevice.RegistrationType.values())
            if (type.stringValue().equals(displayString)) {
                return type;
            }

        return AylaDevice.RegistrationType.APMode; //not found
    }

    private void initData() {
        Log.d(TAG, "initData() called");
        AMAPCore instance = AMAPCore.sharedInstance();
        if (instance == null) {
            MainActivity.getInstance().popBackstackToRoot();
            return;
        }
        AylaDeviceManager dm = instance.getDeviceManager();
        if (dm == null) {
            MainActivity.getInstance().popBackstackToRoot();
            return;
        }
        _aylaRegistration = dm.getAylaRegistration();
    }



    private volatile boolean mConnectDeviceToServiceSuccess = false;
    private String _setupToken;
    private AylaSetupDevice _setupDevice;
    private AylaRegistration _aylaRegistration;

    private void requestScanPermissions() {
        Log.d(TAG, "requestScanPermissions() called");
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission
                .ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }


    public void exitSetup() {
        Log.d(TAG, "exitSetup() called");
        if (_aylaSetup != null) {
            _aylaSetup.removeListener(this);
            _aylaSetup.exitSetup(new Response.Listener<AylaAPIRequest.EmptyResponse>() {
                @Override
                public void onResponse(AylaAPIRequest.EmptyResponse response) {
                    AylaLog.d(TAG, "AylaSetup.exitSetup returned success");
                    _aylaSetup = null;
                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse(AylaError error) {
                    AylaLog.e(TAG, "AylaSetup.exitSetup returned " + error);
                    _aylaSetup = null;
                }
            });
        }
    }

    private void initAylaSetup(){
        try {
            _aylaSetup = new AylaSetup(AMAPCore.sharedInstance().getContext(),
                    AMAPCore.sharedInstance().getSessionManager());
            _aylaSetup.addListener(this);
        } catch (AylaError aylaError) {
            AylaLog.e(TAG, "Failed to create AylaSetup object: " + aylaError);
            Toast.makeText(AMAPCore.sharedInstance().getContext(), aylaError.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void dismiss() {
        super.dismiss();
        exitSetup();
    }

    //添加完成后，返回到首页去
    private void goToPageIndex() {

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                dismiss();

                List<Fragment> lists = MainActivity.getInstance().getSupportFragmentManager().getFragments();
                Fragment fragment = null;
                for (int i=0;i<lists.size();i++){
                    fragment = lists.get(i);
                    if(fragment instanceof MyDeviceListFragment){
                        ((MyDeviceListFragment)fragment).initRGBLightByDsn(deviceDsn);
                        break;
                    }
                }

                MainActivity.getInstance().popBackstackToRoot();
            }
        }, 1000);
    }


    /**
     * 获取当前连接WIFI的SSID
     */
    public String getConnectedSSID() {
        WifiManager wm = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return "";
    }


    private static final int TRY_REGISTER = 0x01;
    Handler reTryHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TRY_REGISTER:{
                    registerWithDsn();
                    break;
                }
            }
        }
    };

    Runnable retryRegistRunnable = new Runnable() {
        @Override
        public void run() {
            reTryHandler.sendEmptyMessage(TRY_REGISTER);
        }
    };

    public View.OnClickListener getListener() {
        return listener;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }
}
