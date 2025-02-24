package org.nstu.bus_tracker;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.ghyeok.stickyswitch.widget.StickySwitch;

public class LauncherActivity extends AppCompatActivity {

    EditText user_password;
    TextView password_label;
    TextView user_id_label;
    FirebaseAuth mAuth;
    EditText token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        final StickySwitch stickySwitch = findViewById(R.id.sticky_switch);

        Button submit_login = findViewById(R.id.button_login);
        Button submit_register = findViewById(R.id.button_register);
        final EditText user_email = findViewById(R.id.user_email);
        final EditText license_no = findViewById(R.id.license_no);
        user_password = findViewById(R.id.user_password);
        password_label = findViewById(R.id.password_label);
        user_id_label = findViewById(R.id.user_id_label);
        token = findViewById(R.id.token_key);

        stickySwitch.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NotNull StickySwitch.Direction direction, @NotNull String text) {
                updateLabel(direction == StickySwitch.Direction.LEFT);
            }
        });

        submit_register.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email, pass, tok;
                        email = user_email.getText().toString().toLowerCase().replaceAll("\\s", "");
                        pass = user_password.getText().toString();
                        tok = token.getText().toString();

                        if (TextUtils.isEmpty(tok)) {
                            showToast("Enter Token");
                            return;
                        }
                        if (!tok.equals("cste14201819")) {
                            showToast("Wrong Token");
                            token.setText("");
                            return;
                        }

                        if (TextUtils.isEmpty(email)) {
                            showToast("Enter Username");
                            return;
                        }

                        if (TextUtils.isEmpty(pass)) {
                            showToast("Enter Password");
                            return;
                        }

                        mAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            showToast("Account Created Successfully.");
                                            token.setText("");
                                        } else {
                                            showToast("Authentication failed: " + task.getException().getMessage());
                                            token.setText("");
                                        }
                                    }
                                });
                    }
                }
        );

        submit_login.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        boolean is_student = stickySwitch.getDirection() == StickySwitch.Direction.LEFT;

                        Log.e("student bool", "" + is_student);

                        if (is_student) {
                            String id, lic;
                            lic = license_no.getText().toString();
                            Log.e(LauncherActivity.class.getSimpleName(), lic);
                            id = user_email.getText().toString();
                            if (TextUtils.isEmpty(id)) {
                                showToast("Enter Any Id");
                                return;
                            }
                            FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference myOwnBusDatabaseReference = mFirebaseDatabase.getReference().child(Helper.CHILD_NAME_FIREBASE).child(lic);
                            if (myOwnBusDatabaseReference != null) {
                                myOwnBusDatabaseReference.removeValue();
                            }
                            Intent intent = new Intent(LauncherActivity.this, MapActivity.class);
                            intent.putExtra("is_this_a_driver", false);
                            startActivity(intent);
                            finish();
                        } else {
                            String email, pass, lic;
                            email = user_email.getText().toString().toLowerCase().replaceAll("\\s", "");
                            pass = user_password.getText().toString();
                            lic = license_no.getText().toString();

                            if (TextUtils.isEmpty(email)) {
                                showToast("Enter Username");
                                return;
                            }

                            if (TextUtils.isEmpty(pass)) {
                                showToast("Enter Password");
                                return;
                            }

                            if (TextUtils.isEmpty(lic)) {
                                showToast("Enter License No");
                                return;
                            }

                            mAuth.signInWithEmailAndPassword(email, pass)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(LauncherActivity.this, MapActivity.class);
                                                intent.putExtra("DRIVER_ID", user_email.getText().toString());
                                                if (!license_no.getText().toString().equals(""))
                                                    intent.putExtra("LICENSE_NO", license_no.getText().toString().toUpperCase());
                                                intent.putExtra("is_this_a_driver", true);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                showToast("Authentication failed: " + task.getException().getMessage());
                                            }
                                        }
                                    });
                        }
                    }
                }
        );

        boolean is_student = stickySwitch.getDirection() == StickySwitch.Direction.LEFT;
        updateLabel(is_student);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        Log.e("onNewIntent", intent.getStringExtra("DRIVER_ID"));
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    private void updateLabel(boolean is_student) {
        final EditText license_no = findViewById(R.id.license_no);
        final TextView license_label = findViewById(R.id.license_label);
        final Button submit_register = findViewById(R.id.button_register);
        final TextView token_label = findViewById(R.id.token_label);
        final EditText token_key = findViewById(R.id.token_key);
        final TextView user_info = findViewById(R.id.user_id_info);
        Log.e("is a stu", is_student + "");
        if (is_student) {
            user_id_label.setText("User ID:");
            user_info.setVisibility(View.VISIBLE);
            user_password.setEnabled(false);
            user_password.setVisibility(View.GONE);
            password_label.setVisibility(View.GONE);
            license_label.setVisibility(View.GONE);
            license_no.setVisibility(View.GONE);
            token_label.setVisibility(View.GONE);
            token_key.setVisibility(View.GONE);
            submit_register.setVisibility(View.GONE);
        } else {
            submit_register.setVisibility(View.VISIBLE);
            user_id_label.setText("Username:");
            user_info.setVisibility(View.GONE);
            license_label.setText("License No:");
            user_password.setEnabled(true);
            user_password.setVisibility(View.VISIBLE);
            password_label.setVisibility(View.VISIBLE);
            license_label.setVisibility(View.VISIBLE);
            license_no.setVisibility(View.VISIBLE);
            token_label.setVisibility(View.VISIBLE);
            token_key.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(LauncherActivity.this, message, Toast.LENGTH_LONG);
        View t_view = toast.getView();
        if (t_view != null) {
            t_view.getBackground().setColorFilter(getResources().getColor(R.color.submit_button), PorterDuff.Mode.SRC_IN);
            TextView text = t_view.findViewById(android.R.id.message);
            if (text != null) {
                text.setTextColor(getResources().getColor(R.color.colorSwitchColor));
            }
        }
        toast.show();
    }
}
