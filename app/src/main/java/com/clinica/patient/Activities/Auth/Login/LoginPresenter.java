package com.clinica.patient.Activities.Auth.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Models.User;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.ToastTool;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

class LoginPresenter extends BasePresenter {

    private LoginActivity activity;
    private LoginView view;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private DatabaseReference mReference;
    private GoogleSignInClient mGoogleSignInClient;

    private static final String TAG = "Login";
    private static final int RC_SIGN_IN = 77;


    LoginPresenter(final LoginView view, final LoginActivity activity) {
        super(view, activity);
        this.activity = activity;
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        this.mReference = FirebaseDatabase.getInstance().getReference();

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, loginResultFacebookCallback);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            view.showEmailError(R.string.empty_email);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError(R.string.invalid_email);
            return false;
        }
        return true;
    }

    boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            view.showPasswordError(R.string.empty_password);
            return false;
        }
        if (password.trim().length() < 8) {
            view.showPasswordError(R.string.invalid_password);
            return false;
        }
        return true;
    }

    void loginWithEmail(String email, String password) {
        view.showLoading();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, authResultCompleteListener);
    }

    void loginWithFacebook() {
        view.showLoading();
        LoginManager.getInstance().logInWithReadPermissions(activity,
                Arrays.asList("public_profile", "email", "user_birthday", "user_gender"));
    }

    void loginWithGoogle() {
        view.showLoading();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                if (acct != null) {
                    User user = new User();
                    user.setGoogleID(acct.getId());
                    user.setDisplayName(acct.getDisplayName());
                    user.setEmail(acct.getEmail());
                    user.setPhotoUrl(acct.getPhotoUrl().toString());
                    user.setCreationTimestamp(new Date().getTime());
                    user.setAccountStatus(User.ACTIVE_STATUS);

                    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                    signInWithCredential(credential, user);
                } else {
                    view.hideLoading();
                    ToastTool.with(activity, R.string.error_happened).show();
                }

            } catch (ApiException e) {
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                view.hideLoading();
                ToastTool.with(activity, e.getCause().getLocalizedMessage()).show();
            }
        } else
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void signInWithCredential(AuthCredential credential, final User user) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> authResultTask) {
                if (authResultTask.isSuccessful()) {
                    mReference.child(Constants.DataBase.USERS_NODE).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(authResultTask.getResult().getUser().getUid())) {
                                mReference.child(Constants.DataBase.USERS_NODE).child(authResultTask.getResult().getUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        view.hideLoading();
                                        if (authResultTask.isSuccessful()) {
                                            view.openMain();
                                        } else {
                                            ToastTool.with(activity, R.string.error_happened).show();
                                            FirebaseAuth.getInstance().signOut();
                                            LoginManager.getInstance().logOut();
                                        }
                                    }
                                });
                            } else {
                                view.hideLoading();
                                view.openMain();
                            }
                            mReference.child(Constants.DataBase.USERS_NODE).removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            view.hideLoading();
                            ToastTool.with(activity, R.string.error_happened).show();
                            FirebaseAuth.getInstance().signOut();
                            LoginManager.getInstance().logOut();
                        }
                    });
                } else {
                    view.hideLoading();
                    ToastTool.with(activity, R.string.error_happened).show();
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                }
            }
        });
    }

    private FacebookCallback<LoginResult> loginResultFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(final LoginResult loginResult) {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                User user = new User();
                                if (object.has("id"))
                                    user.setFacebookID(object.getString("id"));
                                if (object.has("name"))
                                    user.setDisplayName(object.getString("name"));
                                if (object.has("email"))
                                    user.setEmail(object.getString("email"));
                                if (object.has("picture"))
                                    user.setPhotoUrl(object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                if (object.has("birthday")) {
                                    String birthdayString = object.getString("birthday");
                                    DateFormat dateFormat = new SimpleDateFormat("MM/DD/YYYY");
                                    long birthdayTimestamp = dateFormat.parse(birthdayString).getTime();
                                    user.setBirthdayTimestamp(birthdayTimestamp);
                                }
                                if (object.has("gender"))
                                    user.setGender(object.getString("gender"));

                                user.setCreationTimestamp(new Date().getTime());
                                user.setAccountStatus(User.ACTIVE_STATUS);
                                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                signInWithCredential(credential, user);

                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                                view.hideLoading();
                                ToastTool.with(activity, R.string.error_happened).show();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday,picture.width(2000)");
            request.setParameters(parameters);
            request.executeAsync();

        }

        @Override
        public void onCancel() {
            // App code
            Log.i(TAG, "onCancel: ");
            view.hideLoading();
        }

        @Override
        public void onError(FacebookException exception) {
            // App code
            Log.i(TAG, "onError: " + exception.getLocalizedMessage());
            ToastTool.with(activity, exception.getLocalizedMessage());
            view.hideLoading();
        }
    };

    private OnCompleteListener<AuthResult> authResultCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            view.hideLoading();
            if (task.isSuccessful()) {
                view.openMain();
            } else {
                view.showPasswordError(R.string.invalid_password);
            }
        }
    };
}
