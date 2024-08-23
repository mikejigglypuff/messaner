package messaner;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;

public class GsonInstantAdapter extends TypeAdapter<Instant> {

  @Override
  public Instant read(JsonReader in) throws IOException {
    return Instant.parse(in.nextString());
  }

  @Override
  public void write(JsonWriter out, Instant instant) throws IOException {
    out.value(instant.toString());
  }

}
