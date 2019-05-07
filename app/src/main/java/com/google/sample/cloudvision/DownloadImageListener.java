package com.google.sample.cloudvision;

import android.graphics.Bitmap;

public interface DownloadImageListener {

    void onFinish(Bitmap bitmap);

    void onError();

}
