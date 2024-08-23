package messaner.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import messaner.DTO.UserDTO;
import messaner.model.AggNameResult;
import messaner.model.Room;
import messaner.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MongoUserRepository implements UserRepository {

  private final MongoTemplate template;
  private final RoomRepository roomRepository;

  @Autowired
  public MongoUserRepository(ApplicationContext ac, RoomRepository roomRepository) {
    this.template = ac.getBean("mongoTemplate", MongoTemplate.class);
    this.roomRepository = roomRepository;
  }

  @Override
  @Transactional
  public List<User> getSubscribers(String room) {
    Query query = new Query(
        Criteria.where("_id").is(room)
    );
    query.fields().include("subscribers");

    if (roomRepository.roomExists(room)) {
      return template.findOne(query, Room.class).getSubscribers();
    }
    return new ArrayList<>();
  }

  @Override
  @Transactional
  public boolean insertSub(UserDTO userDTO) {
    try {
      if (!roomRepository.roomExists(userDTO.getRoom())) {
        throw new NoSuchElementException();
      }
      if (isSubscribed(userDTO)) {
        return false;
      }

      Query query = new Query(Criteria.where("name").is(userDTO.getRoom()));
      Update update = new Update().push("subscribers", new User(userDTO));
      template.updateFirst(query, update, Room.class);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage()); //로깅 방식 변경할 것
      return false;
    }
  }

  @Override
  @Transactional
  public boolean isSubscribed(UserDTO userDTO) {
    Aggregation agg = Aggregation.newAggregation(
        match(Criteria.where("_id").is(userDTO.getRoom())),
        unwind("subscribers"),
        match(Criteria.where("subscribers.name").is(userDTO.getUser())),
        project().and("subscribers.name").as("name")
    );

    AggregationResults<AggNameResult> result = template.aggregate(agg, "room", AggNameResult.class);
    List<AggNameResult> mappedResult = result.getMappedResults();
    for (AggNameResult r : mappedResult) {
      log.info(r.toString());
    }
    return !mappedResult.isEmpty();
  }

  @Override
  @Transactional
  public boolean removeSub(UserDTO userDTO) {
    try {
      if (!roomRepository.roomExists(userDTO.getRoom())) {
        throw new NoSuchElementException();
      }

      List<User> userList = getSubscribers(userDTO.getRoom());
      if (userList.isEmpty()) {
        throw new NoSuchElementException();
      }

      User removeUser = new User(userDTO);
      userList.remove(removeUser);

      Query removeQuery = new Query(
          Criteria.where("subscribers").elemMatch(Criteria.where("name").is(userDTO.getUser()))
      );
      Update update = new Update().set("subscribers", userList);
      template.updateFirst(removeQuery, update, Room.class);

      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }
  }

  @Override
  @Transactional
  public boolean userExists(String user) {
    Query query = new Query(
        Criteria.where("subscribers").elemMatch(Criteria.where("name").is(user)));
    query.fields().include("name");
    return template.exists(query, Room.class);
  }


}
