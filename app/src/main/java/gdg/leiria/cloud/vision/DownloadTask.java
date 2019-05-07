package gdg.leiria.cloud.vision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTask extends AsyncTask<URL, Void, Bitmap> {

    private DownloadImageListener downloadImageListener;

    DownloadTask(DownloadImageListener downloadImageListener) {
        this.downloadImageListener = downloadImageListener;
    }

    // Realizar a tarefa em background
    protected Bitmap doInBackground(URL... urls) {
        URL url = urls[0];
        HttpURLConnection connection = null;

        try {
            // Iniciar a ligação
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // Obter stream
            InputStream inputStream = connection.getInputStream();

            // Ler bitmap da stream
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

            return bmp;

        } catch (Exception e) {
            downloadImageListener.onError();
        } finally {
            // Fechar a ligação
            if (connection != null){
                connection.disconnect();
            }
        }
        return null;
    }

    // Quando o download é terminado, esta função é executada
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
           downloadImageListener.onFinish(result);
        } else {
          downloadImageListener.onError();
        }
    }

    // Custom method to convert string to url
    protected static URL stringToURL(String urlString) {
        try {
            URL url = new URL(urlString);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

