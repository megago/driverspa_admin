package com.driverspa.ocr.camera;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.internal.client.WordBoxParcel;

import java.util.ArrayList;
import java.util.List;


public class Element implements Text {
    private WordBoxParcel aFh;

    Element(WordBoxParcel var1) {
        this.aFh = var1;
    }

    public String getLanguage() {
        return this.aFh.aFm;
    }

    public String getValue() {
        return this.aFh.aFw;
    }

    public Rect getBoundingBox() {
        return zza.zza(this);
    }

    public Point[] getCornerPoints() {
        return zza.zza(this.aFh.aFt);
    }

    public List<? extends Text> getComponents() {
        return new ArrayList();
    }
}
