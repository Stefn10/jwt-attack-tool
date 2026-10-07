package rs.raf.bvs.jwttool.attacks;

public class AttackStep {

    private StepType type;
    private String text;
    private String code;

    public AttackStep() {}

    public AttackStep(StepType type, String text) {
        this.type = type;
        this.text = text;
    }

    public AttackStep(StepType type, String text, String code) {
        this.type = type;
        this.text = text;
        this.code = code;
    }

    public StepType getType() { return type; }
    public void setType(StepType type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
