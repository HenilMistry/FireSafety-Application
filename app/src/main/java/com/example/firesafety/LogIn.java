package com.example.firesafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogIn extends AppCompatActivity {

    TextInputEditText Email, Password;
    MaterialButton BtnLogIn;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userId;
    private boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        Email = findViewById(R.id.login_email);
        Password = findViewById(R.id.login_password);

        BtnLogIn = findViewById(R.id.login);

        firebaseAuth = FirebaseAuth.getInstance();

        BtnLogIn.setOnClickListener(view -> {
            String email = Objects.requireNonNull(Email.getText()).toString();
            String password = Objects.requireNonNull(Password.getText()).toString();

            if (isValidValidEmail(email)) {
                if (isValidPassword(password)) {
                    loginUser(email, password);
                } else {
                    Toast.makeText(this, "Invalid Password...", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void redirect() {
        if (admin) {
            startActivity(new Intent(LogIn.this, FireAlaram.class));
        } else {
            startActivity(new Intent(LogIn.this, MainActivity.class));
        }
        finish();
    }

    private void loginUser(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Query q = FirebaseDatabase.getInstance().getReference("users");

                        q.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot ss : snapshot.getChildren()) {
                                        UserInfo user = ss.getValue(UserInfo.class);
                                        if (user.getEmail().compareTo(email)==0) {
                                            if (user.isAdmin) {
                                                admin = true;
                                            }
                                        } else {
                                            System.out.println("Not an intended user...");
                                        }
                                    }
                                    redirect();
                                } else {
                                    System.out.println("Error Occurred!");
                                    Toast.makeText(LogIn.this, "Successfully logged in.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LogIn.this, "Login Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static boolean isValidValidEmail(String email) {
        String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(EMAIL_PATTERN);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        final Pattern PASSWORD_PATTERN =
                Pattern.compile("^" +           // represents starting char
                        "(?=.*[0-9])" +         // represents a digit must occur at least once.
                        "(?=.*[a-z])" +         // represents a lower case alphabet must occur at least once.
                        "(?=.*[A-Z])" +         // represents an upper case alphabet that must occur at least once.
                        "(?=.*[@#$%&-+=()])" +     // represents a special character that must occur at least once.
                        "(?=\\S+$)" +           // white spaces don’t allowed in the entire string.
                        ".{8,20}" +             //  represents at least 8 characters and at most 20 characters.
                        "$");                   // represents the end of the string.


        // password is empty
        if (password == null || password.isEmpty()) {
            return false;
        }

        Matcher matcher = PASSWORD_PATTERN.matcher(password);

        return matcher.matches();
    }

    public void signUp(View view) {
        startActivity(new Intent(LogIn.this, SignUp.class));
        finish();
    }
}