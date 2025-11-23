package com.driverspa.ocr.camera;

/**
 * Created by Yerzhan Tanatov on 14/02/17.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.internal.client.FrameMetadataParcel;
import com.google.android.gms.vision.text.internal.client.LineBoxParcel;
import com.google.android.gms.vision.text.internal.client.TextRecognizerOptions;
import com.google.android.gms.vision.text.internal.client.zze;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public final class TextRecognizer extends Detector<TextBlock> {
    private final zze aFp;

    private TextRecognizer() {
        throw new IllegalStateException("Default constructor called");
    }

    private TextRecognizer(zze var1) {
        this.aFp = var1;
    }

    public SparseArray<TextBlock> detect(Frame var1) {
        if(var1 == null) {
            throw new IllegalArgumentException("No frame supplied.");
        } else {
            FrameMetadataParcel var2 = FrameMetadataParcel.zzc(var1);
            Bitmap var3;
            if(var1.getBitmap() != null) {
                var3 = var1.getBitmap();
            } else {
                Frame.Metadata var4 = var1.getMetadata();
                var3 = this.zza(var1.getGrayscaleImageData(), var4.getFormat(), var2.width, var2.height);
            }
//            matrix.postScale(0.5f, 0.5f);

            var3 = this.zzb(var3, var2);
            var2.rotation = 0;
            return this.zza(this.aFp.zzc(var3, var2));
        }
    }

    private Bitmap zza(ByteBuffer var1, int var2, int var3, int var4) {
        byte[] var5;
        if(var1.hasArray() && var1.arrayOffset() == 0) {
            var5 = var1.array();
        } else {
            var5 = new byte[var1.capacity()];
            var1.get(var5);
        }

        ByteArrayOutputStream var6 = new ByteArrayOutputStream();
        YuvImage var7 = new YuvImage(var5, var2, var3, var4, (int[])null);
        var7.compressToJpeg(new Rect(0, 0, var3, var4), 100, var6);
        byte[] var8 = var6.toByteArray();
        return BitmapFactory.decodeByteArray(var8, 0, var8.length);
    }

    private Bitmap zzb(Bitmap var1, FrameMetadataParcel var2) {
        int var3 = var1.getWidth();
        int var4 = var1.getHeight();
        if(var2.rotation != 0) {
            Matrix var5 = new Matrix();
            var5.postRotate((float)this.zzaar(var2.rotation));
            var1 = Bitmap.createBitmap(var1, 0, 0, var3, var4, var5, false);
        }

        if(var2.rotation == 1 || var2.rotation == 3) {
            var2.width = var4;
            var2.height = var3;
        }
        Matrix matrix = new Matrix();

        int ratio = 3;
        int heightRatio = 6;
        Bitmap croppedBitmap = Bitmap.createBitmap(var1, var1.getWidth()-(var1.getWidth() - var1.getWidth()/ratio), // x
                var1.getHeight()/2 - (var1.getHeight()/heightRatio)/2 , // y
                var1.getWidth()- var1.getWidth()/ratio - var1.getWidth()/ratio , //width
                var1.getHeight()/heightRatio, //height
                matrix, true);

        return croppedBitmap;
    }

    private SparseArray<TextBlock> zza(LineBoxParcel[] var1) {
        SparseArray var2 = new SparseArray();
        LineBoxParcel[] var3 = var1;
        int var4 = var1.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            LineBoxParcel var6 = var3[var5];
            SparseArray var7 = (SparseArray)var2.get(var6.aFA);
            if(var7 == null) {
                var7 = new SparseArray();
                var2.append(var6.aFA, var7);
            }

            var7.append(var6.aFB, var6);
        }

        SparseArray var8 = new SparseArray(var2.size());

        for(var4 = 0; var4 < var2.size(); ++var4) {
            var8.append(var2.keyAt(var4), new TextBlock((SparseArray)var2.valueAt(var4)));
        }

        return var8;
    }

    public boolean isOperational() {
        return this.aFp.isOperational();
    }

    private int zzaar(int var1) {
        switch(var1) {
            case 0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return 270;
            default:
                throw new IllegalArgumentException("Unsupported rotation degree.");
        }
    }

    public void release() {
        super.release();
        this.aFp.zzcib();
    }

    public static class Builder {
        private Context mContext;
        private TextRecognizerOptions aFq;

        public Builder(Context var1) {
            this.mContext = var1;
            this.aFq = new TextRecognizerOptions();
        }

        public TextRecognizer build() {
            zze var1 = new zze(this.mContext, this.aFq);
            return new TextRecognizer(var1);
        }
    }
}
