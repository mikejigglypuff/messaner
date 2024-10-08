package messaner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class GsonFactory {

  public Gson instantGson() {
    return new GsonBuilder()
        .registerTypeAdapter(Instant.class, new GsonInstantAdapter())
        .create();
  }
}
