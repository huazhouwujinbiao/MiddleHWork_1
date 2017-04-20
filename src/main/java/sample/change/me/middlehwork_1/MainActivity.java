package sample.change.me.middlehwork_1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
//    private static  String WEB_URL="file:///android_asset/index.html";
    private static  String WEB_URL="file:///android_asset/test.html";
    private WebView webview;
    private UploadHandler mUploadHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebChromeClient(new MyChromeViewClient());
        webview.setWebViewClient(new MyWebViewClinet());
        //设置不用系统浏览器打开,直接显示在当前Webview
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                //这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
/*                new AlertDialog.Builder(MainActivity.this).setTitle("请输入").setIcon(android.R.drawable.ic_dialog_info).setView(edit=new EditText(MainActivity.this)).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        WEB_URL =edit.getText().toString();
                        SharedPreferencesUtils.setParam(MainActivity.this, "String", WEB_URL);
                        webview.loadUrl(WEB_URL);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();*/
            }
        });

        initWebViewSettings();
        initData();

    }


    private void initWebViewSettings() {
        WebSettings settings = webview.getSettings();
//        设置与Js交互的权限
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        String dbPath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabasePath(dbPath);

    }

    private void initData() {
    /*    Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        webview.loadUrl(url);*/
        //  String url =(String) SharedPreferencesUtils.getParam(MainActivity.this, "String", "");
        // if(!TextUtils.isEmpty(url)){
        webview.loadUrl(WEB_URL);
        //}else webview.loadUrl(WEB_URL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == Controller.FILE_SELECTED) {
            // Chose a file from the file picker.
            if (mUploadHandler != null) {
                mUploadHandler.onResult(resultCode, intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断是否可以返回操作
        if (webview.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    class  MyDownloadListener implements DownloadListener {


        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            // TODO Auto-generated method stub

        }

    }

    class MyChromeViewClient extends WebChromeClient {

        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater)
        {
//          quotaUpdater.updateQuota(estimatedSize * 2);
            quotaUpdater.updateQuota(5*1024*1024);
        }
        @Override
        public void onCloseWindow(WebView window) {
            MainActivity.this.finish();
            super.onCloseWindow(window);
        }

        public void onProgressChanged(WebView view, final int progress) {

        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

            new AlertDialog.Builder(MainActivity.this).setTitle("提示信息").setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    }).setCancelable(false).create().show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {

            new AlertDialog.Builder(MainActivity.this).setTitle("提示信息").setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            }).setCancelable(false).create().show();
            return true;

        }

        // Android 2.x
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // Android 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, "", "filesystem");
        }

        // Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

            mUploadHandler = new UploadHandler(new Controller());
            mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
        }

    }

    class MyWebViewClinet extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return true;
        }

    }



    // copied from android-4.4.3_r1/src/com/android/browser/UploadHandler.java

    class UploadHandler {
        /*
         * The Object used to inform the WebView of the file to upload.
         */
        private ValueCallback<Uri> mUploadMessage;
        private String mCameraFilePath;
        private boolean mHandled;
        private boolean mCaughtActivityNotFoundException;
        private Controller mController;

        public UploadHandler(Controller controller) {
            mController = controller;
        }

        public String getFilePath() {
            return mCameraFilePath;
        }

        boolean handled() {
            return mHandled;
        }

        public void onResult(int resultCode, Intent intent) {
            if (resultCode == Activity.RESULT_CANCELED && mCaughtActivityNotFoundException) {
                // Couldn't resolve an activity, we are going to try again so skip
                // this result.
                mCaughtActivityNotFoundException = false;
                return;
            }
            Uri result = (intent == null || resultCode != Activity.RESULT_OK) ? null : intent.getData();

            // As we ask the camera to save the result of the user taking
            // a picture, the camera application does not return anything other
            // than RESULT_OK. So we need to check whether the file we expected
            // was written to disk in the in the case that we
            // did not get an intent returned but did get a RESULT_OK. If it was,
            // we assume that this result has came back from the camera.
            if (result == null && intent == null && resultCode == Activity.RESULT_OK) {
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    // Broadcast to the media scanner that we have a new photo
                    // so it will be added into the gallery for the user.
                    mController.getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            mUploadMessage.onReceiveValue(result);
            mHandled = true;
            mCaughtActivityNotFoundException = false;
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            final String imageMimeType = "image/*";
            final String videoMimeType = "video/*";
            final String audioMimeType = "audio/*";
            final String mediaSourceKey = "capture";
            final String mediaSourceValueCamera = "camera";
            final String mediaSourceValueFileSystem = "filesystem";
            final String mediaSourceValueCamcorder = "camcorder";
            final String mediaSourceValueMicrophone = "microphone";
            // According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
            // or 'microphone' and the default value should be 'filesystem'.
            String mediaSource = mediaSourceValueFileSystem;
            if (mUploadMessage != null) {
                // Already a file picker operation in progress.
                return;
            }
            mUploadMessage = uploadMsg;
            // Parse the accept type.
            String params[] = acceptType.split(";");
            String mimeType = params[0];
            if (capture.length() > 0) {
                mediaSource = capture;
            }
            if (capture.equals(mediaSourceValueFileSystem)) {
                // To maintain backwards compatibility with the previous implementation
                // of the media capture API, if the value of the 'capture' attribute is
                // "filesystem", we should examine the accept-type for a MIME type that
                // may specify a different capture value.
                for (String p : params) {
                    String[] keyValue = p.split("=");
                    if (keyValue.length == 2) {
                        // Process key=value parameters.
                        if (mediaSourceKey.equals(keyValue[0])) {
                            mediaSource = keyValue[1];
                        }
                    }
                }
            }
            //Ensure it is not still set from a previous upload.
            mCameraFilePath = null;
            if (mimeType.equals(imageMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamera)) {
                    // Specified 'image/*' and requested the camera, so go ahead and launch the
                    // camera directly.
                    startActivity(createCameraIntent());
                    return;
                } else {
                    // Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
                    // In all these cases we show a traditional picker filetered on accept type
                    // so launch an intent for both the Camera and image/* OPENABLE.
                    Intent chooser = createChooserIntent(createCameraIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(videoMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamcorder)) {
                    // Specified 'video/*' and requested the camcorder, so go ahead and launch the
                    // camcorder directly.
                    startActivity(createCamcorderIntent());
                    return;
                } else {
                    // Specified just 'video/*', capture=filesystem or an invalid capture parameter.
                    // In all these cases we show an intent for the traditional file picker, filtered
                    // on accept type so launch an intent for both camcorder and video/* OPENABLE.
                    Intent chooser = createChooserIntent(createCamcorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(audioMimeType)) {
                if (mediaSource.equals(mediaSourceValueMicrophone)) {
                    // Specified 'audio/*' and requested microphone, so go ahead and launch the sound
                    // recorder.
                    startActivity(createSoundRecorderIntent());
                    return;
                } else {
                    // Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
                    // In all these cases so go ahead and launch an intent for both the sound
                    // recorder and audio/* OPENABLE.
                    Intent chooser = createChooserIntent(createSoundRecorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                    startActivity(chooser);
                    return;
                }
            }
            // No special handling based on the accept type was necessary, so trigger the default
            // file upload chooser.
            startActivity(createDefaultOpenableIntent());
        }

        private void startActivity(Intent intent) {
            try {
                mController.getActivity().startActivityForResult(intent, Controller.FILE_SELECTED);
            } catch (ActivityNotFoundException e) {
                // No installed app was able to handle the intent that
                // we sent, so fallback to the default file upload control.
                try {
                    mCaughtActivityNotFoundException = true;
                    mController.getActivity().startActivityForResult(createDefaultOpenableIntent(),
                            Controller.FILE_SELECTED);
                } catch (ActivityNotFoundException e2) {
                    // Nothing can return us a file, so file upload is effectively disabled.
                    Toast.makeText(mController.getActivity(), "File uploads are disabled.", Toast.LENGTH_LONG).show();
                }
            }
        }

        private Intent createDefaultOpenableIntent() {
            // Create and return a chooser with the default OPENABLE
            // actions including the camera, camcorder and sound
            // recorder where available.
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
                    createSoundRecorderIntent());
            chooser.putExtra(Intent.EXTRA_INTENT, i);
            return chooser;
        }

        private Intent createChooserIntent(Intent... intents) {
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            chooser.putExtra(Intent.EXTRA_TITLE, "请上传图片");
            return chooser;
        }

        private Intent createOpenableIntent(String type) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(type);
            return i;
        }

        private Intent createCameraIntent() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File externalDataDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File cameraDataDir = new File(externalDataDir.getAbsolutePath() + File.separator + "yl-sn");
            cameraDataDir.mkdirs();
            mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
            return cameraIntent;
        }

        private Intent createCamcorderIntent() {
            return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }

        private Intent createSoundRecorderIntent() {
            return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        }
    }

    class Controller {

        final static int FILE_SELECTED = 4;

        Activity getActivity() {
            return MainActivity.this;
        }
    }

}