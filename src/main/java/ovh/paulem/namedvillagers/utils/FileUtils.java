package ovh.paulem.namedvillagers.utils;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import ovh.paulem.namedvillagers.NamedVillagers;

import java.io.*;

public class FileUtils {
    public static<T> T getDataFromResource(Gson gson, String resource, TypeToken<T> token) {
        File file = NamedVillagers.getInstance().getDataFolder()
                .toPath().resolve(resource)
                .toFile();

        try (InputStream inputStream = file.toURI().toURL()
                .openConnection().getInputStream()) {

            if (inputStream == null) {
                return retryResource(gson, resource, token, file);
            }

            Reader reader = new InputStreamReader(inputStream, Charsets.UTF_8);
            return gson.fromJson(reader, token.getType());

        } catch (Exception e) {
            try {
                return retryResource(gson, resource, token, file);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to load resource: " + resource, e);
            }
        }
    }

    private static<T> T retryResource(Gson gson, String resource, TypeToken<T> token, File file) throws IOException {
        InputStream stream = NamedVillagers.getInstance().getResource(resource);

        if (stream == null) {
            throw new RuntimeException("Resource not found: " + resource);
        }

        org.apache.commons.io.FileUtils.copyInputStreamToFile(stream, file);

        return getDataFromResource(gson, resource, token);
    }
}
