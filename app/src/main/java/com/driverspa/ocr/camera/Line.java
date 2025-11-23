package com.driverspa.ocr.camera;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.internal.client.LineBoxParcel;
import com.google.android.gms.vision.text.internal.client.WordBoxParcel;

import java.util.ArrayList;
import java.util.List;


public class Line implements Text {
    private LineBoxParcel aFi;
    private List<Element> aFj;

    Line(LineBoxParcel var1) {
        this.aFi = var1;
    }

    List<Element> zzchy() {
        if(this.aFi.aFs.length == 0) {
            return new ArrayList(0);
        } else {
            if(this.aFj == null) {
                this.aFj = new ArrayList(this.aFi.aFs.length);
                WordBoxParcel[] var1 = this.aFi.aFs;
                int var2 = var1.length;

                for(int var3 = 0; var3 < var2; ++var3) {
                    WordBoxParcel var4 = var1[var3];
                    this.aFj.add(new Element(var4));
                }
            }

            return this.aFj;
        }
    }

    public String getLanguage() {
        return this.aFi.aFm;
    }

    public String getValue() {
        return this.aFi.aFw;
    }

    public Rect getBoundingBox() {
        return zza.zza(this);
    }

    public Point[] getCornerPoints() {
        return zza.zza(this.aFi.aFt);
    }

    public List<? extends Text> getComponents() {
        return this.zzchy();
    }

    public float getAngle() {
        return this.aFi.aFt.aFr;
    }

    public boolean isVertical() {
        return this.aFi.aFz;
    }
}
