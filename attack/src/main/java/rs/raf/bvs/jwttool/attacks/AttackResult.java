package rs.raf.bvs.jwttool.attacks;

import java.util.ArrayList;
import java.util.List;

public class AttackResult {

    private String title;
    private List<AttackStep> steps = new ArrayList<>();
    private String conclusion;
    private boolean success;

    public AttackResult() {}

    public AttackResult(String title) {
        this.title = title;
    }

    public AttackResult section(String text) {
        steps.add(new AttackStep(StepType.SECTION, text));
        return this;
    }

    public AttackResult info(String text) {
        steps.add(new AttackStep(StepType.INFO, text));
        return this;
    }

    public AttackResult warn(String text) {
        steps.add(new AttackStep(StepType.WARN, text));
        return this;
    }

    public AttackResult ok(String text) {
        steps.add(new AttackStep(StepType.OK, text));
        return this;
    }

    public AttackResult fail(String text) {
        steps.add(new AttackStep(StepType.FAIL, text));
        return this;
    }

    public AttackResult code(String text, String code) {
        steps.add(new AttackStep(StepType.CODE, text, code));
        return this;
    }

    public AttackResult code(String code) {
        steps.add(new AttackStep(StepType.CODE, null, code));
        return this;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<AttackStep> getSteps() { return steps; }
    public void setSteps(List<AttackStep> steps) { this.steps = steps; }

    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
