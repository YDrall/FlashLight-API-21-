package company.no.flashlight;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("FieldCanBeLocal")
public class Flash {

    public Boolean flashStatus=false;
    private Context context;
    private CameraCaptureSession mSession;
    private CameraDevice cameraDevice;
    private CameraManager cameraManager;
    private CaptureRequest.Builder mBuilder;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private boolean initiated=false;

    public Flash(Context context){
        this.context=context;
    }

    // Initiates camera
    @SuppressWarnings("ResourceType")
    public void init() throws CameraAccessException {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraId =checkFlash();

        if (cameraId.length()!=0) {
            cameraManager.openCamera(cameraId, new MyCameraDeviceStateCallback(), null);
            this.initiated=true;
            this.flashStatus=true;
        }
        else {
            Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show();

            throw new CameraAccessException(-1,"Flash Not Available");
        }
    }

    //Helper functions to switch states of flashLight
    public void turnOnFlashLight() {
        try {
            mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            mSession.setRepeatingRequest(mBuilder.build(), null, null);
            this.flashStatus=true;
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight() {
        try {
            mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            mSession.setRepeatingRequest(mBuilder.build(), null, null);
            this.flashStatus=false;
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    //To get Camera id of camera with support of Flash light.
    //returns an empty string if not any attached camera device supports flash.
    private String checkFlash() throws CameraAccessException{
        String cameraIds[]=cameraManager.getCameraIdList();
        for(String id:cameraIds){
            CameraCharacteristics cameraCharacteristics=cameraManager.getCameraCharacteristics(id);
            if(cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)!=null) {
                return id;
            }
        }
        return "";
    }

    public void release() {
        cameraDevice.close();
        this.flashStatus=false;
        this.initiated=false;
    }

    public boolean getFlashStatus() {
        return flashStatus;
    }

    public boolean IsInitiated() {
        return initiated;
    }


    //Mostly API calls
    class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {


        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice=camera;
            try {
                List<Surface> surfaceList= new ArrayList<>();
                surfaceTexture=new SurfaceTexture(1);
                Size size=getSmallestSize(cameraDevice.getId());
                surfaceTexture.setDefaultBufferSize(size.getWidth(),size.getHeight());
                surface = new Surface(surfaceTexture);
                surfaceList.add(surface);

                mBuilder= camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mBuilder.addTarget(surface);

                mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                // default flash mode is off
                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);




                camera.createCaptureSession(surfaceList,new MyCameraCaptureSessionStateCallback(),null);


            }catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        private Size getSmallestSize(String cameraId) throws CameraAccessException {
            Size[] outputSizes = new Size[0];
            StreamConfigurationMap streamConfigurationMap =cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if(streamConfigurationMap!=null) {
                outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
            }
                if (outputSizes == null || outputSizes.length == 0) {
                    throw new IllegalStateException("Camera" + cameraId + "doesn't support any outputSize.");
                }
                Size chosen = outputSizes[0];
                for (Size s : outputSizes) {
                    if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                        chosen = s;
                    }
                }
                return chosen;

        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {


        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }

        class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                mSession=session;
                try {
                    mSession.setRepeatingRequest(mBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }
    }

}
