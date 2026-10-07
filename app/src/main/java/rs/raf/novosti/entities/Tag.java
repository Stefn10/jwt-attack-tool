package rs.raf.novosti.entities;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Tag {

    private Long id;

    @NotNull(message = "Tag value is required")
    @NotEmpty(message = "Tag value is required")
    private String value;

    public Tag() {
    }

    public Tag(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
