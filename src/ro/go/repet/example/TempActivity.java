package ro.go.repet.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import ro.go.repet.R;
import ro.go.repet.https.CustomContextFactory;
import ro.go.repet.https.CustomSocketFactory;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Main activity
 */
public class TempActivity extends Activity {

	private static final String TAG = TempActivity.class.getSimpleName();

	TextView mainTextView;

	String urlTemp, serverCertName, clientCertName, clientCertPwd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		setContentView(R.layout.main);
		mainTextView = (TextView) findViewById(R.id.mainTextView);
		//
		urlTemp = getResources().getString(R.string.url_temp);
		clientCertName = getResources().getString(R.string.client_cert_file_name);
		clientCertPwd = getResources().getString(R.string.client_cert_password);
		serverCertName = getResources().getString(R.string.server_cert_asset_name);
	}

	@Override
	protected void onResume() {
		super.onResume();
		doRequest();
	}

	private void updateOutput(String text) {
		mainTextView.setText(mainTextView.getText() + "\n" + text);
	}

	private void doRequest() {
		try {
			// do in background
			new AsyncTask<String, String, String>() {
				@Override
				protected String doInBackground(String... values) {
					String result = "N/A";
					HttpURLConnection urlConnection = null;
					//
					try {
						final URL requestedUrl = new URL(urlTemp);
						//
						SSLContext sslContext = CustomContextFactory.getInstance().makeContext(
								getAssets().open(clientCertName), clientCertPwd, getAssets().open(serverCertName));
						//
						HttpsURLConnection.setDefaultSSLSocketFactory(new CustomSocketFactory(sslContext
								.getSocketFactory(), sslContext.getProtocol()));
						//
						HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
							public boolean verify(String hostname, SSLSession session) {
								return requestedUrl.getHost().equals(hostname);
							}
						});
						//
						urlConnection = (HttpURLConnection) requestedUrl.openConnection();
						urlConnection.setConnectTimeout(13000);
						urlConnection.setReadTimeout(10000);
						//
						InputStream in = urlConnection.getInputStream();
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
						//
						result = bufferedReader.readLine();
						//
					} catch (Exception ex) {
						// result = ex.getMessage();
					} finally {
						if (urlConnection != null) {
							urlConnection.disconnect();
						}
					}
					//
					publishProgress(result);
					return result;
				}

				@Override
				protected void onProgressUpdate(String... values) {
					updateOutput(Arrays.toString(values));
				}

				@Override
				protected void onPostExecute(String result) {
					updateOutput("");
				}
			}.execute();
			//
		} catch (Exception x) {
			Log.e(TAG, "Failed to create task", x);
			updateOutput(x.toString());
		}
	}

}
