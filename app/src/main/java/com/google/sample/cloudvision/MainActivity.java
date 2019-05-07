/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
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

package com.google.sample.cloudvision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import gdg.leiria.cloud.vision.R;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView textViewResult;
    private ImageView mainImage;
    private EditText editTextUrl;
    private Button buttonAnalyse;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        textViewResult = findViewById(R.id.textViewResult);
        mainImage = findViewById(R.id.main_image);
        editTextUrl = findViewById(R.id.editTextUrl);
        buttonAnalyse = findViewById(R.id.button_analyse);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    /*********************************************************************************************************
    *    ______                    _                 _       _         _
    *    |  _  \                  | |               | |     | |       (_)
    *    | | | |_____      ___ __ | | ___   __ _  __| |   __| | __ _   _ _ __ ___   __ _  __ _  ___ _ __ ___
    *    | | | / _ \ \ /\ / / '_ \| |/ _ \ / _` |/ _` |  / _` |/ _` | | | '_ ` _ \ / _` |/ _` |/ _ \ '_ ` _ \
    *    | |/ / (_) \ V  V /| | | | | (_) | (_| | (_| | | (_| | (_| | | | | | | | | (_| | (_| |  __/ | | | | |
    *    |___/ \___/ \_/\_/ |_| |_|_|\___/ \__,_|\__,_|  \__,_|\__,_| |_|_| |_| |_|\__,_|\__, |\___|_| |_| |_|
    *                                                                                     __/ |
    *                                                                                    |___/
    /*********************************************************************************************************/
    public void onDownloadImageClick(View view) {

        String url = editTextUrl.getText().toString();
        textViewResult.setText("");

        if (url.trim().length() == 0) {
            editTextUrl.setError(getString(R.string.valid_url));
            return;
        }

        new DownloadTask(new DownloadImageListener() {
            @Override
            public void onFinish(Bitmap bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainImage.setImageBitmap(bitmap);
                        buttonAnalyse.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.download_error), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).execute(DownloadTask.stringToURL(url));

    }
    /**********************************************************************************************************/



    /**********************************************************************************************
    *      ___              _ _                  _
    *     / _ \            | (_)                (_)
    *    / /_\ \_ __   __ _| |_ ___  __ _ _ __   _ _ __ ___   __ _  __ _  ___ _ __ ___
    *    |  _  | '_ \ / _` | | / __|/ _` | '__| | | '_ ` _ \ / _` |/ _` |/ _ \ '_ ` _ \
    *    | | | | | | | (_| | | \__ \ (_| | |    | | | | | | | (_| | (_| |  __/ | | | | |
    *    \_| |_/_| |_|\__,_|_|_|___/\__,_|_|    |_|_| |_| |_|\__,_|\__, |\___|_| |_| |_|
    *                                                               __/ |
    *                                                              |___/
    ***********************************************************************************************/

    /**
     * Esta função é responsável por tratar do click no botão "Analisar"
     * @param view
     */
    public void onAnalyseClick(View view) {
        BitmapDrawable drawable = (BitmapDrawable) mainImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        callCloudVision(bitmap);

    }

    /**
     * Esta função é responsável por iniciar o pedido à API da Cloud Vision
     * @param bitmap
     */
    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        textViewResult.setText(R.string.loading_message);

        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, CloudVisionAPI.prepareAnnotationRequest(bitmap, this));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Toast.makeText(this, "Ocorreu um erro a analisar a imagem", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Esta função é responsável por processar a resposta da Cloud Vision API
     */
    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                // A enviar pedido
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (IOException e) {
                Log.d(TAG, "Não possivel comunicar com a API porque: " + e.getMessage());
            }
            return "Ocorreu um erro, consulte os logs para mais detalhes";
        }

        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView textViewResult = activity.findViewById(R.id.textViewResult);
                textViewResult.setText(result);
            }
        }
    }

    /**
     * Esta função é responsável por converter a resposta da Cloud Vision API para texto
     * @param response
     * @return
     */
    private static String convertResponseToString(BatchAnnotateImagesResponse response) {

        StringBuilder message = new StringBuilder("Foram encontradas as seguintes classes:\n\n");

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("Não foi detectado nenhuma classe conhecida");
        }

        return message.toString();
    }
    /*********************************************************************************************/


}
