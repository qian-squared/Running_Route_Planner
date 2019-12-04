package justrun.running.routeplanner;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button button1;
    private TextView text1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();
        setContentView(R.layout.welcome);
        text1 = (TextView)findViewById(R.id.welcome_text1);
        text1.setTypeface(Typeface.createFromAsset(getAssets(),"assets/fonts/Inkfree.ttf"));


        button1 = (Button) findViewById(R.id.welcome_button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(justrun.running.routeplanner.WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
