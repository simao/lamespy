package io.simao.lamespy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import io.simao.lamespy.db.DatabaseHelper;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class DumpFileExporter {
    private Context context;
    private DatabaseHelper databaseHelper;

    public DumpFileExporter(Context context, DatabaseHelper databaseHelper) {
        this.context = context;
        this.databaseHelper = databaseHelper;
    }


    private void deleteOldDumps(File dumpDir) {
        String[] files = dumpDir.list();

        for (String f : files) {
            new File(dumpDir, f).delete();
        }
    }

    private File prepareDumpFile() throws IOException {
        File outputDir = new File(context.getCacheDir(), "dumps");
        //noinspection ResultOfMethodCallIgnored
        outputDir.mkdirs();
        deleteOldDumps(outputDir);
        return File.createTempFile("lamespy-", ".json", outputDir);
    }

    public void sendDumpFileIntent() throws IOException, JSONException {
        Uri dumpFileUri = dumpFileUri();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, dumpFileUri);
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    private Uri dumpFileUri() throws IOException, JSONException {
        LocationExporter locationExporter = new LocationExporter(databaseHelper);
        File outputFile = prepareDumpFile();
        locationExporter.writeJsonToFile(outputFile);
        return  FileProvider.getUriForFile(context, "io.simao.lamespy.fileprovider", outputFile);
    }
}
