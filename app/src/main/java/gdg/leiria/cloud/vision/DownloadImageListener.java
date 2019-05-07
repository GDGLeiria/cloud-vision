package gdg.leiria.cloud.vision;

import android.graphics.Bitmap;

public interface DownloadImageListener {

    void onFinish(Bitmap bitmap);

    void onError();

}
