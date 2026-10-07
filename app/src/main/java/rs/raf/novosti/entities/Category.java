package rs.raf.novosti.entities;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Category {

    private Long id;

    @NotNull(message = "Name field is required")
    @NotEmpty(message = "Name field is required")
    private String name;

    @NotNull(message = "Description field is required")
    @NotEmpty(message = "Description field is required")
    private String description;

    public Category() {
    }

    public Category(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
