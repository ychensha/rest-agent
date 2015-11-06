package ac.cn.iscas.agent.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRepository<T, ID> implements Repository<T, ID>{
  protected Map<ID, T> map = new ConcurrentHashMap<>();
  
  public Optional<T> findOne(ID id) {
    return Optional.ofNullable(map.get(id));
  }
  
  public void add(ID id, T t) {
    map.put(id, t);
  }
  
  public void delete(ID id) {
    map.remove(id);
  }
  
  public List<T> findAll() {
    return new ArrayList<>(map.values());
  }
}
