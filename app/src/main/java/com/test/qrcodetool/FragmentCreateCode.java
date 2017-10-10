package com.test.qrcodetool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.kevin.crop.UCrop;
import com.test.qrcodetool.utils.ImagePicker;
import com.test.qrcodetool.utils.ImageUtil;
import com.test.qrcodetool.utils.Md5Util;
import com.test.qrcodetool.utils.QrcodeUtil;
import com.test.qrcodetool.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by WTZ on 2017/10/8.
 */

public class FragmentCreateCode extends Fragment {
    private static final String TAG = FragmentCreateCode.class.getSimpleName();

    /**
     * 将要生成二维码的内容
     */
    private EditText codeEdit;

    /**
     * 生成二维码代码
     */
    private Button twoCodeBtn;
    /**
     * 用于展示生成二维码的imageView
     */
    private ImageView codeImg;

    /**
     * 生成一维码按钮
     */
    private Button oneCodeBtn;

    private LinearLayout saveShareLayout;
    private Button setLogoBtn;
    private Button saveCodeBtn;
    private Button shareCodeBtn;

    private TextView saveDirTips;

    private String codeContent;
    private Bitmap codeBitmap;
    private File saveFile;

    private static final String SAVE_DIR_ROOT = "qrcode";
    private static final String SAVE_DIR_SUB_ICON = SAVE_DIR_ROOT + File.separator + "icon";
    private static final String SAVE_NAME_SUFFIX = ".jpg";

    private boolean isSaving;
    private boolean isQrcode;

    // 拍照临时图片
    private String mTempPhotoPath;
    // 剪切后图像文件
    private Uri mDestinationUri;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDestinationUri = Uri.fromFile(new File(getActivity().getCacheDir(), "cropImage.jpeg"));
        mTempPhotoPath = getIconSavePath() + File.separator + "tempPhoto.jpeg";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_create_code, container, false);
        codeEdit = (EditText) rootView.findViewById(R.id.code_edittext);
        twoCodeBtn = (Button) rootView.findViewById(R.id.code_btn);
        oneCodeBtn = (Button) rootView.findViewById(R.id.btn_code);
        codeImg = (ImageView) rootView.findViewById(R.id.code_img);
        saveShareLayout = (LinearLayout) rootView.findViewById(R.id.ll_save_share);
        saveCodeBtn = (Button) rootView.findViewById(R.id.btn_save);
        shareCodeBtn = (Button) rootView.findViewById(R.id.btn_share);
        saveDirTips = (TextView) rootView.findViewById(R.id.tv_save_tips);
        setLogoBtn = (Button) rootView.findViewById(R.id.btn_set_logo);

        twoCodeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isQrcode = true;
                setColorForQrcodeDialog();
            }
        });

        oneCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isQrcode = false;
                codeContent = codeEdit.getText().toString().trim();
                if (TextUtils.isEmpty(codeContent)) {
                    return;
                }
                int size = codeContent.length();
                for (int i = 0; i < size; i++) {
                    int c = codeContent.charAt(i);
                    if ((19968 <= c && c < 40623)) {
                        Toast.makeText(FragmentCreateCode.this.getContext(), "生成条形码的内容不能是中文", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
                codeBitmap = null;
                saveShareLayout.setVisibility(View.INVISIBLE);
                setLogoBtn.setVisibility(View.GONE);
                shareCodeBtn.setVisibility(View.GONE);
                saveDirTips.setVisibility(View.INVISIBLE);
                try {
                    if (!TextUtils.isEmpty(codeContent)) {
                        codeBitmap = QrcodeUtil.CreateOneDCode(codeContent);
                    }
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                if (codeBitmap != null) {
                    codeImg.setImageBitmap(codeBitmap);
                    saveShareLayout.setVisibility(View.VISIBLE);
                    saveDirTips.setVisibility(View.VISIBLE);
                }
            }
        });

        saveShareLayout.setVisibility(View.INVISIBLE);
        shareCodeBtn.setVisibility(View.GONE);
        saveDirTips.setVisibility(View.INVISIBLE);
        saveCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSaving) {
                    return;
                }
                isSaving = true;
                startSaveTask();
            }
        });

        shareCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                //此处一定要用Uri.fromFile(file),其中file为File类型，否则附件无法发送成功。
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(saveFile));
                share.setType("image/jpeg");
                startActivity(Intent.createChooser(share, "Share Image"));
            }
        });

        String format = getString(R.string.format_save_dir);
        String result = String.format(format, getCodeSavePath());
        saveDirTips.setText(result);

        setLogoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLogoDialog();
            }
        });

        return rootView;
    }

    private void createQrcode(boolean isColorful) {
        codeContent = codeEdit.getText().toString().trim();
        if (TextUtils.isEmpty(codeContent)) {
            return;
        }
        codeBitmap = null;
        saveShareLayout.setVisibility(View.INVISIBLE);
        setLogoBtn.setVisibility(View.GONE);
        shareCodeBtn.setVisibility(View.GONE);
        saveDirTips.setVisibility(View.INVISIBLE);
        try {
            if (isColorful) {
                codeBitmap = QrcodeUtil.CreateColorQrCode(codeContent, 600, 600);
            } else {
                codeBitmap = QrcodeUtil.CreateQrCode(codeContent, 600, 600);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (codeBitmap != null) {
            codeImg.setImageBitmap(codeBitmap);
            setLogoBtn.setVisibility(View.VISIBLE);
            saveShareLayout.setVisibility(View.VISIBLE);
            saveDirTips.setVisibility(View.VISIBLE);
        }
    }

    private void startSaveTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveCodeBitmap(codeContent, codeBitmap, false);
                isSaving = false;
                FragmentCreateCode.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shareCodeBtn.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), "已保存", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

    private File getCodeSavePath() {
        return FileUtil.getStorageDir(getContext(), SAVE_DIR_ROOT);
    }

    private File getIconSavePath() {
        return FileUtil.getStorageDir(getContext(), SAVE_DIR_SUB_ICON);
    }

    private void saveCodeBitmap(String content, Bitmap bitmap, boolean recycle) {
        if (content == null) {
            Toast.makeText(getActivity(), "内容为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bitmap == null) {
            Toast.makeText(getActivity(), "图像为空", Toast.LENGTH_SHORT).show();
            return;
        }

        File dir = getCodeSavePath();
        if (dir == null) {
            Toast.makeText(getActivity(), "获取存储目录失败", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dir.getAbsolutePath());
        stringBuilder.append(File.separator);

        if (isQrcode) {
            stringBuilder.append("qrcode_");
        } else {
            stringBuilder.append("barcode_");
        }

        String md5 = Md5Util.getStringMD5(content);
        stringBuilder.append(md5);

        stringBuilder.append(SAVE_NAME_SUFFIX);

        String filePath = stringBuilder.toString();

        saveFile = FileUtil.bitmapToFile(bitmap, filePath, recycle);

        // 把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                    filePath, new File(filePath).getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 通知图库更新
        getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + "/sdcard/")));
    }

    private void setLogoDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("设置LOGO")
                .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ImagePicker.pickFromGallery(FragmentCreateCode.this, new File(mTempPhotoPath));
                    }
                })
                .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ImagePicker.pickByCamera(FragmentCreateCode.this, new File(mTempPhotoPath));
                    }
                }).show();
    }


    private void setColorForQrcodeDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("选择颜色")
                .setNegativeButton("黑白", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        createQrcode(false);
                    }
                })
                .setPositiveButton("彩色", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        createQrcode(true);
                    }
                }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImagePicker.ACTIVITY_REQUESTCODE_CAMERA:   // 调用相机拍照
                    File temp = new File(mTempPhotoPath);
                    ImagePicker.startCropActivity(Uri.fromFile(temp), mDestinationUri, this);
                    break;
                case ImagePicker.ACTIVITY_REQUESTCODE_GALLERY:  // 直接从相册获取
                    ImagePicker.startCropActivity(data.getData(), mDestinationUri, this);
                    break;
                case UCrop.REQUEST_CROP:    // 裁剪图片结果
                    ImagePicker.handleCropResult(getContext(), data, new ImagePicker.OnPictureSelectedListener() {
                        @Override
                        public void onPictureSelected(Uri fileUri, Bitmap bitmap) {
                            if (codeBitmap != null) {
                                Bitmap logo = ImageUtil.roundBitmap(bitmap);
                                codeBitmap = QrcodeUtil.addLogoForQRCode(codeBitmap, logo);
                                codeImg.setImageBitmap(codeBitmap);
                            }
                        }
                    });
                    break;
                case UCrop.RESULT_ERROR:    // 裁剪图片错误
                    ImagePicker.handleCropError(getContext(), data);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
