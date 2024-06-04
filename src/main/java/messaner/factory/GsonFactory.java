package messaner.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import messaner.GsonInstantAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class GsonFactory {
    public Gson instantGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, new GsonInstantAdapter())
                .create();
    }
}
