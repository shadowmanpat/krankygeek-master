package com.example.sm.ratingapp;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public  static String urlLink="https://play.google.com/store/apps/details?id=xxx.iku&_branch_match_id=441937105458344124";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




//        SharedPreferences preferences = getSharedPreferences("progress", MODE_PRIVATE);
//        int appUsedCount = preferences.getInt("appUsedCount",0);
//        appUsedCount++;
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt("appUsedCount", appUsedCount);
//        editor.apply();

//        if (appUsedCount==10 || appUsedCount==50 || appUsedCount==100 || appUsedCount==200 || appUsedCount==300){
//            AskForRating(appUsedCount);
//        } else {
//            finish();
//        }
    }

    public void rateMe(View v){
        //Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Uri uri = Uri.parse("market://details?id=xxx.iku&_branch_match_id=441937105458344124" );
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                   // Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                    Uri.parse(urlLink)));
        }
    }

    public  void rateMe1(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Rate application")
                .setMessage("Please, rate the app at PlayMarket")
                .setPositiveButton("RATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (getApplicationContext() != null) {


                            MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(urlLink)));
                        }

                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=xxx.iku&_branch_match_id=441937105458344124")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlLink )));
                        }
                    }
                })
                .setNegativeButton("CANCEL", null);
        builder.show();
    }

    public  void rateMe2(View v){
        AppRater.app_launched(this);

    }
    private void AskForRating(int _appUsedCount){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Please Rate Us");
        alert.setIcon(R.drawable.logo);
        alert.setMessage("Thanks for using the application. If you like ROCKET SOCIAL please rate us! Your feedback is important for us!");
        alert.setPositiveButton("Rate it",new Dialog.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(urlLink));
                startActivity(i);
            }
        });
        alert.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.show();
    }
}
