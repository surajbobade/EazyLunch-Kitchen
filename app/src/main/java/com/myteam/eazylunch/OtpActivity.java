package com.myteam.eazylunch;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukesh.OnOtpCompletionListener;
import com.myteam.eazylunch.databinding.ActivityOtpBinding;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    ActivityOtpBinding binding;
    FirebaseAuth auth;
    String verificationId;
    ProgressDialog dialog;
    FirebaseFirestore rootRef;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(ContextCompat.getColor(OtpActivity.this,R.color.primary));

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP");
        dialog.setCancelable(false);
        dialog.show();

        rootRef = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        String phoneNumber =getIntent().getStringExtra("phoneNumber");
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(OtpActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) { }
                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) { }
                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);

                        dialog.dismiss();
                        verificationId = verifyId;

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        binding.otpView.requestFocus();
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            DocumentReference docIdRef = rootRef.collection("restaurants").document(auth.getCurrentUser().getPhoneNumber());
                            if(docIdRef!=null){
                                Toast.makeText(OtpActivity.this,"Verification completed",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(OtpActivity.this,OrderActivity.class));
                                finish();
                            }else{
                                Toast.makeText(OtpActivity.this,"Verification completed",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(OtpActivity.this,ResDetails.class));
                                finish();
                            }
                        }else{
                            Toast.makeText(OtpActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(OtpActivity.this,SignIn.class));
                            finish();
                        }
                    }
                });
            }
        });
    }
}
