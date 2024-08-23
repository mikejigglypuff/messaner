package messaner.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import lombok.Getter;
import messaner.DTO.ChatDTO;


@Getter
public class Chat {

  private String writer;
  private String message;
  private Instant createdAt;

  public Chat() {
  }

  public Chat(ChatDTO chatDTO, String user) {
    writer = user;
    message = chatDTO.getChat();
    createdAt = Instant.now();
  }

  public Chat(ChatDTO chatDTO, String user, Instant date) {
    writer = user;
    message = chatDTO.getChat();
    createdAt = date;
  }

  public Chat(ChatDTO chatDTO, String user, String date) {
    writer = user;
    message = chatDTO.getChat();
    createdAt = Instant.parse(date);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Chat chat) {
      return this.toString().equals(chat.toString());
    }
    return false;
  }

  @Override
  public String toString() {
    DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.KOREA)
            .withZone(ZoneId.systemDefault());

    StringBuilder sb = new StringBuilder(" writer: ");
    if (writer != null) {
      sb.append(writer);
    } else {
      sb.append("null");
    }
    sb.append(" msg: ");

    if (message != null) {
      sb.append(writer);
    } else {
      sb.append("null");
    }
    sb.append(" createdAt: ");

    if (createdAt != null) {
      sb.append(dateTimeFormatter.format(createdAt));
    } else {
      sb.append("null");
    }

    return sb.toString();
  }
}
