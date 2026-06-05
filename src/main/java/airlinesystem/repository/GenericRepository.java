package airlinesystem.repository;

import java.util.List;

public interface GenericRepository<T> {
    public void add(T object);

    public T get(String id);
    public List<T> getAll();

    public void update(T object);

    public void delete(String id);
}