package com.myncic.scalecode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Android手指拍照
 * 
 * @author wwh
 */
public class CameraActivity extends Activity implements OnClickListener{
	private Camera camera;
	private Camera.Parameters parameters = null;
	SurfaceView surfaceView ;
	Bundle bundle = null; // 声明一个Bundle对象，用来存储数据
	private Context context;
	private Button takepic_cancel,takepic;
	private String savepath="";
	private Bitmap curBitmap=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// 显示界面
		setContentView(R.layout.activity_camera);
		context=this;
		getViewId();
		init();
	}

	private void init(){
		try{
			Uri sParcelable=getIntent().getParcelableExtra(android.provider.MediaStore.EXTRA_OUTPUT);
			savepath=sParcelable.getPath();
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			if(savepath==null || savepath.isEmpty()){
				savepath=getIntent().getExtras().getString("savepath");
				getIntent().getStringExtra(android.provider.MediaStore.EXTRA_OUTPUT);
				String ss= getIntent().getExtras().getString(android.provider.MediaStore.EXTRA_OUTPUT);
				Log.e("tag","sspath="+ss);
				if(savepath==null || savepath.isEmpty()){
					savepath=Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/"+System.currentTimeMillis()+".jpg";
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			savepath=Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/"+System.currentTimeMillis()+".jpg";
		}
		try{
			File file=new File(savepath.substring(0,savepath.lastIndexOf("/")));
			if(!file.exists())
				file.mkdirs();
		}catch(Exception e){
			e.printStackTrace();
			if(savepath!=null && !savepath.isEmpty()){
				String temppath= savepath.replace("/", "@");
				temppath=temppath.substring(0,temppath.lastIndexOf("@"));
				File file=new File(temppath.replace("@", "/"));
				if(!file.exists())
					file.mkdirs();
			}else {
				finish();
				return;
			}
		}

		takepic_cancel.setOnClickListener(this);
		takepic.setOnClickListener(this);
		
		surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceView.getHolder().setFixedSize(640, 480);	//设置Surface分辨率
		surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
		surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数
	}

	private void getViewId(){
		takepic_cancel = (Button)findViewById(R.id.takepic_cancel);
		takepic = (Button)findViewById(R.id.takepic);
		surfaceView= (SurfaceView) this.findViewById(R.id.surfaceView);
	}
	
	private final class MyPictureCallback implements PictureCallback {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				File file=new File(savepath);
				if(file.exists()){
					file.delete();
				}
				setImageBitmap(data);
			} catch (Exception e) {
				e.printStackTrace();
			}catch(OutOfMemoryError e1){
				Toast.makeText(context, "内存溢出错误 602!",Toast.LENGTH_SHORT).show();
			}
		}
	}
	public void setImageBitmap(byte[] bytes) {
		try {
			
			BitmapFactory.Options options =new BitmapFactory.Options();   
		    options.inJustDecodeBounds =false;   
		    options.inSampleSize = 4;   
			Bitmap cameraBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,options);
			// 根据拍摄的方向旋转图像（纵向拍摄时要需要将图像选择90度)
			Matrix matrix = new Matrix();
			matrix.setRotate(CameraActivity.getPreviewDegree(this));
			curBitmap = Bitmap.createBitmap(cameraBitmap, 0, 0, cameraBitmap.getWidth(),
							cameraBitmap.getHeight(), matrix, true);
			cameraBitmap.recycle();
			Log.e("tag","bytes.length="+bytes.length);
			bytes=null;
			if(curBitmap!=null){
				try {
					saveBitmap_JPEG(savepath,curBitmap,90);
					Intent intent = CameraActivity.this.getIntent();
					String ImagePath = "file://" + savepath;
					Uri uri = Uri.parse(ImagePath);
					intent.setData(uri);
					CameraActivity.this.setResult(Activity.RESULT_OK,intent);
				} catch (Exception e) {
					e.printStackTrace();
				}			
				finish();
			}else takepic.setVisibility(0);
		} catch (Exception e) {
			e.printStackTrace();
		}catch(OutOfMemoryError e1){
			Toast.makeText(context, "内存溢出错误 603!",Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	public void saveBitmap_JPEG(String bitName,Bitmap bitmap,int quality)throws Exception {
		File f = new File(bitName);
		if (!f.exists())
			f.createNewFile();
		CompressFormat format= Bitmap.CompressFormat.JPEG;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(bitName);
			bitmap.compress(format, quality, stream);
			stream.flush();
			stream.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
	}
	private final class SurfaceCallback implements Callback {
		// 拍照状态变化时调用该方法
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			try{
				parameters = camera.getParameters(); // 获取各项参数
				parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
				parameters.setPreviewSize(width, height); // 设置预览大小
				parameters.setPreviewFrameRate(5);	//设置每秒显示4帧
				parameters.setPictureSize(width, height); // 设置保存的图片尺寸
				parameters.setJpegQuality(80); // 设置照片质量
			}catch(Exception e){
				e.printStackTrace();
				Toast.makeText(context,"相机调用出错!", Toast.LENGTH_SHORT).show();
			}
		}

		// 开始拍照时调用该方法
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera = Camera.open(); // 打开摄像头
				camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
				camera.setDisplayOrientation(getPreviewDegree(CameraActivity.this));
				camera.startPreview(); // 开始预览
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context,"打开相机出错!", Toast.LENGTH_SHORT).show();
			}
		}
		// 停止拍照时调用该方法
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				camera.release(); // 释放照相机
				camera = null;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		if(curBitmap!=null)
			curBitmap.recycle();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
			if (camera != null && event.getRepeatCount() == 0) {
				// 拍照
				//注：调用takePicture()方法进行拍照是传入了一个PictureCallback对象——当程序获取了拍照所得的图片数据之后
				//，PictureCallback对象将会被回调，该对象可以负责对相片进行保存或传入网络
				camera.takePicture(null, null, new MyPictureCallback());
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	public static int getPreviewDegree(Activity activity) {
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}
	long time=0;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.takepic_cancel:
				finish();
			break;

		case R.id.takepic:
			if (camera != null) {
				takepic.setVisibility(8);
				// 拍照
				//注：调用takePicture()方法进行拍照是传入了一个PictureCallback对象——当程序获取了拍照所得的图片数据之后
				//，PictureCallback对象将会被回调，该对象可以负责对相片进行保存或传入网络
				camera.takePicture(null, null, new MyPictureCallback());
				
			}
			break;
		}
	}
}
