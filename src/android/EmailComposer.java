/**
 * 
 * Phonegap Email composer plugin for Android with multiple attachments handling
 * 
 * Version 1.0
 * 
 * Guido Sabatini 2012
 *
 * Version 1.3
 *
 * Jia Chang Jee 2013
 *
 * Version 1.4
 *
 * Markus Plutka 2014
 *
 */

package de.hellmannecommerce.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Base64;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;

public class EmailComposer extends CordovaPlugin {

	private CallbackContext command;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

		this.command = callbackContext;

		if ("showEmailComposer".equals(action)) {
			try {
				JSONObject parameters = args.getJSONObject(0);
				if (parameters != null) {
					sendEmail(parameters);
				} else {
                                    LOG.e("EmailComposer", "No parameters");
                                }
			} catch (Exception e) {
				LOG.e("EmailComposer", "Unable to send email");
			}
			// callbackContext.success();
			return true;
		}

                if ("isServiceAvailable".equals(action)) {
                    isServiceAvailable();

                    return true;
                }

		return false;  // Returning false results in a "MethodNotFound" error.
	}

        /**
         * Überprüft, ob Emails versendet werden können.
         */
        private void isServiceAvailable () {
                Boolean available   = isEmailAccountConfigured();
                PluginResult result = new PluginResult(PluginResult.Status.OK, available);

                command.sendPluginResult(result);
        }

        /**
         * Gibt an, ob es eine Anwendung gibt, welche E-Mails versenden kann.
         */
        private Boolean isEmailAccountConfigured () {
                Intent  intent    = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","max@mustermann.com", null));
                Boolean available = cordova.getActivity().getPackageManager().queryIntentActivities(intent, 0).size() > 0;

                return available;
        }

	private void sendEmail(JSONObject parameters) {
		
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                final EmailComposer plugin = this;
		
		//String callback = parameters.getString("callback");

		boolean isHTML = false;
		try {
			isHTML = parameters.getBoolean("bIsHTML");
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling isHTML param: " + e.toString());
		}

		if (isHTML) {
			emailIntent.setType("text/html");
		} else {
			emailIntent.setType("text/plain");
		}

		// setting subject
		try {
			String subject = parameters.getString("subject");
			if (subject != null && subject.length() > 0) {
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			}
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling subject param: " + e.toString());
		}

		// setting body
		try {
			String body = parameters.getString("body");
			if (body != null && body.length() > 0) {
				if (isHTML) {
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(body));
				} else {
                                        // mplutka@hellmann.net: Try fix for exception -> empty body
                                        /*
                                        ArrayList<CharSequence> extra_text = new ArrayList<CharSequence>();
                                        extra_text.add(body);
                                        emailIntent.putCharSequenceArrayListExtra(android.content.Intent.EXTRA_TEXT, extra_text);
                                        */
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
				}
			}
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling body param: " + e.toString());
		}

		// setting TO recipients
		try {
			JSONArray toRecipients = parameters.getJSONArray("toRecipients");
			if (toRecipients != null && toRecipients.length() > 0) {
				String[] to = new String[toRecipients.length()];
				for (int i=0; i<toRecipients.length(); i++) {
					to[i] = toRecipients.getString(i);
				}
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to);
			}
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling toRecipients param: " + e.toString());
		}

		// setting CC recipients
		try {
			JSONArray ccRecipients = parameters.getJSONArray("ccRecipients");
			if (ccRecipients != null && ccRecipients.length() > 0) {
				String[] cc = new String[ccRecipients.length()];
				for (int i=0; i<ccRecipients.length(); i++) {
					cc[i] = ccRecipients.getString(i);
				}
				emailIntent.putExtra(android.content.Intent.EXTRA_CC, cc);
			}
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling ccRecipients param: " + e.toString());
		}

		// setting BCC recipients
		try {
			JSONArray bccRecipients = parameters.getJSONArray("bccRecipients");
			if (bccRecipients != null && bccRecipients.length() > 0) {
				String[] bcc = new String[bccRecipients.length()];
				for (int i=0; i<bccRecipients.length(); i++) {
					bcc[i] = bccRecipients.getString(i);
				}
				emailIntent.putExtra(android.content.Intent.EXTRA_BCC, bcc);
			}
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling bccRecipients param: " + e.toString());
		}

		// setting attachments
		try {
			JSONArray attachments = parameters.getJSONArray("attachments");
			if (attachments != null && attachments.length() > 0) {
				ArrayList<Uri> uris = new ArrayList<Uri>();
				//convert from paths to Android friendly Parcelable Uri's
				for (int i=0; i<attachments.length(); i++) {
					try {
						File file = new File(attachments.getString(i));
						if (file.exists()) {
							Uri uri = Uri.fromFile(file);
							uris.add(uri);
						}
					} catch (Exception e) {
						LOG.e("EmailComposer", "Error adding an attachment: " + e.toString());
					}
				}
				if (uris.size() > 0) {
					emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				}
			}
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling attachments param: " + e.toString());
		}

                    
		// setting attachments data
		try {
			JSONArray attachmentsData = parameters.getJSONArray("attachmentsData");
			if (attachmentsData != null && attachmentsData.length() > 0) {
				ArrayList<Uri> uris = new ArrayList<Uri>();
				for (int i=0; i<attachmentsData.length(); i++) {
					JSONArray fileInformation = attachmentsData.getJSONArray(i);
					
					String filename = fileInformation.getString(0);
					String filedata = fileInformation.getString(1);
					
					byte[] fileBytes = Base64.decode(filedata, 0);
					File filePath = new File(this.cordova.getActivity().getCacheDir() + "/" + filename);
					FileOutputStream os = new FileOutputStream(filePath, true);
					os.write(fileBytes);
					os.flush();
					os.close();
					
					// Uri uri = Uri.fromFile(filePath);
					Uri uri = Uri.parse("content://" + EmailAttachmentProvider.AUTHORITY + "/" + filename);
					uris.add(uri);
				}
				if (uris.size() > 0) {
					emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				}
			}
		} catch (Exception e) {
			LOG.e("EmailComposer", "Error handling attachmentsData param: " + e.toString());
		} 
		
                this.cordova.getThreadPool().execute( new Runnable() {
                    public void run() {
                        cordova.startActivityForResult(plugin, Intent.createChooser(emailIntent, "Select Email App"), 0);
                    }
                });
		//this.cordova.startActivityForResult(this, emailIntent, 0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
                super.onActivityResult(requestCode, resultCode, intent);
		LOG.e("EmailComposer", "ResultCode: " + resultCode);

		// resultCode is always 0 because Android doesn't provide a different resultCode e.g. for email discarded or saved
                // so we set it to 2 here
		command.success(2);
	}

}
