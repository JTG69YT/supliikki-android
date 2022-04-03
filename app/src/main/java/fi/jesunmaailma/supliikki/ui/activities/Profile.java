package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import fi.jesunmaailma.supliikki.R;

public class Profile extends AppCompatActivity {
    ImageView ivProfilePic;
    TextView tvUsername, tvEmail;
    MaterialButton btnSignIn, btnSignOut, btnEditEmail, btnEditPassword, btnDeleteAccount;
    FirebaseAuth auth;
    FirebaseFirestore database;
    FirebaseUser user;
    FirebaseAnalytics analytics;
    DocumentReference documentReference;
    GoogleSignInClient client;
    Toolbar toolbar;

    BottomNavigationView bottomNavigationView;

    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        inflater = this.getLayoutInflater();

        ivProfilePic = findViewById(R.id.iv_profile);
        tvUsername = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        btnSignIn = findViewById(R.id.btn_login_link);
        btnSignOut = findViewById(R.id.btn_sign_out);
        btnEditEmail = findViewById(R.id.btn_edit_email);
        btnEditPassword = findViewById(R.id.btn_edit_password);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        client = GoogleSignIn.getClient(Profile.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);
        database = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        if (user == null) {
            tvUsername.setText("Hei tyyppi, et ole kirjautunut sisään.");
            tvEmail.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);
            btnEditEmail.setVisibility(View.GONE);
            btnEditPassword.setVisibility(View.GONE);
            btnDeleteAccount.setVisibility(View.GONE);
        } else {
            tvEmail.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);
            btnEditEmail.setVisibility(View.VISIBLE);
            btnEditPassword.setVisibility(View.VISIBLE);
            btnDeleteAccount.setVisibility(View.VISIBLE);

            documentReference = database.collection("Users").document(auth.getCurrentUser().getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        ivProfilePic.setImageResource(R.drawable.ic_account_2);
                        tvUsername.setText(String.format("%s %s", snapshot.getString("firstName"), snapshot.getString("lastName")));
                        tvEmail.setText(snapshot.getString("email"));
                    } else {
                        tvUsername.setText(user.getDisplayName());
                        tvEmail.setText(user.getEmail());
                    }
                }
            });
        }

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext()
                        , LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOutDialog(Profile.this);
            }
        });

        btnEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditEmailDialog(Profile.this);
            }
        });

        btnEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPasswordDialog(Profile.this);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAccountDialog(Profile.this);
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.mi_account);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mi_hosts:
                        startActivity(new Intent(getApplicationContext(), HostsActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.mi_home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.mi_account:
                        return true;
                }
                return false;
            }
        });
    }

    private void SignOutDialog(final Activity activity) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(String.format("%s %s", document.getString("firstName"), document.getString("lastName")));
                    builder.setMessage("Haluatko varmasti kirjautua ulos Supliikista?");
                    builder.setPositiveButton("Kirjaudu ulos", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                    overridePendingTransition(0, 0);
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(user.getDisplayName());
                    builder.setMessage("Haluatko varmasti kirjautua ulos Supliikista?");
                    builder.setPositiveButton("Kirjaudu ulos", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                    overridePendingTransition(0, 0);
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void EditEmailDialog(Activity activity) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    View view = inflater.inflate(R.layout.update_email_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Päivitä sähköposti")
                            .setMessage("Päivitä sähköpostisi täyttämällä alla oleva kenttä.")
                            .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText email = view.findViewById(R.id.emailEdit);

                                    if (email.getText().toString().isEmpty()) {
                                        email.setError("Sähköposti vaaditaan.");
                                        return;
                                    }

                                    user.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            Map<String, Object> userData = new HashMap<>();

                                            userData.put("email", email.getText().toString());

                                            documentReference.update(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(
                                                                getApplicationContext(),
                                                                "Sähköposti päivitetty.",
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                        startActivity(
                                                                new Intent(getApplicationContext(), MainActivity.class)
                                                        .addFlags(
                                                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        ));
                                                        finish();
                                                        overridePendingTransition(0, 0);
                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Virhe tapahtui jostain randomista syystä: " +
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("Peruuta", null)
                            .setView(view)
                            .create().show();
                } else {
                    View view = inflater.inflate(R.layout.update_email_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Päivitä sähköposti")
                            .setMessage("Päivitä sähköpostisi täyttämällä alla oleva kenttä.")
                            .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    EditText email = view.findViewById(R.id.emailEdit);

                                    if (email.getText().toString().isEmpty()) {
                                        email.setError("Sähköposti vaaditaan.");
                                        return;
                                    }

                                    user.updateEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Sähköposti päivitetty.",
                                                    Toast.LENGTH_LONG
                                            ).show();
                                            startActivity(
                                                    new Intent(getApplicationContext(), MainActivity.class)
                                            .addFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            ));
                                            finish();
                                            overridePendingTransition(0, 0);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Virhe tapahtui jostain randomista syystä: " +
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    });

                                }
                            }).setNegativeButton("Peruuta", null)
                            .setView(view)
                            .create().show();
                }
            }
        });
    }

    private void EditPasswordDialog(Activity activity) {
        View view = inflater.inflate(R.layout.update_password_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Päivitä salasana")
                .setMessage("Päivitä salasanasi täyttämällä alla olevat kentät.")
                .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText password = view.findViewById(R.id.passwordEdit);
                        EditText passwordConf = view.findViewById(R.id.passwordConfEdit);

                        if (password.getText().toString().isEmpty()) {
                            password.setError("Salasana vaaditaan.");
                            return;
                        }

                        if (password.getText().toString().length() < 8) {
                            password.setError("Salasanan täytyy olla 8 merkin pituinen.");
                            return;
                        }

                        if (!password.getText().toString().equals(passwordConf.getText().toString())) {
                            password.setError("Salasanat eivät täsmää.");
                            return;
                        }

                        user.updatePassword(password.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Salasana päivitetty.",
                                        Toast.LENGTH_LONG
                                ).show();
                                startActivity(
                                        new Intent(getApplicationContext(), MainActivity.class)
                                                .addFlags(
                                                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                ));
                                finish();
                                overridePendingTransition(0, 0);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Virhe tapahtui jostain randomista syystä: " +
                                                e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });

                    }
                }).setNegativeButton("Peruuta", null)
                .setView(view)
                .create().show();
    }

    private void DeleteAccountDialog(Activity activity) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setTitle(
                            String.format(
                                    "%s\n(%s)",
                                    document.getString("firstName") +
                                    " " +
                                    document.getString("lastName"),
                                    document.getString("email")
                            )
                    )
                            .setMessage(
                                    String.format(
                                            "%s\n\n%s %s\n\n%s",
                                            "Haluatko varmasti poistaa tämän tilin?",
                                            "Poistaminen tyhjentää kirjautumistietosi, jolloin et enää pääse kirjautumaan tilillesi,",
                                            "etkä voi nauttia meidän tarjoamasta sisällöstä.",
                                            "Voit aina luoda uuden tilin, jos kadut tilisi poistamista."
                                    )
                            )
                            .setPositiveButton("Kyllä, poista", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(
                                                                getApplicationContext(),
                                                                "Tilisi on poistettu onnistuneesti.\nHyvää päivänjatkoa.",
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                        auth.signOut();
                                                        updateUI();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Älä poista", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setTitle(
                            String.format(
                                    "%s\n(%s)",
                                    user.getDisplayName(),
                                    user.getEmail()
                            )
                    )
                            .setMessage(
                                    String.format(
                                            "%s\n\n%s %s\n\n%s",
                                            "Haluatko varmasti poistaa tämän tilin?",
                                            "Poistaminen tyhjentää kirjautumistietosi, jolloin et enää pääse kirjautumaan tilillesi,",
                                            "etkä voi nauttia meidän tarjoamasta sisällöstä.",
                                            "Voit aina luoda uuden tilin, jos kadut tilisi poistamista."
                                    )
                            )
                            .setPositiveButton("Kyllä, poista", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Tilisi on poistettu onnistuneesti.\nHyvää päivänjatkoa.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                auth.signOut();
                                                client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                        updateUI();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }).setNegativeButton("Älä poista", null)
                            .create().show();
                }
            }
        });
    }

    private void updateUI() {
        startActivity(
                new Intent(
                        getApplicationContext(), MainActivity.class
                ).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
        );
        finish();
        overridePendingTransition(0, 0);
    }
}