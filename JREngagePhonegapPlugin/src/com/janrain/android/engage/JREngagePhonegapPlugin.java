/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 Copyright (c) 2010, Janrain, Inc.

 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation and/or
   other materials provided with the distribution.
 * Neither the name of the Janrain, Inc. nor the names of its
   contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 Author: lillialexis
 Date:   12/28/11
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.janrain.android.engage;


import android.widget.Toast;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.AndroidUtils;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
/**
 * Phonegap plugin for authenticating with Janrain Engage
 * <p>
 * result example - {"filename":"/sdcard","isdir":true,"children":[{"filename":"a.txt","isdir":false},{..}]}
 * </p>
 * <pre>
 * {@code
 * successCallback = function(result){
 *     //result is a json
 *
 * }
 * failureCallback = function(error){
 *     //error is error message
 * }
 *
 * </pre>
 * @author Lilli Szafranski and Nathan Ramsey
 *
 */
public class JREngagePhonegapPlugin extends Plugin implements JREngageDelegate {
    private JREngage     mJREngage;
    private PluginResult mResult;
    private JRDictionary mFullAuthenticationResponse;
    private boolean mFinishedPluginExecution;

    @Override
    public synchronized PluginResult execute(final String cmd, final JSONArray args, final String callback) {
        mFinishedPluginExecution = false;
        mResult = null;
        try {
            ctx.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        if(cmd.equals("print")) {
                            showToast(args.getString(0));
                        } else if(cmd.equals("initializeJREngage")) {
                            initializeJREngage(args.getString(0), args.getString(1));
                        } else if (cmd.equals("showAuthenticationDialog")) {
                            showAuthenticationDialog();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.d("[JREngagePhonegapWrapper]", "error: ", e);
        }

        Log.d("[JREngagePhoneGapWrapper]","???");
        // TODO: Maybe need to add more thread protection (got into weird infinite loop); 
        // but could have fixed this already
        while (!mFinishedPluginExecution) {
            Log.d("[JREngagePhoneGapWrapper]", "mFinishedPluginExecution = false");
            try {
                wait();
            } catch (InterruptedException e) {
                Log.d("[JREngagePhoneGapWrapper]", "error: ", e);
//                mResult = new PluginResult(Status.ERROR, "sleep error");
//                mFinishedPluginExecution = true;
            }
        }

        Log.d("[JREngagePhoneGapWrapper]", "mFinishedPluginExecution = true");

        return mResult;
    }

    private synchronized PluginResult showToast(final String message) {
        Toast myToast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
        myToast.show();

//        mResult = new PluginResult(Status.OK, message);
//        mFinishedPluginExecution = true;
//        notifyAll();
//
//        return null;

        return new PluginResult(Status.OK, message);
    }

    private synchronized PluginResult initializeJREngage(String appId, String tokenUrl) {
        JREngage.sLoggingEnabled = true;
        mJREngage = JREngage.initInstance(ctx, appId, tokenUrl, this);

//        if (mJREngage == null)  // TODO: Change error messages
//            mResult = new PluginResult(Status.ERROR, "init error");
//        else
//            mResult = new PluginResult(Status.OK, "Initializing JREngage...");
//
//        mFinishedPluginExecution = true;
//        notifyAll();
//
//        return null;

        return new PluginResult(Status.OK, "Initializing JREngage...");
    }

    private PluginResult buildSuccessResult(JRDictionary successDictionary) {
        String message = successDictionary.toJSON();//.toString();//AndroidUtils.urlEncode(successDictionary.toString());
        
        Log.d("[buildSuccessResult]", message);
        return new PluginResult(Status.OK, message);
    }

    private PluginResult buildFailureResult(JREngageError error) {
        String message = stringFromError(error);//AndroidUtils.urlEncode(stringFromError(error));

        Log.d("[buildFailureResult]", "stringFromError  : " + stringFromError(error));
        Log.d("[buildFailureResult]", "error.toString() : " + error.toString());

        return new PluginResult(Status.ERROR, message);
    }

    // TODO: What does error.toString produce?
    private String stringFromError(JREngageError error)
    {
        JRDictionary errorDictionary = new JRDictionary();
        errorDictionary.put("code", error.getCode());
        errorDictionary.put("message", error.getMessage());

        return errorDictionary.toString();
    }

    private PluginResult showAuthenticationDialog() {
        mJREngage.showAuthenticationDialog();
        return null;
    }

    public synchronized void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        Log.d("[jrEngageDialogDidFailToShowWithError]", "ERROR");
        mResult = buildFailureResult(error);//new PluginResult(Status.ERROR, "sleep error");
        mFinishedPluginExecution = true;
        notifyAll();
    }

    // TODO: What do we do in this case?
    public synchronized void jrAuthenticationDidNotComplete() {
//        Log.d("[jrAuthenticationDidNotComplete]", "ERROR");
//        mResult = buildFailureMessage(stringFromError(null));//new PluginResult(Status.ERROR, "sleep error");
//        mFinishedPluginExecution = true;
//        notifyAll();
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        Log.d("[jrAuthenticationDidSucceedForUser]", "SUCCESS");

        auth_info.remove("stat");

        mFullAuthenticationResponse = new JRDictionary();
        mFullAuthenticationResponse.put("auth_info", auth_info);
        mFullAuthenticationResponse.put("provider", provider);
    }

    public synchronized void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        Log.d("[jrAuthenticationDidFailWithError]", "ERROR");
        mResult = buildFailureResult(error);//new PluginResult(Status.ERROR, "sleep error");
        mFinishedPluginExecution = true;
        notifyAll();
    }

    public synchronized void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
        Log.d("[jrAuthenticationDidReachTokenUrl]", "SUCCESS");

        mFullAuthenticationResponse.put("tokenUrl", tokenUrl);
        mFullAuthenticationResponse.put("tokenUrlPayload", tokenUrlPayload);
        mFullAuthenticationResponse.put("stat", "ok");

        mResult = buildSuccessResult(mFullAuthenticationResponse);
        mFinishedPluginExecution = true;
        notifyAll();
    }

    public synchronized void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        Log.d("[jrAuthenticationCallToTokenUrlDidFail]", "ERROR");
        mResult = buildFailureResult(error);//new PluginResult(Status.ERROR, "sleep error");
        mFinishedPluginExecution = true;
        notifyAll();
    }

    public void jrSocialDidNotCompletePublishing() { }
    public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) { }
    public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) { }
    public void jrSocialDidCompletePublishing() { }
}




