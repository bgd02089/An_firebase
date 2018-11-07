package an_firebase.example.com.an_firebase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    String FileName;
    EditText EditName;
    String email = "bgd02089@gmail.com";
    String password = "bgd02089";

    private ImageView ivPreview;
    private VideoView vvPreview;
    private int imageIndex;

    private Uri filePath;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        Button btChoose = (Button) findViewById(R.id.bt_choose);
        Button btUpload = (Button) findViewById(R.id.bt_upload);
        Button btDownload = (Button) findViewById(R.id.bt_download);
        Button btCheck = (Button) findViewById(R.id.bt_check);
        ivPreview = (ImageView) findViewById(R.id.iv_preview);
        vvPreview = (VideoView) findViewById(R.id.vv_preview);

        EditName = (EditText) findViewById(R.id.txt_filename);
        final StorageReference imageReference = FirebaseStorage.getInstance().getReference().child("images");

        StorageReference fileRef = null;
        progressDialog = new ProgressDialog(this);

        //버튼 클릭 이벤트

        btChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택
                Intent intent = new Intent();
                intent.setType("image/*"); // image/*
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
                //imageIndex = 0;

                if (intent.getType() == "mp4")
                    imageIndex = 1;
                else
                    imageIndex = 0;
            }
        });

        btCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //로그인
                signInWithEmailAndPassword();
            }
        });
        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //업로드
                uploadFile();
            }
        });
        btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //다운로드
                downloadFile();
            }
        });
    }

    public void signInWithEmailAndPassword() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Authentication Success.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "AAuthentication failedd.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        //updateUI(null);
    }

    //결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (imageIndex == 0) {
                ivPreview.setVisibility(View.VISIBLE);
                vvPreview.setVisibility(View.INVISIBLE);

                filePath = data.getData();
                Log.d(TAG, "uri:" + String.valueOf(filePath));
                try {
                    //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    ivPreview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ivPreview.setVisibility(View.INVISIBLE);
                vvPreview.setVisibility(View.VISIBLE);

                vvPreview = (VideoView)findViewById(R.id.vv_preview);
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(vvPreview);
                vvPreview.setVideoPath(String.valueOf(filePath));
                vvPreview.setMediaController(mediaController);
                vvPreview.start();
            }
        }
    }


    FirebaseStorage storage = FirebaseStorage.getInstance();

            //upload the file
            private void uploadFile() {
                String filename;
                StorageReference storageRef = null;

                //업로드할 파일이 있으면 수행
                if (filePath != null) {
                    //업로드 진행 Dialog 보이기
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("업로드중...");
                    progressDialog.show();

                    //Unique한 파일명을 만들자.
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
                    Date now = new Date();

                    /*filename = formatter.format(now);
                    if(filename.contains("IMG"))
                        imageIndex = 0;
                    else
                        imageIndex = 1;
                    */
                    //if (imageIndex == 0) {
                        filename = formatter.format(now) + ".png";
                        storageRef = storage.getReferenceFromUrl("gs://an-firebase.appspot.com").child("images/" + filename);
                    //} else
                    //    filename = formatter.format(now) + ".mp4";
                    //storageRef = storage.getReferenceFromUrl("gs://an-firebase.appspot.com").child("videos/" + filename);

                    storageRef.putFile(filePath)
                        //성공시
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                                Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        //실패시
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        //진행중
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                        double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                //dialog에 진행률을 퍼센트로 출력해 준다
                                progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                            }
                        });
            } else {
                    Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
                }
            }


            private void downloadFile() {
                vvPreview.setVisibility(View.INVISIBLE);
                ivPreview.setVisibility(View.INVISIBLE);

                FileName = EditName.getText().toString();

                if(FileName.contains("mp4"))
                    imageIndex = 1;
                else
                    imageIndex = 0;

                StorageReference mStorageRef;

                if (imageIndex == 0) {
                    mStorageRef = storage.getReferenceFromUrl("gs://an-firebase.appspot.com").child("images/" + FileName);
                } else {
                    mStorageRef = storage.getReferenceFromUrl("gs://an-firebase.appspot.com").child("videos/" + FileName);
                }
                Toast.makeText(MainActivity.this, "Filename : " + FileName, Toast.LENGTH_LONG).show();

                progressDialog.setTitle("Downloading...");
                progressDialog.setMessage(null);
                progressDialog.show();

                if (imageIndex == 0) {
                    final long ONE_MEGABYTE = 1024 * 1024;
                    try {
                        final File localFile = File.createTempFile("images", "jpeg");

                        mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                ivPreview.setVisibility(View.VISIBLE);
                                Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                ivPreview.setImageBitmap(bmp);
                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                // percentage in progress dialog
                                progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    final long ONE_MEGABYTE = 1024 * 1024;
                    try {
                        final File localFile = File.createTempFile("videos", "mp4");

                        mStorageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                vvPreview.setVisibility(View.VISIBLE);
                                vvPreview.setVideoPath(String.valueOf(localFile));
                                vvPreview.start();
                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                // percentage in progress dialog
                                progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }