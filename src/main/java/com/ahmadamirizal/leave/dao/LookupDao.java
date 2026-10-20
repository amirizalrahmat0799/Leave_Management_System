package com.ahmadamirizal.leave.dao;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import com.ahmadamirizal.leave.model.Department;
import com.ahmadamirizal.leave.model.Designation;

/** Departments and designations: two small tables with the same shape. */
public class LookupDao extends JdbcDao {

    public LookupDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<Department> departments() {
        return query("SELECT id, name, description FROM departments ORDER BY name",
                rs -> new Department(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
    }

    public Optional<Department> department(int id) {
        return queryOne("SELECT id, name, description FROM departments WHERE id = ?",
                rs -> new Department(rs.getInt("id"), rs.getString("name"), rs.getString("description")), id);
    }

    public void saveDepartment(Department d) {
        if (d.getId() == null) {
            d.setId(insert("INSERT INTO departments (name, description) VALUES (?, ?)", d.getName(), d.getDescription()));
        } else {
            update("UPDATE departments SET name = ?, description = ? WHERE id = ?", d.getName(), d.getDescription(), d.getId());
        }
    }

    public boolean departmentInUse(int id) {
        return !query("SELECT 1 FROM users WHERE department_id = ?", rs -> 1, id).isEmpty();
    }

    public void deleteDepartment(int id) {
        update("DELETE FROM departments WHERE id = ?", id);
    }

    public List<Designation> designations() {
        return query("SELECT id, name, description FROM designations ORDER BY name",
                rs -> new Designation(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
    }

    public Optional<Designation> designation(int id) {
        return queryOne("SELECT id, name, description FROM designations WHERE id = ?",
                rs -> new Designation(rs.getInt("id"), rs.getString("name"), rs.getString("description")), id);
    }

    public void saveDesignation(Designation d) {
        if (d.getId() == null) {
            d.setId(insert("INSERT INTO designations (name, description) VALUES (?, ?)", d.getName(), d.getDescription()));
        } else {
            update("UPDATE designations SET name = ?, description = ? WHERE id = ?", d.getName(), d.getDescription(), d.getId());
        }
    }

    public boolean designationInUse(int id) {
        return !query("SELECT 1 FROM users WHERE designation_id = ?", rs -> 1, id).isEmpty();
    }

    public void deleteDesignation(int id) {
        update("DELETE FROM designations WHERE id = ?", id);
    }
}
