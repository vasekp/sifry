package cz.absolutno.sifry.regexp;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import cz.absolutno.sifry.R;
import cz.absolutno.sifry.Utils;
import cz.absolutno.sifry.regexp.DownloadFragment.ProgressUpdater;

final class DownloadTask extends AsyncTask<Void, Integer, Void> {
	
	private final String savePath;
	private final String downloadUrl;
	private ProgressUpdater updater;
	
    public DownloadTask(ProgressUpdater updater, String downloadUrl, String savePath) {
		super();
		this.downloadUrl = downloadUrl;
		this.savePath = savePath;
		this.updater = updater;
	}

	@Override
    protected Void doInBackground(Void... v) {
        try {
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            long fileLength = connection.getContentLength();
            
            if(isCancelled()) return null;
            
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(savePath);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int)(total*100/fileLength));
                output.write(data, 0, count);
                if(isCancelled()) break;
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
        	publishProgress(-1);
        }
        return null;
    }

	@Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if(progress[0] < 0) {
        	Utils.toast(R.string.tRDChybaStahovani);
        	updater.close(false);
        } else
        	updater.update(progress[0]);
        if(progress[0] == 100)
        	updater.close(true);
    }
	
	@Override
	protected void onCancelled() {
		Utils.toast(R.string.tRDZruseno);
    	updater.close(false);
	}
}