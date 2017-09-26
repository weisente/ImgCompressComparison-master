package top.weisente.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import net.bither.util.NativeUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PICK_IMAGE = 10011;
    public static final int REQUEST_KITKAT_PICK_IMAGE = 10012;

    private List<ImageBean> mImageList = new ArrayList<>();
    private ImageAdapter mAdapter = new ImageAdapter(mImageList);
    Activity activity;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        View fab = findViewById(R.id.fab);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter = new ImageAdapter(mImageList);
                recyclerView.setAdapter(mAdapter);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                            REQUEST_PICK_IMAGE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_KITKAT_PICK_IMAGE);
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            showResult(getFileByUri(data.getData()),getFileByUri(data.getData())," type=没压缩");

            switch (requestCode) {

                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        Uri uri = data.getData();
                        compressImageByJni(uri);
                        compressImageByLuban(uri);

                    } else {
                    }
                    break;
                case REQUEST_KITKAT_PICK_IMAGE:
                    if (data != null) {
                        Uri uri = data.getData();
                        compressImageByJni(uri);
                        compressImageByLuban(uri);
                    } else {

                    }
                    break;
            }
        }
    }
    public static long oldtime;
    public void compressImageByLuban(final Uri uri){

        Luban.with(this)
                .load(FileTools.instance().getPath(getApplicationContext(), uri))                                   // 传人要压缩的图片列表
                .ignoreBy(100)                                  // 忽略不压缩图片的大小
                .setTargetDir(Environment.getExternalStorageDirectory().getAbsolutePath() )                       // 设置压缩后文件存储位置
                .setCompressListener(new OnCompressListener() { //设置回调
                    @Override
                    public void onStart() {
                        // TODO 压缩开始前调用，可以在方法内启动 loading UI
                        oldtime = System.currentTimeMillis();
                    }

                    @Override
                    public void onSuccess(File file) {
                        // TODO 压缩成功后调用，返回压缩后的图片文件

                        showResult(getFileByUri(uri),file,"时间："+ (System.currentTimeMillis() - oldtime )+"   type=Luban");
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO 当压缩过程出现问题时调用
                        Toast.makeText(getApplicationContext(),"失败",Toast.LENGTH_SHORT).show();
                    }
                }).launch();    //启动压缩

    }

    public void compressImageByJni(Uri uri) {
        try {
            long old = System.currentTimeMillis();
            File saveFile = new File(Environment.getExternalStorageDirectory(), "compress_" +old + ".jpg");
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            NativeUtil.compressBitmap(bitmap, saveFile.getAbsolutePath());

            showResult(getFileByUri(uri),saveFile,"时间："+ (System.currentTimeMillis() - old)+"   type=JNI");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showResult(File file1, File file,String TimeAndtype) {
        int[] originSize = computeSize(file1.getAbsolutePath());
        int[] thumbSize = computeSize(file.getAbsolutePath());
        String originArg = String.format(Locale.CHINA, "原图参数：%d*%d, %dk", originSize[0], originSize[1],file1.length() >> 10);
        String thumbArg = String.format(Locale.CHINA, "压缩后参数：%d*%d, %dk", thumbSize[0], thumbSize[1], file.length() >> 10);

        ImageBean imageBean = new ImageBean(originArg, thumbArg,TimeAndtype,file.getAbsolutePath());
        mImageList.add(imageBean);
        mAdapter.notifyDataSetChanged();
    }

    private int[] computeSize(String srcImg) {
        int[] size = new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeFile(srcImg, options);
        size[0] = options.outWidth;
        size[1] = options.outHeight;
        return size;
    }

    public File getFileByUri(Uri uri) {
        String path = FileTools.instance().getPath(getApplicationContext(), uri);
        return  new File(path);
    }



}
