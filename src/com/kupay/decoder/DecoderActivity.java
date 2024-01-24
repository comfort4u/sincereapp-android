/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kupay.decoder;

import java.io.IOException;
import java.util.Collection;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;


import com.kupay.R;
import com.kupay.decoder.camara.CameraManager;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Example Decoder Activity.
 * 
 * @author Justin Wetherell (phishman3579@gmail.com)
 */
public class DecoderActivity extends Fragment implements IDecoderActivity, SurfaceHolder.Callback {

    protected static final String TAG = "app";

    protected DecoderActivityHandler handler = null;
    protected ViewfinderView viewfinderView = null;
    protected CameraManager cameraManager = null;
    protected boolean hasSurface = false;
    protected Collection<BarcodeFormat> decodeFormats = null;
    protected String characterSet = null;
    private Handler mHandler = new Handler();
    private final Runnable mLoadCamera = new Runnable()
    {
        public void run()
        {
            startCamera();
        }
    };
    SurfaceView surfaceView;
    

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View c = View.inflate(getActivity().getApplicationContext(), R.layout.decoder,null);
		viewfinderView = (ViewfinderView) c.findViewById(R.id.viewfinder_view);
		
		return c;
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        Log.v(TAG, "onCreate()");

       Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        handler = null;
        hasSurface = false;
      
    }

    @Override
	public void onDestroy() {
        super.onDestroy();
        hasSurface = false;
        Log.v(TAG, "onDestroy()");
    }

    @Override
	public void onResume() {
        super.onResume();
        
        Log.v(TAG, "onResume()1");

        // CameraManager must be initialized here, not in onCreate().
        if (cameraManager == null){
        	cameraManager = new CameraManager(getActivity().getApplicationContext());
        	Log.v(TAG, "En resume cameraManager es nullo");
        }else{
        	Log.v(TAG, "En resume viewfinderView NO es nullo");
        }

        if (viewfinderView == null) {
        	Log.v(TAG, "En resume viewfinderView es nullo");
            viewfinderView = (ViewfinderView) getView().findViewById(R.id.viewfinder_view);
            viewfinderView.setCameraManager(cameraManager);
        }else{
        	Log.v(TAG, "En resume viewfinderView NO es nullo");
        }
        
        viewfinderView.setVisibility(View.VISIBLE);
        
        
      
        
        mHandler.postDelayed(mLoadCamera, 100);

    }
    
    
    public void startCamera(){
    	
   
    			//(SurfaceView) getView().findViewById(R.id.preview_view);
    	 RelativeLayout rl = (RelativeLayout) getView().findViewById(R.id.fitCamera);
    	  surfaceView = new SurfaceView(getActivity());
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
 
        	
        	Log.v("app", "Cargando camara");
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        
        if(rl != null && surfaceView != null)
            rl.addView(surfaceView);
        
    }
    
    @Override
	public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause() DA");

        if (handler != null) {
        	Log.v("app", "El handeler no es nulo");
            handler.quitSynchronously();
            handler = null;
        }

        cameraManager.closeDriver();

        if (!hasSurface) {
        	Log.v("app", "No hay superficie<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }
    
    public void reStartCamera(){
    	
    	if (cameraManager == null){
        	//cameraManager = new CameraManager(getActivity().getApplicationContext());
        	Log.v(TAG, "En reStart cameraManager es nullo");
        }else{
        	Log.v(TAG, "En reStart viewfinderView NO es nullo");
        }

        if (viewfinderView == null) {
        	Log.v(TAG, "En reStart viewfinderView es nullo");
           // viewfinderView = (ViewfinderView) getView().findViewById(R.id.viewfinder_view);
            //viewfinderView.setCameraManager(cameraManager);
        }else{
        	Log.v(TAG, "En reStart viewfinderView NO es nullo");
        }
    	
    	if (handler != null){
    	 handler.quitSynchronously();
         handler = null;
    	}
    	Log.v(TAG, "SufaceView: "+surfaceView );
    	
         SurfaceHolder surfaceHolder = surfaceView.getHolder();
         surfaceHolder.removeCallback(this);
         initCamera(surfaceHolder);
    	
    }

    

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.v(TAG, "SUPERFICIE CREADA");
        if (holder == null)
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.v(TAG, "SUPERFICIE DESTRUIDA");
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Ignore
    }

    @Override
    public ViewfinderView getViewfinder() {
        return viewfinderView;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode) {
        drawResultPoints(barcode, rawResult);
    }

    protected void drawResultPoints(Bitmap barcode, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_image_border));
            paint.setStrokeWidth(3.0f);
            paint.setStyle(Paint.Style.STROKE);
            Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
            canvas.drawRect(border, paint);

            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1]);
            } else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and
                // metadata
                drawLine(canvas, paint, points[0], points[1]);
                drawLine(canvas, paint, points[2], points[3]);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    canvas.drawPoint(point.getX(), point.getY(), paint);
                }
            }
        }
    }

    protected static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b) {
        canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
    }

    protected void showScanner() {
        viewfinderView.setVisibility(View.VISIBLE);
    }

    protected void initCamera(SurfaceHolder surfaceHolder) {
        try {
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
        	cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
            	Log.v("app", "INICIANDO HANDELER PARA CAMARA");
            	handler = new DecoderActivityHandler(this, decodeFormats, characterSet, cameraManager);
            }
        } catch (IOException ioe) {
            Log.v(TAG, ioe.toString());
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.v(TAG, "Unexpected error initializing camera", e);
        }
    }
}
