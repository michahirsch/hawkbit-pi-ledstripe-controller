package org.michahirsch.hawkbit.ledstripe.ddi.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

public class FilePersistenceStrategy implements DownloadPersistenceStrategy {

    private final String FILE_PREFIX = "hawkbit-exec-controller";

    @Override
    public File handleInputStream(final InputStream downloadStream, final String filename) throws IOException {

        final File createTempFile = File.createTempFile(FILE_PREFIX, filename);
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(createTempFile))) {
            ByteStreams.copy(downloadStream, out);
        }finally
        {
            downloadStream.close();
        }
        return createTempFile;
    }

}
