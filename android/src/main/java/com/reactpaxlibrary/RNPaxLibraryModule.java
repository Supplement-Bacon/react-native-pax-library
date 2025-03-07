
package com.reactpaxlibrary;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.pax.dal.ICashDrawer;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.neptunelite.api.NeptuneLiteUser;
import com.pax.dal.IScannerHw;
import com.pax.dal.entity.ScanResult;
import com.pax.dal.exceptions.ScannerHwDevException;

public class RNPaxLibraryModule extends ReactContextBaseJavaModule {
    static {
        System.loadLibrary("DeviceConfig");
    }

    private static final String NAME = "Pax";
    private final ReactApplicationContext reactContext;

    private IDAL dal;
    private IPrinter printer;
    private ICashDrawer cashDrawer;
    private IScannerHw mIScannerHw;


    public RNPaxLibraryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        try {
            dal = NeptuneLiteUser.getInstance().getDal(reactContext);
            printer = dal.getPrinter();
            cashDrawer = dal.getCashDrawer();
            mIScannerHw = dal.getScannerHw();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void printStr(String text, Double cutMode, Promise promise) {
        try {
            printer.init();
            printer.setGray(3);
            printer.printStr(text, null);
            printer.start();
            
            promise.resolve(true);
        } catch (Exception e) {
            e.printStackTrace();
            promise.resolve(false);
        }
    }
    
    @ReactMethod
    public void sayHi(Promise promise) {
        promise.resolve("Hi");
    }

    @ReactMethod
    public void openDrawer(Promise promise) {
        final int result = cashDrawer.open();

        if (result == 0) {
            promise.resolve(result);
        } else {
            promise.reject("Error "+ result, "The cash drawer cannot be opened.");
        }
    }

    @ReactMethod
    public void scanCode(final Promise promise) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIScannerHw != null) {
                        mIScannerHw.open();
                        ScanResult scanResult = mIScannerHw.read(10000); // Max 10 secondes
                        if (scanResult != null) {
                            // To ensure the Promise is called on the main thread, we post the result on the main thread
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mIScannerHw.stop();
                                        mIScannerHw.close();
                                    } catch (Exception e) {
                                        //
                                    }
                                    Toast.makeText(getReactApplicationContext(), scanResult.getContent(), Toast.LENGTH_SHORT).show();
                                    promise.resolve(scanResult.getContent());
                                }
                            });
                        } else {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    promise.reject("SCAN_ERROR", "No result obtained during the scan.");
                                }
                            });
                        }
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                promise.reject("SCANNER_NULL", "The scanner object is not available.");
                            }
                        });
                    }
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            promise.reject("SCAN_EXCEPTION", "Error during the scan", e);
                        }
                    });
                }
            }
        }).start();
    }

}