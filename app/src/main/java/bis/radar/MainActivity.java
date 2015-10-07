package bis.radar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private CircleImageView mProfilePicture;
    private Button mSignIn;
    private Button mSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                updateUI();
            }
        };

        new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
            }
        };

        mProfilePicture = (CircleImageView) findViewById(R.id.profile_picture);

        mSignIn = (Button) findViewById(R.id.sign_in);
        mSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        if (BuildConfig.DEBUG) {
                            Snackbar.make(v, "Success", Snackbar.LENGTH_SHORT).show();
                        }
                        updateUI();
                    }

                    @Override
                    public void onCancel() {
                        if (BuildConfig.DEBUG) {
                            Snackbar.make(v, "Cancel", Snackbar.LENGTH_SHORT).show();
                        }
                        updateUI();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Snackbar.make(v, error.getLocalizedMessage(), Snackbar.LENGTH_SHORT).show();
                        updateUI();
                    }
                });
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, null);
            }
        });

        mSignOut = (Button) findViewById(R.id.sign_out);
        mSignOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                updateUI();
            }
        });

        updateUI();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI() {
        if (isFacebookSignedIn()) {
            mSignIn.setVisibility(View.GONE);
            mSignOut.setVisibility(View.VISIBLE);
        } else {
            mSignIn.setVisibility(View.VISIBLE);
            mSignOut.setVisibility(View.GONE);
        }

        mProfilePicture.post(new Runnable() {
            @Override
            public void run() {
                loadProfilePhoto();
                RadarDrawable radarDrawable = new RadarDrawable();
                radarDrawable.setStartRadius(Math.min(mProfilePicture.getWidth(), mProfilePicture.getHeight()) / 2);
                radarDrawable.start();
                ((ImageView) findViewById(R.id.radar)).setImageDrawable(radarDrawable);
            }
        });
    }

    private boolean isFacebookSignedIn() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    private void loadProfilePhoto() {
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            Uri uri = profile.getProfilePictureUri(mProfilePicture.getWidth(), mProfilePicture.getHeight());
            Picasso.with(this)
                    .load(uri)
                    .placeholder(android.R.color.holo_green_light)
                    .error(android.R.color.holo_red_light)
                    .into(mProfilePicture);
        } else {
            mProfilePicture.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }
    }
}
