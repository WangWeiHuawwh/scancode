package com.myncic.scalecode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import com.google.zxing.WriterException;
import com.myncic.scalecode.encoding.EncodingHandler;

/**
 * Created by Administrator on 2015/8/13.
 */
public class CreatBitmap extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        Bitmap qrCodeBitmap = null;
        try {
            qrCodeBitmap = EncodingHandler.createQRCode("wwhdemail", 350);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(qrCodeBitmap);
    }
}
