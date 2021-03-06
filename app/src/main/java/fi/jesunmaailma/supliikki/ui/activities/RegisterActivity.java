package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import fi.jesunmaailma.supliikki.R;

public class RegisterActivity extends AppCompatActivity {
    public static final int GOOGLE_REQ_CODE = 100;

    EditText firstNameEdit, lastNameEdit, emailEdit, passwordEdit;
    MaterialButton registerBtn;
    TextView btnReadMore, loginActivityBtn, btnForgotPassword;

    SignInButton btnSignInWithGoogle;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore database;

    ProgressBar pbLoading;

    GoogleSignInClient client;

    AlertDialog.Builder builder;
    LayoutInflater inflater;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnReadMore = findViewById(R.id.btn_read_more);
        btnSignInWithGoogle = findViewById(R.id.btn_sign_in_with_google);
        registerBtn = findViewById(R.id.registerBtn);
        loginActivityBtn = findViewById(R.id.login_activity_btn);
        pbLoading = findViewById(R.id.pb_loading);

        btnForgotPassword = findViewById(R.id.forgotPasswordBtn);

        firstNameEdit = findViewById(R.id.firstNameEdit);
        lastNameEdit = findViewById(R.id.lastNameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);

        builder = new AlertDialog.Builder(this);
        inflater = getLayoutInflater();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        ).requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(RegisterActivity.this, gso);

        firstNameEdit.addTextChangedListener(watcher);
        lastNameEdit.addTextChangedListener(watcher);
        emailEdit.addTextChangedListener(watcher);
        passwordEdit.addTextChangedListener(watcher);

        btnReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jesun_maailma_tili_url = "https://finnplace.ml/jesun-maailma-tili";

                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(jesun_maailma_tili_url)
                );
                startActivity(intent);
            }
        });

        btnSignInWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbLoading.setVisibility(View.VISIBLE);
                registerBtn.setEnabled(false);
                Intent intent = client.getSignInIntent();
                startActivityForResult(intent, GOOGLE_REQ_CODE);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = firstNameEdit.getText().toString();
                String lastName = lastNameEdit.getText().toString();
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                if (firstName.isEmpty()) {
                    firstNameEdit.setError("Etunimi vaaditaan.");
                    return;
                }

                if (lastName.isEmpty()) {
                    lastNameEdit.setError("Sukunimi vaaditaan.");
                    return;
                }

                if (email.isEmpty()) {
                    emailEdit.setError("S??hk??posti vaaditaan.");
                    return;
                }

                if (password.isEmpty()) {
                    passwordEdit.setError("Salasana vaaditaan.");
                    return;
                }

                if (password.length() < 8) {
                    passwordEdit.setError("Salasanan t??ytyy olla 8 merkin pituinen.");
                    return;
                }

                pbLoading.setVisibility(View.VISIBLE);
                registerBtn.setEnabled(false);

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            uid = auth.getCurrentUser().getUid();
                            DocumentReference documentReference = database.collection("Users").document(uid);

                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("firstName", firstName);
                            userDetails.put("lastName", lastName);
                            userDetails.put("email", email);
                            documentReference.set(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        updateUI();
                                    } else {
                                        pbLoading.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),
                                                "Virhe! " + task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Virhe! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            pbLoading.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = inflater.inflate(R.layout.forgot_password_dialog, null);
                builder.setTitle("Unohditko salasanasi?")
                        .setMessage("Sy??t?? s??hk??postisi saadaksesi salasanan palautuslinkin.")
                        .setPositiveButton("Palauta", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText email = view.findViewById(R.id.emailEdit);
                                String emailVal = email.getText().toString();

                                if (emailVal.isEmpty()) {
                                    email.setError("Pakollinen.");
                                    return;
                                }

                                auth.sendPasswordResetEmail(emailVal).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Salasanan palautuslinkki l??hetetty.", Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Peruuta", null)
                        .setView(view)
                        .create().show();
            }
        });

        loginActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_REQ_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task
                            .getResult(ApiException.class);
                    if (account != null) {
                        AuthCredential credential = GoogleAuthProvider
                                .getCredential(account.getIdToken(), null);
                        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    updateUI();
                                }
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            } else {
                pbLoading.setVisibility(View.GONE);
            }
        }
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String firstName = firstNameEdit.getText().toString();
            String lastName = lastNameEdit.getText().toString();
            String email = emailEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            registerBtn.setEnabled(!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !password.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void updateUI() {
        pbLoading.setVisibility(View.GONE);
        startActivity(new Intent(getApplicationContext(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
        overridePendingTransition(0, 0);
    }
}