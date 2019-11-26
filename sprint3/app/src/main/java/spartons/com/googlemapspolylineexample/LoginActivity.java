package spartons.com.googlemapspolylineexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.content.pm.PackageManager;
//import android.support.v4.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
//import android.content.Intent;
//import android.support.v7.widget.AppCompatSpinner;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // sign in
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;
//import com.google.maps.errors.ApiException;

public class LoginActivity extends AppCompatActivity {
    //private static final int RC_SIGN_IN = 1;
    private static final String TAG = "AndroidClarified";
    private GoogleSignInClient googleSignInClient;
    private SignInButton googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        googleSignInButton = findViewById(R.id.sign_in_button);

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent signInIntent = googleSignInClient.getSignInIntent();
               startActivityForResult(signInIntent, 101);
            }
        });
        //findViewById(R.id.sign_in_button).setOnClickListener(this);
    }
//    private void signIn() {
//        Intent signInIntent = GoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 101:
                    try {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        onLoggedIn(account);
                    } catch (ApiException e) {
                        Log.w(TAG, "signInResult: failed code=" + e.getStatusCode());
                    }
                    break;
            }
    }

    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
        Intent intent = new Intent(this, MainActivity.class);//should be main activity
        intent.putExtra(MainActivity.ACCOUNT_SERVICE, googleSignInAccount);

        startActivity(intent);
        finish();
    }
}
